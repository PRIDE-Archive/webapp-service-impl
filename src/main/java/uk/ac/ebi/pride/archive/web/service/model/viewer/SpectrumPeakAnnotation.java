package uk.ac.ebi.pride.archive.web.service.model.viewer;

/**
 * @author Jose A. Dianes
 * @since 0.1.3
 *
 */
public class SpectrumPeakAnnotation {
    String group;
    double mz;
    String ionType;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public double getMz() {
        return mz;
    }

    public void setMz(double mz) {
        this.mz = mz;
    }

    public String getIonType() {
        return ionType;
    }

    public void setIonType(String ionType) {
        this.ionType = ionType;
    }
}
