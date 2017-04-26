package uk.ac.ebi.pride.archive.web.service.controller.viewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.archive.web.service.model.viewer.*;
import uk.ac.ebi.pride.proteinidentificationindex.mongo.search.model.MongoProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.mongo.search.service.MongoProteinIdentificationSearchService;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.psmindex.mongo.search.model.MongoPsm;
import uk.ac.ebi.pride.psmindex.mongo.search.service.MongoPsmSearchService;
import uk.ac.ebi.pride.psmindex.search.model.Psm;
import uk.ac.ebi.pride.psmindex.search.service.PsmSearchService;
import uk.ac.ebi.pride.spectrumindex.search.service.SpectrumSearchService;

import java.util.*;

/**
 * @author Florian Reisinger
 * @since 0.1
 */
@SuppressWarnings("UnusedDeclaration")
public class ViewerControllerImpl {

    private static final Logger logger = LoggerFactory.getLogger(ViewerControllerImpl.class);



    private ProteinIdentificationSearchService proteinIdentificationSearchService;
    private PsmSearchService psmSearchService;
    private MongoProteinIdentificationSearchService mongoProteinIdentificationSearchService;
    private MongoPsmSearchService mongoPsmSearchService;
    private SpectrumSearchService spectrumSearchService;

    public ViewerControllerImpl(ProteinIdentificationSearchService proteinIdentificationSearchService,
                                MongoProteinIdentificationSearchService mongoProteinIdentificationSearchService,
                                PsmSearchService psmSearchService,
                                MongoPsmSearchService mongoPsmSearchService,
                                SpectrumSearchService spectrumSearchService) {
        this.proteinIdentificationSearchService = proteinIdentificationSearchService;
        this.mongoProteinIdentificationSearchService = mongoProteinIdentificationSearchService;
        this.psmSearchService = psmSearchService;
        this.mongoPsmSearchService = mongoPsmSearchService;
        this.spectrumSearchService = spectrumSearchService;
    }

    protected ViewerControllerImpl() {
    }


    public Protein getProteinData(String proteinID) throws InvalidDataException {
        // the webapp requires a unique protein ID, which is generated as: <assay accession>__<protein accession>
        // Note: the protein accession is the submitted accession used to identify the protein identification of the respective assay
        // since this is for internal use only, this could potentially be a PRIDE internal id as well...
        logger.info("Protein " + proteinID + " requested...");

        String assayAccession = getAssayAccessionFromProteinID(proteinID);
        String proteinAccession = getProteinAccessionFromProteinID(proteinID);
        logger.debug("Extracted assay accession " + assayAccession + " and protein accession " + proteinAccession);


        List<ProteinIdentification> proteins = proteinIdentificationSearchService.findByAssayAccessionAndAccession(proteinAccession, assayAccession);
        if (proteins == null || proteins.isEmpty()) {
            logger.debug("No protein found!");
            return null;
        }
        if (proteins.size() > 1) {
            logger.debug("More than one protein found!");
            throw new InvalidDataException("Invalid protein record! Non-unique result for: " + proteinID);
        }

        ProteinIdentification protein = proteins.iterator().next();
        String proteinId = protein.getId();
        MongoProteinIdentification mongoProteinIdentification = mongoProteinIdentificationSearchService.findById(proteinId);

        if (mongoProteinIdentification.getSubmittedSequence() == null || mongoProteinIdentification.getSubmittedSequence().length() < 5) {
            if (mongoProteinIdentification.getInferredSequence() == null || mongoProteinIdentification.getInferredSequence().length() < 5) {
                logger.debug("No valid protein record!");
                throw new InvalidDataException("Invalid protein record! No valid sequence for: " + proteinID);
            }
        }

        // create a new WS Protein record
        Protein resultProtein = ObjectMapper.mapProteinIdentifiedToWSProtein(mongoProteinIdentification);
        // set the protein ID to uniquely identify this protein record
        resultProtein.setId(proteinID);
        // ToDo: we don't know the species! Could be provided by search? or remove completely?
        resultProtein.setTaxonID(-1);
        // ToDo: we have no tissue information! get tissue(s) from protein/assay level? or remove completely?

        // now add the peptides to the Protein
        // retrieve all PSMs to generate the information needed for a Protein record
        List<Psm> psms = psmSearchService.findByProteinAccessionAndAssayAccession(proteinAccession, assayAccession);
        List<String> psmIDs = new ArrayList<>();
        for (Psm psm : psms) {
            psmIDs.add(psm.getId());
        }
        List<MongoPsm> mongoPsms = mongoPsmSearchService.findByIdIn(psmIDs);
        // create list of 'peptide' objects collapsing PSMs
        Collection<PeptideMatch> peptides = mapPsms2WSPeptidesFiltered(mongoPsms);
        // take into account if the peptide sequence matches the protein sequence at the stated location
        adjustPeptideProteinMatches(peptides, resultProtein.getSequence());
        resultProtein.getPeptides().addAll(peptides);

        /*
        // create PeptideMatches for all PSMs, collapsing them by sequence and position
        // accumulating modifications (except terminal ones) and
        // setting the uniqueness flag if the reported position matches the calculated one
        // ToDo: optimise: treat matching peptides differently than non-matching and remap only those not matching
        PeptideMapper mapper = new PeptideMapper(psms, resultProtein.getSequence());
        Collection<PeptideMatch> peptideMatches = mapper.getMatchedPeptides();

        // add the generated list of peptides to the protein record
        resultProtein.getPeptides().addAll(peptideMatches);

        // we can't rely on the provided positions, as we have remapped the peptides
        // and the locations could have shifted
        inferProteinModifications(resultProtein);
        */

        logger.debug("Returning protein Id=" + resultProtein.getId() + " Accession=" + resultProtein.getAccession());
        return resultProtein;
    }

