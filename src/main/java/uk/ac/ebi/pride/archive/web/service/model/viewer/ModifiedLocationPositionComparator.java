package uk.ac.ebi.pride.archive.web.service.model.viewer;

import java.util.Comparator;

/**
 * Class to compare ModifiedLocation objects based on their position.
 * If two ModifiedLocation objects have the same position, their Modification String is compared.
 */
public class ModifiedLocationPositionComparator implements Comparator<ModifiedLocation> {
    @Override
    public int compare(ModifiedLocation o1, ModifiedLocation o2) {

        // first compare locations
        if (o1.getPosition() < o2.getPosition()) {
            return -1;
        } else if (o1.getPosition() > o2.getPosition()) {
            return 1;
        }

        // if on the same location, compare the modification string
        if (o1.getModification() == null) {
            if (o2.getModification() == null) {
                return 0;
            } else {
                return 1;
            }
        }
        if (o2.getModification() == null) {
            return -1;
        }

        return o1.getModification().compareTo(o2.getModification());
    }
}
