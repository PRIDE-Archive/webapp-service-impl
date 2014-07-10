package uk.ac.ebi.pride.archive.web.service.model.viewer;

/**
 * @author Florian Reisinger
 *         Date: 07/05/14
 * @since $version
 */
@SuppressWarnings("unused")
public class PeptideMatch extends Peptide {

    // corresponds to a 'Peptide' in Proteomes

    private int position;
    private int uniqueness;

    public PeptideMatch() {
        this.position = -1;
        this.uniqueness = -1;
    }

    protected PeptideMatch(int position, String sequence) {
        this.position = position;
        this.setSequence(sequence);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getUniqueness() {
        return uniqueness;
    }

    public void setUniqueness(int uniqueness) {
        this.uniqueness = uniqueness;
    }
}