    public PeptideList getPSMData(String peptideID) {
        logger.info("Request for PSMs for peptide " + peptideID);
        // retrieve the PSMs for a particular peptide sequence of a particular protein

        String psmSequence = getPsmSequenceFromPsmID(peptideID);
        String proteinAccession = getProteinAccessionFromPsmID(peptideID);
        String assayAccession = getAssayAccessionFromPsmID(peptideID);
        logger.debug("Peptide ID decoded into: assay=" + assayAccession + " protein=" + proteinAccession + " seq=" + psmSequence);


        List<Peptide> result;
        if (proteinAccession.equalsIgnoreCase(DummyDataCreator.PROT_1_ACCESSION) && assayAccession.equalsIgnoreCase(DummyDataCreator.ASSAY_ACCESSION)) {
            List<Peptide> allPsms = DummyDataCreator.createDummyPeptideList();
            List<Peptide> filteredPsms = new ArrayList<Peptide>();

            // only add those peptides where the sequence matches the requested one
            for (Peptide peptide : allPsms) {
                if (peptide.getSequence().equalsIgnoreCase(psmSequence)) {
                    filteredPsms.add(peptide);
                }
            }
            result = filteredPsms;
        } else {
            // retrieve ALL PSMs for the protein (since there is no search option to further restrict the result by peptide sequence)
            List<Psm> psms = psmSearchService.findByProteinAccessionAndAssayAccession(proteinAccession, assayAccession);
            if (psms == null || psms.isEmpty()) {
                return null;
            }
            List<String> mognoPsmIds = new ArrayList<>();
            for (Psm psm : psms) {
                mognoPsmIds.add(psm.getId());
            }
            List<MongoPsm> mongoPsms = mongoPsmSearchService.findByIdIn(mognoPsmIds);
            // and then filter out the peptides we are not interested in
            List<MongoPsm> filteredPsms = new ArrayList<>();
            for (MongoPsm psm : mongoPsms) {
                if (psm.getPeptideSequence().equalsIgnoreCase(psmSequence)) {
                    filteredPsms.add(psm);
                }
            }
            result = ObjectMapper.mapPsms2WSPeptides(filteredPsms, true);
        }

        return new PeptideList(result);
    }


