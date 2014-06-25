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

    public static class ModifiedLocationPositionComparator implements Comparator<ModifiedLocation> {
        @Override
        public int compare(ModifiedLocation o1, ModifiedLocation o2) {
            return Integer.compare(o1.getPosition(), o2.getPosition());
        }
    }
}
