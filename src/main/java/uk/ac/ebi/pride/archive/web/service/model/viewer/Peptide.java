package uk.ac.ebi.pride.archive.web.service.model.viewer;

import java.util.List;

/**
 * @author Florian Reisinger
 *         Date: 07/05/14
 * @since $version
 */
public class Peptide {

    // corresponds to a 'Peptiform/PeptideVariance' in Proteomes

    private String id;
    private boolean symbolic;
    private String sequence;
    private int taxonID;
    private List<ModifiedLocation> modifiedLocations;
    private List<String> tissues;
    private List<String> assays;

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

    public List<ModifiedLocation> getModifiedLocations() {
        return modifiedLocations;
    }

    public void setModifiedLocations(List<ModifiedLocation> modifiedLocations) {
        this.modifiedLocations = modifiedLocations;
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
}
