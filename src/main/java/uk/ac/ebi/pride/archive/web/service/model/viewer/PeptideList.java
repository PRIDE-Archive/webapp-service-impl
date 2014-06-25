package uk.ac.ebi.pride.archive.web.service.model.viewer;

import java.util.List;

/**
 * @author Florian Reisinger
 *         Date: 07/05/14
 * @since $version
 */
public class PeptideList {

    private List<Peptide> peptideList;

    public PeptideList(List<Peptide> peptideList) {
        this.peptideList = peptideList;
    }

    public List<Peptide> getPeptideList() {
        return peptideList;
    }

    public void setPeptideList(List<Peptide> peptideList) {
        this.peptideList = peptideList;
    }
}
