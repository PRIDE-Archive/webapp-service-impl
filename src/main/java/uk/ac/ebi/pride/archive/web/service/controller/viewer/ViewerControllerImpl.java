package uk.ac.ebi.pride.archive.web.service.controller.viewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private ProteinIdentificationSearchService proteinIdentificationSearchService;
    private PsmSearchService psmSearchService;

    @Autowired
    public ViewerControllerImpl(ProteinIdentificationSearchService proteinSearch, PsmSearchService psmSearch) {
        this.proteinIdentificationSearchService = proteinSearch;
        this.psmSearchService = psmSearch;
    }

    public Protein getProteinData(String proteinID) {
        logger.info("Protein " + proteinID + " requested...");

        String proteinAccession = getProteinAccessionFromProteinID(proteinID);
        String assayAccession = getAssayAccessionFromProteinID(proteinID);

        // for a specific test case we create a dummy result
        Protein resultProtein = new Protein();
        String dummyProteinAccession = DummyDataCreator.assayAccession + "__" + DummyDataCreator.prot1Accession;
        if (proteinID.equals(dummyProteinAccession)) {
            resultProtein = DummyDataCreator.createDummyProtein1();
        } else {
            // ToDo: use method that searches for protein by assay accession
            List<ProteinIdentified> proteins = proteinIdentificationSearchService.findByAccessionAndProjectAccessions(proteinAccession, assayAccession);
            if (proteins == null || proteins.isEmpty()) {
                return null;
            }

            ProteinIdentified protein = proteins.get(0);
            // ToDo: the result should only contain one protein!


            // retrieve all PSMs to generate the peptide list needed for a Protein record
//        List<Psm> psms = psmSearchService.findByProteinAccessionAndAssayAccession(proteinAccession, assayAccession);
            List<Psm> psms = psmSearchService.findByProteinAccessionAndProjectAccession(proteinAccession, assayAccession);

            // create list of 'peptide' objects collapsing PSMs (using their sequence and position in the protein)
            List<PeptideMatch> peptides = createPeptidesFromPsmList(psms);
            resultProtein.setSequence(/*protein.getSequence()*/ "");  // ToDo: to be provided by Search
            resultProtein.setId(proteinID); // convention: the ID of a protein (identification) is the assay accession + double underscore + protein accession
            resultProtein.setAccession(protein.getAccession());
            resultProtein.setDescription(/*protein.getDescription()*/ "");   // ToDo: to be provided by Search
            resultProtein.setPeptides(peptides);
            List<ModifiedLocation> modifiedLocations = new ArrayList<ModifiedLocation>(0);
            resultProtein.setModifiedLocations(modifiedLocations);   // ToDo: to possibly be provided by Search
            resultProtein.setTaxonID(-1); // ToDo: that should also be provided by the Search
            List<String> tissues = new ArrayList<String>(0);
            resultProtein.setTissues(tissues);
        }

        return resultProtein;
    }

    public PeptideList getPSMData(String peptideID) {
        logger.info("PSMs for peptide " + peptideID + " requested...");
        // retrieve the PSMs for a particular peptide sequence of a particular protein

        String psmSequence = getPsmSequenceFromPsmID(peptideID);
        String proteinAccession = getProteinAccessionFromPsmID(peptideID);
        String assayAccession = getAssayAccessionFromPsmID(peptideID);
        logger.debug("Peptide ID decoded into: assay=" + assayAccession + " protein=" + proteinAccession + " seq=" + psmSequence);
//
//
//        // ToDo: check if assay and sequence is enough to retrieve PSMs for only one protein identification!
////        psmSearchService.findByPeptideSequenceAndAssayAccession(psmSequence, assayAccession);
//        // ToDo: alternative retrieve all PSMs for protein + assay and filter (use cache as the same data is used in the above method)
//        List<Psm> psms = psmSearchService.findByProteinAccessionAndProjectAccession(proteinAccession, assayAccession);
//
//        PeptideList result = createPeptideListFromListOfPsms(psms);
//
//        if (result == null) {
//            throw new ResourceNotFoundException("No PSMs found for id: " + peptideID);
//        }

        List<Peptide> allPsms = DummyDataCreator.createDummyPeptideList();
        List<Peptide> filteredPsms = new ArrayList<Peptide>();

        for (Peptide peptide : allPsms) {
            if (peptide.getSequence().equalsIgnoreCase(psmSequence)) {
                filteredPsms.add(peptide);
            }
        }
        PeptideList result = new PeptideList();
        result.setPeptideList(filteredPsms);

        return result;
    }


    private List<PeptideMatch> createPeptidesFromPsmList(List<Psm> psms) {
        Map<String, PeptideMatch> peptideMatchMap = new HashMap<String, PeptideMatch>();

        // we have to combine PSMs to peptides that are unique by their sequence
        PeptideMatch mappedObject;
        for (Psm psm : psms) {
            String peptideMatchId = psm.getPepSequence() + ":" + psm.getStartPosition();
            if (peptideMatchMap.containsKey(peptideMatchId)) {
                // retrieve the already existing peptide record, so additional data can be added (if needed)
                mappedObject = peptideMatchMap.get(peptideMatchId);
            } else {
                // create a new object with all the data shared by all PSMs
                mappedObject = new PeptideMatch();
                mappedObject.setId(psm.getAssayAccession() + "__" + psm.getProteinAccession() + "__" + psm.getPepSequence());// set the peptide id following the convention
                mappedObject.setSequence(psm.getPepSequence());
                mappedObject.setPosition(psm.getStartPosition());
                mappedObject.setUniqueness(-1);
                mappedObject.setSymbolic(false);
                mappedObject.setTaxonID(-1);
                if (mappedObject.getAssays() == null) {
                    List<String> assays = new ArrayList<String>();
                    mappedObject.setAssays(assays);
                }
                mappedObject.getAssays().add(psm.getAssayAccession());

                peptideMatchMap.put(peptideMatchId, mappedObject);
            }
            // add (if needed) accumulative data
            List<String> tissues = new ArrayList<String>(0);
            mappedObject.setTissues(tissues);
            List<ModifiedLocation> modifiedLocations = new ArrayList<ModifiedLocation>(0);
            mappedObject.setModifiedLocations(modifiedLocations);
        }

        return new ArrayList<PeptideMatch>(peptideMatchMap.values());
    }

    private String getAssayAccessionFromProteinID(String proteinID) {
        // convention: <assay accession><double underscore><protein accession>;
        // example   : 13567__P12345
        String[] parts = proteinID.split("__");
        if (parts.length != 2) {
            throw new IllegalArgumentException("A valid protein ID need to have two parts separated by double underscore!");
        }
        return parts[0];
    }
    private String getProteinAccessionFromProteinID(String proteinID) {
        // convention: <assay accession><double underscore><protein accession>;
        // example   : 13567__P12345
        String[] parts = proteinID.split("__");
        if (parts.length != 2) {
            throw new IllegalArgumentException("A valid protein ID need to have two parts separated by double underscore!");
        }
        return parts[1];
    }
    private String getAssayAccessionFromPsmID(String psmID) {
        // convention: <assay accession><double underscore><protein accession><double underscore><psm sequence>;
        // example   : 13567__P12345__NDRQSLNISNK
        String[] parts = psmID.split("__");
        if (parts.length != 3) {
            throw new IllegalArgumentException("A valid psm ID need to have two parts separated by double underscore!");
        }
        return parts[0];
    }
    private String getProteinAccessionFromPsmID(String psmID) {
        // convention: <assay accession><double underscore><protein accession><double underscore><psm sequence>;
        // example   : 13567__P12345__NDRQSLNISNK
        String[] parts = psmID.split("__");
        if (parts.length != 3) {
            throw new IllegalArgumentException("A valid psm ID need to have two parts separated by double underscore!");
        }
        return parts[1];
    }
    private String getPsmSequenceFromPsmID(String psmID) {
        // convention: <assay accession><double underscore><protein accession><double underscore><psm sequence>;
        // example   : 13567__P12345__NDRQSLNISNK
        String[] parts = psmID.split("__");
        if (parts.length != 3) {
            throw new IllegalArgumentException("A valid psm ID need to have two parts separated by double underscore!");
        }
        return parts[2];
    }


}
