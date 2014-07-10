package uk.ac.ebi.pride.archive.web.service.model.viewer;

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
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ModifiedLocation that = (ModifiedLocation) o;

        if (position != that.position) { return false; }
        if (modification != null ? !modification.equals(that.modification) : that.modification != null) {
            return false;
        }

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


}
