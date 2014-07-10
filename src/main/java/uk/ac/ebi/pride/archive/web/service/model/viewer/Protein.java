package uk.ac.ebi.pride.archive.web.service.model.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Florian Reisinger
 * @since 0.1
 */
@SuppressWarnings("unused")
public class Protein {

    private String id;
    private String accession;
    private int taxonID;
    private String sequence;
    private String description;
    private Set<ModifiedLocation> modifiedLocations = new TreeSet<ModifiedLocation>(new ModifiedLocationPositionComparator());
    private List<String> tissues = new ArrayList<String>();
    private String coverage;
    private List<List<Integer>> regions;
    private Set<PeptideMatch> peptides = new TreeSet<PeptideMatch>(new PeptideMatchPositionComparator());

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

    public Set<ModifiedLocation> getModifiedLocations() {
        return modifiedLocations;
    }

    public void setModifiedLocations(List<ModifiedLocation> modifiedLocations) {
        this.modifiedLocations.clear();
        this.modifiedLocations.addAll(modifiedLocations);
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

    public Set<PeptideMatch> getPeptides() {
        return peptides;
    }

    public void setPeptides(List<PeptideMatch> peptides) {
        this.peptides.clear();
        this.peptides.addAll(peptides);
    }
}
