package uk.ac.ebi.pride.archive.web.service.model.viewer;

import java.util.Comparator;

/**
 * @author Florian Reisinger
 * @since 1.0.1
 */
public class ModifiedLocation {


    private int position;
    private String modification;

    public ModifiedLocation() {}
    public ModifiedLocation(String modification, int position) {
        this.modification = modification;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getModification() {
        return modification;
    }

    public void setModification(String modification) {
        this.modification = modification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModifiedLocation that = (ModifiedLocation) o;

        if (position != that.position) return false;
        if (modification != null ? !modification.equals(that.modification) : that.modification != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = position;
        result = 31 * result + (modification != null ? modification.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ModifiedLocation[" + position + ":" + modification + ']';
    }


    /**
     * Class to compare ModifiedLocation objects based on their position.
     * If two ModifiedLocation objects have the same position, their Modification String is compared.
     */
    public static class ModifiedLocationPositionComparator implements Comparator<ModifiedLocation> {
        @Override
        public int compare(ModifiedLocation o1, ModifiedLocation o2) {

            // first compare locations
            if (o1.position < o2.position) {
                return -1;
            } else if (o1.position > o2.position) {
                return 1;
            }

            // if on the same location, compare the modification string
            if (o1.modification == null) {
                if (o2.modification == null) {
                    return 0;
                } else {
                    return 1;
                }
            }
            if (o2.modification == null) {
                return -1;
            }

            return o1.modification.compareTo(o2.modification);
        }
    }

}
