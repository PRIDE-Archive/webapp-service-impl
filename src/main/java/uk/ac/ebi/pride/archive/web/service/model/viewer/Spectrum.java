package uk.ac.ebi.pride.archive.web.service.model.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Jose A. Dianes
 * @since 0.1.3
 */
@SuppressWarnings("unused")
public class Spectrum {

    private String id;

    private List<SpectrumPeak> peaks = new ArrayList<SpectrumPeak>();
    private double precursorMz;
    private double precursorIntensity;
    private int precursorCharge;
    private double mzStart;
    private double mzStop;

    public Spectrum() {
    }

    public Spectrum(String id, double mzStart, double mzStop, List<SpectrumPeak> peaks) {
        this.id = id;
        this.mzStart = mzStart;
        this.mzStop = mzStop;
        this.peaks = peaks;
    }

    public String getId() {
        return id;
    }

    public void setId(String spectrumId) {
        this.id = spectrumId;
    }

    public List<SpectrumPeak> getPeaks() {
        return peaks;
    }

    public void setPeaks(List<SpectrumPeak> peaks) {
        this.peaks = peaks;
    }

    public double getPrecursorMz() {
        return precursorMz;
    }

    public void setPrecursorMz(double precursorMz) {
        this.precursorMz = precursorMz;
    }

    public double getPrecursorIntensity() {
        return precursorIntensity;
    }

    public void setPrecursorIntensity(double precursorIntensity) {
        this.precursorIntensity = precursorIntensity;
    }

    public int getPrecursorCharge() {
        return precursorCharge;
    }

    public void setPrecursorCharge(int precursorCharge) {
        this.precursorCharge = precursorCharge;
    }

    public double getMzStart() {
        return mzStart;
    }

    public void setMzStart(double mzStart) {
        this.mzStart = mzStart;
    }

    public double getMzStop() {
        return mzStop;
    }

    public void setMzStop(double mzStop) {
        this.mzStop = mzStop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Spectrum)) return false;
        Spectrum spectrum = (Spectrum) o;
        return Double.compare(spectrum.precursorMz, precursorMz) == 0 &&
                Double.compare(spectrum.precursorIntensity, precursorIntensity) == 0 &&
                precursorCharge == spectrum.precursorCharge &&
                Double.compare(spectrum.mzStart, mzStart) == 0 &&
                Double.compare(spectrum.mzStop, mzStop) == 0 &&
                Objects.equals(id, spectrum.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, precursorMz, precursorIntensity, precursorCharge, mzStart, mzStop);
    }
}
