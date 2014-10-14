package uk.ac.ebi.pride.archive.web.service.model.viewer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jose A. Dianes
 * @since 0.1.3
 *
 */
public class SpectrumPeak implements Serializable {

    private double mz;
    private double intensity;

    public double getMz() {
        return mz;
    }

    public void setMz(double mz) {
        this.mz = mz;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public static List<SpectrumPeak> getAsSpectrumPeakList(double[] peaksMz, double[] peaksIntensities) {
        List<SpectrumPeak> res = new ArrayList<SpectrumPeak>();

        for (int i=0; i<peaksMz.length; i++) {
            SpectrumPeak newPeak = new SpectrumPeak();
            newPeak.setMz(peaksMz[i]);
            newPeak.setIntensity(peaksIntensities[i]);
            res.add(newPeak);
        }
        return res;
    }
}
