package uk.ac.ebi.pride.archive.web.service.controller.viewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.archive.web.service.model.viewer.*;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.psmindex.search.model.Psm;
import uk.ac.ebi.pride.psmindex.search.service.PsmSearchService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Florian Reisinger
 * @since 0.1
 */
@SuppressWarnings("UnusedDeclaration")
public class ViewerControllerImpl {

    private static final Logger logger = LoggerFactory.getLogger(ViewerControllerImpl.class);

    public ViewerControllerImpl(ProteinIdentificationSearchService proteinIdentificationSearchService, PsmSearchService psmSearchService) {
        this.psmSearchService = psmSearchService;
        this.proteinIdentificationSearchService = proteinIdentificationSearchService;
    }

    protected ViewerControllerImpl() {
    }

    private ProteinIdentificationSearchService proteinIdentificationSearchService;
    private PsmSearchService psmSearchService;

    // ToDo: better to throw custom exceptions?

    public Protein getProteinData(String proteinID) {
        // the webapp requires a unique protein ID, which is generated as: <assay accession>__<protein accession>
        // since this is for internal use only, this could potentially be a PRIDE internal id as well...
        logger.info("Protein " + proteinID + " requested...");

        String assayAccession = getAssayAccessionFromProteinID(proteinID);
        String proteinAccession = getProteinAccessionFromProteinID(proteinID);
        logger.debug("Extracted assay accession " + assayAccession + " and protein accession " + proteinAccession);

        Protein resultProtein;
        // for a specific test case we create a dummy result
        if (proteinID.equals(DummyDataCreator.PROTEIN_1_ID)) {
            resultProtein = DummyDataCreator.createDummyProtein1();
        } else { // for all other cases we try to retrieve the real records
            List<ProteinIdentified> proteins = proteinIdentificationSearchService.findByAccessionAndAssayAccessions(proteinAccession, assayAccession);
            if (proteins == null || proteins.isEmpty()) {
                return null;
            }
            if (proteins.size() > 1) {
                throw new IllegalStateException("Non-unique result for protein query with ID: " + proteinID);
            }

            ProteinIdentified foundProtein = proteins.iterator().next();
            if (foundProtein.getSequence() == null || foundProtein.getSequence().length() < 5) {
                throw new IllegalArgumentException("Illegal protein! No valid sequence present for: " + proteinID);
            }

            // create a new WS Protein record
            resultProtein = mapProteinIdentifiedToWSProtein(foundProtein);
            // set the protein ID to uniquely identify this protein record
            resultProtein.setId(proteinID);

            // now add the peptides to the Protein
            // retrieve all PSMs to generate the information needed for a Protein record
            List<Psm> psms = psmSearchService.findByProteinAccessionAndAssayAccession(proteinAccession, assayAccession);

            // create list of 'peptide' objects collapsing PSMs
//            List<PeptideMatch> peptides = createPeptideMatchesFromPsmList(psms);
            List<PeptideMatch> peptides = mapPsms2WSPeptidesFiltered(psms, PeptideMatch.class);
            // take into account if the peptide sequence matches the protein sequence at the stated location
            adjustPeptideProteinMatches(peptides, resultProtein.getSequence());
            resultProtein.getPeptides().addAll(peptides);

            // infer protein modifications from list of peptides
            inferProteinModifications(resultProtein);

             // ToDo: we don't know the species! Could be provided by search? or remove completely?
            resultProtein.setTaxonID(-1);

            // ToDo: we have no tissue information! get tissue(s) from protein/assay level? or remove completely?
        }

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
            // and then filter out the peptides we are not interested in
            List<Psm> filteredPsms = new ArrayList<Psm>();
            for (Psm psm : psms) {
                if (psm.getPeptideSequence().equalsIgnoreCase(psmSequence)) {
                    filteredPsms.add(psm);
                }
            }

            result = mapPsms2WSPeptides(filteredPsms, Peptide.class);

            if (result == null) {
                throw new IllegalStateException("No PSMs found for id: " + peptideID);
            }
        }

        return new PeptideList(result);
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

    private static Protein mapProteinIdentifiedToWSProtein(ProteinIdentified foundProtein) {
        Protein resultProtein;
        resultProtein = new Protein();
        resultProtein.setAccession(foundProtein.getAccession());
        String sequence = foundProtein.getSequence();
        if (sequence != null && sequence.length() > 5) {
            resultProtein.setSequence(sequence);
        } else {
            throw new IllegalStateException("No valid protein sequence available for protein: " + foundProtein.getAccession());
        }
        resultProtein.setDescription(foundProtein.getName());
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
            ModifiedLocation loc = new ModifiedLocation(mod.getAccession(), mod.getMainPosition());
            // we ignore peptide terminal modifications
            if (loc.getPosition() > 0 && loc.getPosition() < psm.getPeptideSequence().length()+1) {
                modifiedLocations.add( loc );
            }
        }
        return modifiedLocations;
    }
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
                peptideMatchMap.put(peptideMatchId, mapPsm2WSPeptide(psm, clazz));
            }

            // add cumulative data (for now: modifications) to the record
            mappedObject = peptideMatchMap.get(peptideMatchId);
            if (psm.getModifications() != null) {
                // add the ModificationLodationS, the Peptide will take care of removing duplicated ones
                mappedObject.getModifiedLocations().addAll(mapPsmModifications2WSPeptideModifiedLocations(psm));
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

    protected static void inferProteinModifications(Protein protein) {
        // if there are no peptides to possibly infer modifications from, there is nothing to do!
        if (protein.getPeptides() == null) {
            return;
        }
        // if there is no protein sequence or it seems invalid, we can't proceed with the inference
        if (protein.getSequence() == null || protein.getSequence().length() < 5) {
            throw new IllegalArgumentException("Can not infer modifications for protein with invalid sequence: " + protein.getAccession());
        }
        if (protein.getPeptides() == null || protein.getPeptides().isEmpty()) {
            return;
        }

        // ToDo: aggregate duplicated ModifiedLocationS (i.e. due to multiple peptides)
        // ToDo: take inconsistent match cases into account! For example:
        //      peptide sequence does not match protein sequence
        //      peptide sequence does match the protein sequence, but not on the reported location
        for (PeptideMatch peptideMatch : protein.getPeptides()) {
            // if there are no modifications, we move on...
            if (peptideMatch.getModifiedLocations() == null || peptideMatch.getModifiedLocations().isEmpty()) {
                continue;
            }
            int startPos = protein.getSequence().indexOf(peptideMatch.getSequence());
            while (startPos != -1) {
                for (ModifiedLocation modifiedLocation : peptideMatch.getModifiedLocations()) {
                    // ToDo: handle n-terminal modification (if startPos == 0, e.g. peptide matches at the beginning of the protein)
                    // excludes n-terminal modification
                    // ToD; check for c-terminal modifications (they should only be mapped on the end of the protein!)
                    if (modifiedLocation.getPosition() > 0) {
                        ModifiedLocation protMod = new ModifiedLocation();
                        protMod.setPosition(startPos + modifiedLocation.getPosition());
                        protMod.setModification(modifiedLocation.getModification());
                        protein.getModifiedLocations().add(protMod);
                    } // ToDo: what to do in cases where position <= 0 ?
                }
                // check if the peptide matches the protein on another location
                startPos = protein.getSequence().indexOf(peptideMatch.getSequence(), startPos + 1);
            }
        }
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