    public Spectrum getSpectrumData(String variationId) throws InvalidDataException {
        logger.info("Request for Spectrum for variation " + variationId);
        // retrieve the Spectrum for a particular peptide sequence of a particular protein
        String assayAccession = getAssayAccessionFromVariationID(variationId);
        String proteinAccession = getProteinAccessionFromVariationID(variationId);
        String reportedId = getReportedIdFromVariationId(variationId);
        String peptideSequence = getPeptideSequenceFromVariationId(variationId);
        logger.debug("Variation ID decoded into: assay=" + assayAccession + " protein=" + proteinAccession +
            " seq=" + peptideSequence + " reportedId=" + reportedId);
        List<Psm> psms = psmSearchService.findByReportedIdAndAssayAccessionAndProteinAccessionAndPeptideSequence(reportedId,
            assayAccession, proteinAccession, peptideSequence);
        // we assert the we have one and only one PSM for the provided ID
        if (psms == null || psms.size() != 1) {
            throw new InvalidDataException("No unique PSM found for unique identifier: " + variationId);
        }
        String psmId = psms.get(0).getId();
        MongoPsm mongoPsm = mongoPsmSearchService.findById(psmId);
        logger.debug("Found PSM=" + mongoPsm.getId());

        //For mongodb escaping is not needed
        uk.ac.ebi.pride.spectrumindex.search.model.Spectrum spectrumSearchResult = spectrumSearchService.findById(mongoPsm.getSpectrumId());

        // we assert that we have one and only one spectrum for the provided spectrum ID
        if (spectrumSearchResult == null) {
            throw new InvalidDataException("No spectra data for spectrum with ID: " + mongoPsm.getSpectrumId() + " for PSM: " + mongoPsm.getId() + " and variant ID: " + variationId);
        }
        logger.debug("Found Spectrum=" + spectrumSearchResult);
        // convert the spectrum from the index into a spectrum object of the web service
        Spectrum spectrum = ObjectMapper.mapIndexSpectrum2WSSpectrum(spectrumSearchResult);
        spectrum.setId(variationId); // overwrite the spectrum ID to use the ID system of the webapp
        return spectrum;
    }


//    protected static void inferProteinModifications(Protein protein) {
//        // erase any existing modifications first
//        protein.getModifiedLocations().clear();
//
//        // if there is no protein sequence or it seems invalid, we can't proceed with the inference
//        if (protein.getSequence() == null || protein.getSequence().length() < 5) {
//            throw new IllegalArgumentException("Can not infer modifications for protein with invalid sequence: " + protein.getAccession());
//        }
//        // if there are no peptides to possibly infer modifications from, there is nothing to do!
//        if (protein.getPeptides() == null || protein.getPeptides().isEmpty()) {
//            return;
//        }
//
//        // ToDo: take biological significance into account
//        for (PeptideMatch peptideMatch : protein.getPeptides()) {
//            // if there are no modifications, we move on...
//            if (peptideMatch.getModifiedLocations() == null || peptideMatch.getModifiedLocations().isEmpty()) {
//                continue;
//            }
//            int startPos = protein.getSequence().indexOf(peptideMatch.getSequence());
//            while (startPos != -1) {
//                for (ModifiedLocation modifiedLocation : peptideMatch.getModifiedLocations()) {
//                    int pos = modifiedLocation.getPosition();
//                    // only create a modification on the protein if it is within the peptide
//                    // and not on the terminal edges
//                    // ToDo: handle terminal modifications if the peptide terminal = the protein terminal
//                    if (pos > 0 && pos <= peptideMatch.getSequence().length()) {
//                        ModifiedLocation protMod = new ModifiedLocation();
//                        protMod.setPosition(startPos + pos);
//                        protMod.setModification(modifiedLocation.getModification());
//                        protein.getModifiedLocations().add(protMod);
//                    }
//                }
//                // check if the peptide matches the protein on another location
//                startPos = protein.getSequence().indexOf(peptideMatch.getSequence(), startPos + 1);
//            }
//        }
//    }


    /**
     * Highjack the uniqueness flag to assign a positive value if the peptide sequence matches
     * the protein sequence at the specified position and a negative value otherwise.
     * Note: this does not check if the peptide sequence matches the protein sequence at all.
     *       It only checks if it matches were it is supposed to match, e.g. on peptide.getPosition().
     *
     * @param peptides the peptides to match against the protein sequence
     * @param protSeq the protein sequence to match the peptides against
     */
    private void adjustPeptideProteinMatches(Iterable<PeptideMatch> peptides, String protSeq) {
        // note: we only adjust the uniqueness flag according to whether the peptide matches at the stated position
        for (PeptideMatch peptide : peptides) {
            String pepSeq = peptide.getSequence();
            if ( protSeq.regionMatches(peptide.getPosition() - 1, pepSeq, 0, pepSeq.length()) ) {
                peptide.setUniqueness(1);
            } else {
                peptide.setUniqueness(-1);
            }
        }
    }

