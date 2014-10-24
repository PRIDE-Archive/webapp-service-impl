package uk.ac.ebi.pride.archive.web.service.model.viewer;

import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.psmindex.search.model.Psm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author florian@ebi.ac.uk.
 */
public class ObjectMapper {

    private static final String NEUTRAL_LOSS = "neutral loss";

    public static Protein mapProteinIdentifiedToWSProtein(ProteinIdentification foundProtein) {
        Protein resultProtein;
        String sequence;

        resultProtein = new Protein();
        resultProtein.setAccession(foundProtein.getSubmittedAccession());

        if (foundProtein.getSubmittedSequence() != null && foundProtein.getSubmittedSequence().length() < 5) {
            sequence = foundProtein.getSubmittedSequence();
            resultProtein.setIsInferredSequence(Boolean.FALSE);
        } else {
            sequence = foundProtein.getInferredSequence();
            resultProtein.setIsInferredSequence(Boolean.TRUE);
        }

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


    public static List<Peptide> mapPsms2WSPeptides(List<Psm> psms, boolean assignPsmUniqueId) {
        List<Peptide> mappedObjects = new ArrayList<Peptide>(psms.size());
        for (Psm psm : psms) {
            Peptide mappedObject = mapPsm2WSPeptide(psm, assignPsmUniqueId);
            if (psm.getModifications() != null) {
                mappedObject.setModifiedLocations(mapPsmModifications2WSPeptideModifiedLocations(psm.getModifications()));
            }
            mappedObjects.add(mappedObject);
        }
        return mappedObjects;
    }

    public static Peptide mapPsm2WSPeptide(Psm psm, boolean assignPsmUniqueId) {
        if (psm == null) { return null; }

        Peptide mappedObject = new Peptide();
        mappedObject.setSequence(psm.getPeptideSequence());
        mappedObject.setSymbolic(false); // symbolic does not apply to Archive data
        mappedObject.setTaxonID(-1); // we don't have the species information, so we set a default value
        StringBuilder sb = new StringBuilder();
        for (CvParamProvider paramProvider : psm.getSearchEngineScores()) {
            sb.append(paramProvider.getName());
            sb.append(":");
            sb.append(paramProvider.getValue());
            sb.append(";");
        }
        mappedObject.setScores(sb.toString());
        if (psm.getModifications() != null) {
            mappedObject.setModifiedLocations(mapPsmModifications2WSPeptideModifiedLocations(psm.getModifications()));
        }
        if (psm.getAssayAccession() != null) {
            mappedObject.getAssays().add(psm.getAssayAccession());
        }

        // handle differences between use cases
        // we assign different IDs for Peptides (PSMs) and PeptideMatches (aggregations)
        if (assignPsmUniqueId) {
            // create and assign a unique ID for the Peptide (PSM)
            // assay, protein and peptide sequence are not unique enough,
            // we have to add an id that makes the ID unique to a specific PSM
            mappedObject.setId(psm.getAssayAccession() + "__" + psm.getProteinAccession() + "__" + psm.getPeptideSequence() + "__" + psm.getReportedId());
        } else {
            // create and assign a unique ID for the PeptideMatch (aggregation of PSMs)
            // assay, protein and peptide sequence are unique enough
            mappedObject.setId(psm.getAssayAccession() + "__" + psm.getProteinAccession() + "__" + psm.getPeptideSequence());
        }

        return mappedObject;
    }
    public static List<ModifiedLocation> mapPsmModifications2WSPeptideModifiedLocations(Iterable<ModificationProvider> psmModifications) {
        if (psmModifications == null) {
            return null;
        }
        List<ModifiedLocation> modifiedLocations = new ArrayList<ModifiedLocation>(0);
        for (ModificationProvider mod : psmModifications) {
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



}
