package uk.ac.ebi.pride.archive.web.service.model.viewer;

import java.util.List;

/**
 * @author Florian Reisinger
 *         Date: 07/05/14
 * @since $version
 */
public class Protein {

    private String id;
    private String accession;
    private int taxonID;
    private String sequence;
    private String description;
    private List<ModifiedLocation> modifiedLocations;
    private List<String> tissues;
    private String coverage;
    private List<List<Integer>> regions;
    private List<PeptideMatch> peptides;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public int getTaxonID() {
        return taxonID;
    }

    public void setTaxonID(int taxonID) {
        this.taxonID = taxonID;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public List<List<Integer>> getRegions() {
        return regions;
    }

    public void setRegions(List<List<Integer>> regions) {
        this.regions = regions;
    }

    public List<PeptideMatch> getPeptides() {
        return peptides;
    }

    public void setPeptides(List<PeptideMatch> peptides) {
        this.peptides = peptides;
    }
}
