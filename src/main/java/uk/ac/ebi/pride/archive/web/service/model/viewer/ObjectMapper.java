package uk.ac.ebi.pride.archive.web.service.model.viewer;

import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.proteinidentificationindex.mongo.search.model.MongoProteinIdentification;
import uk.ac.ebi.pride.psmindex.mongo.search.model.MongoPsm;
import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.utilities.pridemod.model.PTM;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author florian@ebi.ac.uk.
 */
public class ObjectMapper {

    private static final String NEUTRAL_LOSS = "Neutral loss";
    private static final ModReader modReader = ModReader.getInstance();


    public static Protein mapProteinIdentifiedToWSProtein(MongoProteinIdentification mongoProteinIdentification) {
        Protein resultProtein;
        String sequence;
        resultProtein = new Protein();
        resultProtein.setAccession(mongoProteinIdentification.getSubmittedAccession());
        if (mongoProteinIdentification.getSubmittedSequence() != null && mongoProteinIdentification.getSubmittedSequence().length() > 5) {
            sequence = mongoProteinIdentification.getSubmittedSequence();
            resultProtein.setIsInferredSequence(Boolean.FALSE);
        } else {
            sequence = mongoProteinIdentification.getInferredSequence();
            resultProtein.setIsInferredSequence(Boolean.TRUE);
        }
        if (sequence != null && sequence.length() > 5) {
            resultProtein.setSequence(sequence);
        } else {
            throw new IllegalStateException("No valid protein sequence available for protein: " + mongoProteinIdentification.getAccession());
        }
        resultProtein.setDescription(mongoProteinIdentification.getName());
        for (ModificationProvider mod : mongoProteinIdentification.getModifications()) {
            // we ignore any modifications that do not have a main location
            if (mod.getMainPosition() != null) {
                PTM ptm = modReader.getPTMbyAccession(mod.getAccession());
                double mass = 0;
                if(ptm!= null){
                    mass = ptm.getMonoDeltaMass();
                }
                ModifiedLocation protMod = new ModifiedLocation(mod.getName() + " (" + mod.getAccession() + ")", mod.getMainPosition(), mass);
                resultProtein.getModifiedLocations().add(protMod);
            }
        }
        return resultProtein;
    }


    public static List<Peptide> mapPsms2WSPeptides(List<MongoPsm> psms, boolean assignPsmUniqueId) {
        List<Peptide> mappedObjects = new ArrayList<Peptide>(psms.size());
        for (MongoPsm psm : psms) {
            Peptide mappedObject = mapPsm2WSPeptide(psm, assignPsmUniqueId);
            if (psm.getModifications() != null) {
                mappedObject.setModifiedLocations(mapPsmModifications2WSPeptideModifiedLocations(psm.getModifications()));
            }
            mappedObjects.add(mappedObject);
        }
        return mappedObjects;
    }

    public static Peptide mapPsm2WSPeptide(MongoPsm psm, boolean assignPsmUniqueId) {
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
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1); // remove the last ';'
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
            double mass = 0;
            if (mod.getAccession() == null) {
//                TODO look for a better mass for neutral loss
                loc = new ModifiedLocation(NEUTRAL_LOSS, mod.getMainPosition(), mass);
            } else {
                PTM ptm = modReader.getPTMbyAccession(mod.getAccession());
                if(ptm!= null){
                    mass = ptm.getMonoDeltaMass();
                }
                if (mod.getName() == null) {
                    loc = new ModifiedLocation(mod.getAccession(), mod.getMainPosition(), mass);
                } else {
                    loc = new ModifiedLocation(mod.getName() + " (" + mod.getAccession() + ")", mod.getMainPosition(), mass);
                }
            }
            modifiedLocations.add(loc);
        }
        return modifiedLocations;
    }

    public static Spectrum mapIndexSpectrum2WSSpectrum(uk.ac.ebi.pride.spectrumindex.search.model.Spectrum spectrum) {
        if (spectrum == null) {
            return null;
        }
        Spectrum mappedObject = new Spectrum();
        mappedObject.setId(spectrum.getId());
        mappedObject.setPeaks(SpectrumPeak.getAsSpectrumPeakList(spectrum.getPeaksMz(), spectrum.getPeaksIntensities()));
        mappedObject.setPrecursorCharge(spectrum.getPrecursorCharge());
        mappedObject.setPrecursorMz(spectrum.getPrecursorMz());
        mappedObject.setPrecursorIntensity(spectrum.getPrecursorIntensity());

        Arrays.sort(spectrum.getPeaksMz()); // make sure the mz values are sorted
        double minMZ = spectrum.getPeaksMz()[0]; // take the first value as the minimal mz value
        double maxMZ = spectrum.getPeaksMz()[spectrum.getPeaksMz().length-1]; // take the last value as maximal mz value
        mappedObject.setMzStop(maxMZ);
        mappedObject.setMzStart(minMZ);

        return mappedObject;
    }



}
