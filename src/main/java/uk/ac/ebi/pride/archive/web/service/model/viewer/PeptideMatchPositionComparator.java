package uk.ac.ebi.pride.archive.web.service.model.viewer;

import java.util.Comparator;
import java.util.Iterator;

/**
 * @author florian@ebi.ac.uk
 * @since 0.1.2
 */
public class PeptideMatchPositionComparator implements Comparator<PeptideMatch> {
    @Override
    public int compare(PeptideMatch o1, PeptideMatch o2) {
        // we have to take care of the special case if both PeptideMatches have positions < 1,
        // e.g. they have no mapped position on the protein
        if (o1.getPosition() > 0 || o2.getPosition() > 0) {
            // if at least one PeptideMatch is mapped, we can compare using the position
            if (o1.getPosition() < o2.getPosition()) {
                return -1;
            } else if (o1.getPosition() > o2.getPosition()) {
                return 1;
            }
        }

        // if on the same location or not matched, compare the peptide sequence string
        if (o1.getSequence() == null) {
            if (o2.getSequence() == null) {
                return 0;
            } else {
                return 1;
            }
        }
        if (o2.getSequence() == null) {
            return -1;
        }
        int aux = o1.getSequence().compareTo(o2.getSequence());
        if (aux != 0) {
            return aux;
        }

        // same sequence, same or unmatched positions
        // we have to go deeper and compare the modifications of the peptide
        aux = Integer.compare(o1.getModifiedLocations().size(), o2.getModifiedLocations().size());
        if (aux != 0) {
            return aux;
        }
        // both have the same number of modifications, but could be 0
        if (o1.getModifiedLocations().size() == 0) {
            return 0;
        }
        // if the peptides also carry the same number of modifications,
        // we have to check them individually
        Iterator<ModifiedLocation> iter_o1 = o1.getModifiedLocations().iterator();
        Iterator<ModifiedLocation> iter_o2 = o2.getModifiedLocations().iterator();
        ModifiedLocationPositionComparator comparator = new ModifiedLocationPositionComparator();
        aux = comparator.compare( iter_o1.next(), iter_o2.next() );
        while (aux == 0 && iter_o1.hasNext()) {
            aux = comparator.compare( iter_o1.next(), iter_o2.next() );
        }

        return aux;
    }

}
