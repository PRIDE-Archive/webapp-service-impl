package uk.ac.ebi.pride.archive.web.service.model.viewer;

import java.util.*;

/**
 * @author Florian Reisinger
 *         Date: 07/05/14
 * @since 0.1
 */
@SuppressWarnings("unused")
public class Peptide {

    // corresponds to a 'Peptiform/PeptideVariance' in Proteomes

    private String id;
    private boolean symbolic;
    private String sequence;
    private int taxonID;
    private Set<ModifiedLocation> modifiedLocations = new TreeSet<ModifiedLocation>(new ModifiedLocationPositionComparator());
    private List<String> tissues = new ArrayList<String>(0);
    private List<String> assays = new ArrayList<String>(1);
    private String scores;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSymbolic() {
        return symbolic;
    }

    public void setSymbolic(boolean symbolic) {
        this.symbolic = symbolic;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public int getTaxonID() {
        return taxonID;
    }

    public void setTaxonID(int taxonID) {
        this.taxonID = taxonID;
    }

    public Set<ModifiedLocation> getModifiedLocations() {
        return modifiedLocations;
    }

    public void setModifiedLocations(Collection<ModifiedLocation> modifiedLocations) {
        this.modifiedLocations.clear();
        this.modifiedLocations.addAll(modifiedLocations);
    }

    public List<String> getTissues() {
        return tissues;
    }

    public void setTissues(List<String> tissues) {
        this.tissues = tissues;
    }

    public List<String> getAssays() {
        return assays;
    }

    public void setAssays(List<String> assays) {
        this.assays = assays;
    }

    public String getScores() {
        return scores;
    }

    public void setScores(String scores) {
        this.scores = scores;
    }

}
