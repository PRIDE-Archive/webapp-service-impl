package uk.ac.ebi.pride.archive.web.service.controller.viewer;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.archive.web.service.model.viewer.*;
import uk.ac.ebi.pride.archive.web.service.model.viewer.Spectrum;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationSearchService;
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

    private static final String NEUTRAL_LOSS = "neutral loss";


    private ProteinIdentificationSearchService proteinIdentificationSearchService;
    private PsmSearchService psmSearchService;
    private SpectrumSearchService spectrumSearchService;

    public ViewerControllerImpl(ProteinIdentificationSearchService proteinIdentificationSearchService, PsmSearchService psmSearchService, SpectrumSearchService spectrumSearchService) {
        this.psmSearchService = psmSearchService;
        this.proteinIdentificationSearchService = proteinIdentificationSearchService;
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


        List<ProteinIdentification> proteins = proteinIdentificationSearchService.findBySubmittedAccessionAndAssayAccession(proteinAccession, assayAccession);
        if (proteins == null || proteins.isEmpty()) {
            logger.debug("No protein found!");
            return null;
        }
        if (proteins.size() > 1) {
            logger.debug("More than one protein found!");
            throw new InvalidDataException("Invalid protein record! Non-unique result for: " + proteinID);
        }

        ProteinIdentification foundProtein = proteins.iterator().next();
        if (foundProtein.getSubmittedSequence() == null || foundProtein.getSubmittedSequence().length() < 5) {
            if (foundProtein.getInferredSequence() == null || foundProtein.getInferredSequence().length() < 5) {
                logger.debug("No valid protein record!");
                throw new InvalidDataException("Invalid protein record! No valid sequence for: " + proteinID);
            }
        }

        // create a new WS Protein record
        Protein resultProtein = mapProteinIdentifiedToWSProtein(foundProtein);
        // set the protein ID to uniquely identify this protein record
        resultProtein.setId(proteinID);

        // now add the peptides to the Protein
        // retrieve all PSMs to generate the information needed for a Protein record
        List<Psm> psms = psmSearchService.findByProteinAccessionAndAssayAccession(proteinAccession, assayAccession);

        // create list of 'peptide' objects collapsing PSMs
        List<PeptideMatch> peptides = mapPsms2WSPeptidesFiltered(psms, PeptideMatch.class);
        // take into account if the peptide sequence matches the protein sequence at the stated location
        adjustPeptideProteinMatches(peptides, resultProtein.getSequence());
        resultProtein.getPeptides().addAll(peptides);

        // infer protein modifications from list of peptides
        // no longer needed, as modifications are now available from the protein record directly
        // possible mismatches are ignored
//            inferProteinModifications(resultProtein);

        // ToDo: we don't know the species! Could be provided by search? or remove completely?
        resultProtein.setTaxonID(-1);

        // ToDo: we have no tissue information! get tissue(s) from protein/assay level? or remove completely?

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
            // and then filter out the peptides we are not interested in
            List<Psm> filteredPsms = new ArrayList<Psm>();
            for (Psm psm : psms) {
                if (psm.getPeptideSequence().equalsIgnoreCase(psmSequence)) {
                    filteredPsms.add(psm);
                }
            }

            result = mapPsms2WSPeptides(filteredPsms, Peptide.class);
        }

        return new PeptideList(result);
    }


    public Spectrum getSpectrumData(String variationId) {
        logger.info("Request for Spectrum for variation " + variationId);
        // retrieve the Spectrum for a particular peptide sequence of a particular protein

        String assayAccession = getAssayAccessionFromVariationID(variationId);
        String proteinAccession = getProteinAccessionFromVariationID(variationId);
        String reportedId = getReportedIdFromVariationId(variationId);
        String peptideSequence = getPeptideSequenceFromVariationId(variationId);
        logger.debug("Variation ID decoded into: assay=" + assayAccession + " protein=" + proteinAccession + " seq=" + peptideSequence + " reportedId=" + reportedId);

        List<Psm> psms = psmSearchService.findByReportedIdAndAssayAccessionAndProteinAccessionAndPeptideSequence(reportedId, assayAccession, proteinAccession, peptideSequence);

        Spectrum spectrum = null;
        if (psms != null && psms.size()>0) {
            Psm psm = psms.get(0);
            logger.debug("Found PSM=" + psm.getId());
            List<uk.ac.ebi.pride.spectrumindex.search.model.Spectrum> spectrumSearchResult = spectrumSearchService.findById(psm.getSpectrumId());
            if (spectrumSearchResult != null && spectrumSearchResult.size()>0) {
                uk.ac.ebi.pride.spectrumindex.search.model.Spectrum sp = spectrumSearchResult.get(0);
                logger.debug("Found Spectrum=" + sp.getId());
                spectrum = new Spectrum();
                spectrum.setId(variationId);
                spectrum.setPeaks(SpectrumPeak.getAsSpectrumPeakList(sp.getPeaksMz(), sp.getPeaksIntensities()));
                spectrum.setMzStart(
                        Collections.min(
                                Arrays.asList( ArrayUtils.toObject(sp.getPeaksMz()) )
                        ).doubleValue()
                );
                spectrum.setMzStop(
                        Collections.max(
                                Arrays.asList( ArrayUtils.toObject(sp.getPeaksMz()) )
                        ).doubleValue()
                );
            }
        }

        if (spectrum==null) logger.debug("Found NO Spectrum!");
        return spectrum;
    }

    /**
     * Highjack the uniqueness flag to assign a positive value if the peptide sequence matches
     * the protein sequence at the specified position and a negative value otherwise.
     * Note: this does not check if the peptide sequence matches the protein sequence at all.
     *       It only checks if it matches were it is supposed to match, e.g. on peptide.getPosition().
     *
     * @param peptides the peptides to match against the protein sequence
     * @param protSeq the protein sequence to match the peptides against
     */
    private void adjustPeptideProteinMatches(List<PeptideMatch> peptides, String protSeq) {
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

    private static Protein mapProteinIdentifiedToWSProtein(ProteinIdentification foundProtein) {
        Protein resultProtein;
        String sequence;

        resultProtein = new Protein();
        resultProtein.setAccession(foundProtein.getSubmittedAccession());

        if (foundProtein.getSubmittedSequence() != null && foundProtein.getSubmittedSequence().length() < 5)
            sequence = foundProtein.getSubmittedSequence();
        else
            sequence = foundProtein.getInferredSequence();

        if (sequence != null && sequence.length() > 5) {
            resultProtein.setSequence(sequence);
        } else {
            throw new IllegalStateException("No valid protein sequence available for protein: " + foundProtein.getAccession());
        }
        resultProtein.setDescription(foundProtein.getName());

        for (ModificationProvider mod : foundProtein.getModifications()) {
            // we ignore any modifications that do not have a main location
            if (mod.getMainPosition() != null) {
                ModifiedLocation protMod = new ModifiedLocation(mod.getName() + " (" + mod.getAccession() + ")", mod.getMainPosition());
                resultProtein.getModifiedLocations().add(protMod);
            }
        }

        return resultProtein;
    }

    private static <T extends Peptide> T mapPsm2WSPeptide(Psm psm, Class<T> clazz) {
        if (psm == null) { return null; }

        T mappedObject;
        try {
            mappedObject = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate object for class: " + clazz.getName());
        }

        mappedObject.setSequence(psm.getPeptideSequence());
        mappedObject.setSymbolic(false); // symbolic does not apply to Archive data
        mappedObject.setTaxonID(-1); // we don't have the species information, so we set a default value
        if (psm.getModifications() != null) {
            mappedObject.setModifiedLocations(mapPsmModifications2WSPeptideModifiedLocations(psm));
        }
        if (psm.getAssayAccession() != null) {
            mappedObject.getAssays().add(psm.getAssayAccession());
        }

        // handle differences between the base Peptide and a PeptideMatch
        // we assign different IDs for Peptides and PeptideMatches
        // we assign additional values to PeptideMatches
        if (mappedObject instanceof PeptideMatch) {
            ((PeptideMatch) mappedObject).setPosition(psm.getStartPosition() == null ? -1 : psm.getStartPosition());
            ((PeptideMatch) mappedObject).setUniqueness(-1); // default value that should be overwritten for matching peptides
            // create and assign a unique ID for the PeptideMatch
            mappedObject.setId(psm.getAssayAccession() + "__" + psm.getProteinAccession() + "__" + psm.getPeptideSequence());
        } else {
            // create and assign a unique ID for the Peptide (PSM)
            mappedObject.setId(psm.getAssayAccession() + "__" + psm.getProteinAccession() + "__" + psm.getPeptideSequence() + "__" + psm.getReportedId());
        }

        return mappedObject;
    }
    private static List<ModifiedLocation> mapPsmModifications2WSPeptideModifiedLocations(Psm psm) {
        if (psm == null || psm.getModifications() == null) {
            return null;
        }
        List<ModifiedLocation> modifiedLocations = new ArrayList<ModifiedLocation>(0);
        for (ModificationProvider mod : psm.getModifications()) {
            if (mod.getMainPosition() == null || mod.getMainPosition() < 0) {
                // we ignore modifications that don't specify a main location
                continue;
            }
            // we ignore neutral loss annotations if there is a main modification accession
            // in case there is no main modification, but there is a neutral loss annotation
            // we add the neutral loss as main modification for the given position
            ModifiedLocation loc;
            if (mod.getAccession() == null) {
                loc = new ModifiedLocation(NEUTRAL_LOSS, mod.getMainPosition());
            } else {
                loc = new ModifiedLocation(mod.getName() + " (" + mod.getAccession() + ")", mod.getMainPosition());
            }
                modifiedLocations.add( loc );
        }
        return modifiedLocations;
    }

    // This method is similar to the method mapPsms2WSPeptides mapping Psm objects to Peptide objects,
    // with two major differences:
    // a) it collapses peptides with the same sequence and start position and
    // b) it will remove modifications on C/N-terminal.
    private static <T extends Peptide> List<T> mapPsms2WSPeptidesFiltered(List<Psm> psms, Class<T> clazz) {
        Map<String, T> peptideMatchMap = new HashMap<String, T>();

        // we have to combine PSMs to peptides that are unique by their sequence + start position
        // include start position in ID to distinguish peptides with the same sequence,
        // but mapped to different locations on the protein sequence
        T mappedObject;
        for (Psm psm : psms) {
            String peptideMatchId = psm.getPeptideSequence() + ":" + psm.getStartPosition();
            // create a new record one does not already exist
            if ( !peptideMatchMap.containsKey(peptideMatchId) ) {
                T pep = mapPsm2WSPeptide(psm, clazz);
                // clear all modifications, as we add them new skipping terminal modifications
                pep.getModifiedLocations().clear();
                peptideMatchMap.put(peptideMatchId, pep);
            }

            // add cumulative data (for now: modifications) to the record
            mappedObject = peptideMatchMap.get(peptideMatchId);
            if (psm.getModifications() != null) {
                // add the ModificationLodationS, the Peptide will take care of removing duplicated ones
                try {
                    // add modifications skipping terminal modifications
                    for (ModifiedLocation mod : mapPsmModifications2WSPeptideModifiedLocations(psm)) {
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

        return new ArrayList<T>(peptideMatchMap.values());
    }
    private static <T extends Peptide> List<T> mapPsms2WSPeptides(List<Psm> psms, Class<T> clazz) {
        List<T> mappedObjects = new ArrayList<T>(psms.size());
        for (Psm psm : psms) {
            T mappedObject = mapPsm2WSPeptide(psm, clazz);
            if (psm.getModifications() != null) {
                mappedObject.setModifiedLocations(mapPsmModifications2WSPeptideModifiedLocations(psm));
            }
            mappedObjects.add(mappedObject);
        }
        return mappedObjects;
    }

//    protected static void inferProteinModifications(Protein protein) {
//        // if there are no peptides to possibly infer modifications from, there is nothing to do!
//        if (protein.getPeptides() == null) {
//            return;
//        }
//        // if there is no protein sequence or it seems invalid, we can't proceed with the inference
//        if (protein.getSequence() == null || protein.getSequence().length() < 5) {
//            throw new IllegalArgumentException("Can not infer modifications for protein with invalid sequence: " + protein.getAccession());
//        }
//        if (protein.getPeptides() == null || protein.getPeptides().isEmpty()) {
//            return;
//        }
//
//        // ToDo: aggregate duplicated ModifiedLocationS (i.e. due to multiple peptides)
//        // ToDo: take inconsistent match cases into account! For example:
//        //      peptide sequence does not match protein sequence
//        //      peptide sequence does match the protein sequence, but not on the reported location
//        for (PeptideMatch peptideMatch : protein.getPeptides()) {
//            // if there are no modifications, we move on...
//            if (peptideMatch.getModifiedLocations() == null || peptideMatch.getModifiedLocations().isEmpty()) {
//                continue;
//            }
//            int startPos = protein.getSequence().indexOf(peptideMatch.getSequence());
//            while (startPos != -1) {
//                for (ModifiedLocation modifiedLocation : peptideMatch.getModifiedLocations()) {
//                    // ToDo: handle n-terminal modification (if startPos == 0, e.g. peptide matches at the beginning of the protein)
//                    // excludes n-terminal modification
//                    // ToD; check for c-terminal modifications (they should only be mapped on the end of the protein!)
//                    if (modifiedLocation.getPosition() > 0) {
//                        ModifiedLocation protMod = new ModifiedLocation();
//                        protMod.setPosition(startPos + modifiedLocation.getPosition());
//                        protMod.setModification(modifiedLocation.getModification());
//                        protein.getModifiedLocations().add(protMod);
//                    } // ToDo: what to do in cases where position <= 0 ?
//                }
//                // check if the peptide matches the protein on another location
//                startPos = protein.getSequence().indexOf(peptideMatch.getSequence(), startPos + 1);
//            }
//        }
//    }

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