    // This method is similar to the method mapPsms2WSPeptides mapping Psm objects to Peptide objects,
    // with two major differences:
    // a) it collapses peptides with the same sequence and start position and
    // b) it will remove modifications on C/N-terminal.
    private static Collection<PeptideMatch> mapPsms2WSPeptidesFiltered(List<MongoPsm> psms) {
        Map<String, PeptideMatch> peptideMatchMap = new HashMap<String, PeptideMatch>();

        // we have to combine PSMs to peptides that are unique by their sequence + start position
        // include start position in ID to distinguish peptides with the same sequence,
        // but mapped to different locations on the protein sequence
        PeptideMatch mappedObject;
        for (MongoPsm psm : psms) {
            String peptideMatchId;
            if (psm.getStartPosition() == null) {
                peptideMatchId = psm.getPeptideSequence();
            } else {
                peptideMatchId = psm.getPeptideSequence() + ":" + psm.getStartPosition();
            }

            mappedObject = peptideMatchMap.get(peptideMatchId);
            // create a new record one does not already exist
            if ( mappedObject == null ) {
                Peptide pep = ObjectMapper.mapPsm2WSPeptide(psm, false);
                // clear all modifications, as we add them new skipping terminal modifications
                mappedObject = new PeptideMatch();
                mappedObject.setId(pep.getId());
                mappedObject.setSequence(pep.getSequence());
                mappedObject.setTaxonID(pep.getTaxonID());
                mappedObject.setAssays(pep.getAssays());
                mappedObject.setTissues(pep.getTissues());
                mappedObject.setPosition(psm.getStartPosition() == null ? -1 : psm.getStartPosition());
                peptideMatchMap.put(peptideMatchId, mappedObject);
            }

            // add cumulative data (for now: modifications) to the record
            if (psm.getModifications() != null) {
                // add the ModificationLodationS, the Peptide will take care of removing duplicated ones
                try {
                    // add modifications skipping terminal modifications
                    for (ModifiedLocation mod : ObjectMapper.mapPsmModifications2WSPeptideModifiedLocations(psm.getModifications())) {
                        // if the modification is within the peptide sequence, e.g not a terminal modification
                        // we add the modification, otherwise we skip it
                        if (mod.getPosition() > 0 && mod.getPosition() < psm.getPeptideSequence().length() +1) {
                            mappedObject.getModifiedLocations().add(mod);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return peptideMatchMap.values();
    }

    public static String getPeptideSequenceFromVariationId(String variationId) {
        // convention: <assay accession><double underscore><protein accession><double underscore><peptide_sequence><double underscore><reported_id>;
        // example   : variance=8128__P02768__DAHKSEVAHR__1417
        String[] parts = variationId.split("__");
        if (parts.length != 4) {
            throw new IllegalArgumentException("A valid variation ID need to have four parts separated by double underscore!");
        }
        return parts[2];
    }

    public static String getReportedIdFromVariationId(String variationId) {
        // convention: <assay accession><double underscore><protein accession><double underscore><peptide_sequence><double underscore><reported_id>;
        // example   : variance=8128__P02768__DAHKSEVAHR__1417
        String[] parts = variationId.split("__");
        if (parts.length != 4) {
            throw new IllegalArgumentException("A valid variation ID need to have four parts separated by double underscore!");
        }
        return parts[3];
    }

    public static String getProteinAccessionFromVariationID(String variationId) {
        // convention: <assay accession><double underscore><protein accession><double underscore><peptide_sequence><double underscore><reported_id>;
        // example   : variance=8128__P02768__DAHKSEVAHR__1417
        String[] parts = variationId.split("__");
        if (parts.length != 4) {
            throw new IllegalArgumentException("A valid variation ID need to have four parts separated by double underscore!");
        }
        return parts[1];
    }

    public static String getAssayAccessionFromVariationID(String variationId) {
        // convention: <assay accession><double underscore><protein accession><double underscore><peptide_sequence><double underscore><reported_id>;
        // example   : variance=8128__P02768__DAHKSEVAHR__1417
        String[] parts = variationId.split("__");
        if (parts.length != 4) {
            throw new IllegalArgumentException("A valid variation ID need to have four parts separated by double underscore!");
        }
        return parts[0];
    }

    public static String getAssayAccessionFromProteinID(String proteinID) {
        // convention: <assay accession><double underscore><protein accession>;
        // example   : 13567__P12345
        String[] parts = proteinID.split("__");
        if (parts.length != 2) {
            throw new IllegalArgumentException("A valid protein ID need to have two parts separated by double underscore!");
        }
        return parts[0];
    }
    private static String getProteinAccessionFromProteinID(String proteinID) {
        // convention: <assay accession><double underscore><protein accession>;
        // example   : 13567__P12345
        String[] parts = proteinID.split("__");
        if (parts.length != 2) {
            throw new IllegalArgumentException("A valid protein ID need to have two parts separated by double underscore!");
        }
        return parts[1];
    }
    public static String getAssayAccessionFromPsmID(String psmID) {
        // convention: <assay accession><double underscore><protein accession><double underscore><psm sequence>;
        // example   : 13567__P12345__NDRQSLNISNK
        String[] parts = psmID.split("__");
        if (parts.length != 3) {
            throw new IllegalArgumentException("A valid psm ID need to have two parts separated by double underscore!");
        }
        return parts[0];
    }
    private static String getProteinAccessionFromPsmID(String psmID) {
        // convention: <assay accession><double underscore><protein accession><double underscore><psm sequence>;
        // example   : 13567__P12345__NDRQSLNISNK
        String[] parts = psmID.split("__");
        if (parts.length != 3) {
            throw new IllegalArgumentException("A valid psm ID need to have two parts separated by double underscore!");
        }
        return parts[1];
    }
    private static String getPsmSequenceFromPsmID(String psmID) {
        // convention: <assay accession><double underscore><protein accession><double underscore><psm sequence>;
        // example   : 13567__P12345__NDRQSLNISNK
        String[] parts = psmID.split("__");
        if (parts.length != 3) {
            throw new IllegalArgumentException("A valid psm ID need to have two parts separated by double underscore!");
        }
        return parts[2];
    }



}
