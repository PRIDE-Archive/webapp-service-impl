package uk.ac.ebi.pride.archive.web.service.model.viewer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jose A. Dianes
 * @since 0.1.3
 *
 */
@SuppressWarnings("unused")
public class Spectrum {

    private String id;
    private double mzStart;
    private double mzStop;
    private List<SpectrumPeak> peaks = new ArrayList<SpectrumPeak>();

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

    public List<SpectrumPeak> getPeaks() {
        return peaks;
    }

    public void setPeaks(List<SpectrumPeak> peaks) {
        this.peaks = peaks;
    }

}
