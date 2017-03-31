package gmjonker.math;

import gmjonker.util.LambdaLogger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class IndicationCorrelationOnline
{
    @Getter private List<Indication> series1 = new ArrayList<>();
    @Getter private List<Indication> series2 = new ArrayList<>();

    private static final LambdaLogger log = new LambdaLogger(IndicationCorrelationOnline.class);
    
    public void addDataPoint(Indication indication1, Indication indication2)
    {
        series1.add(indication1);
        series2.add(indication2);
    }

    public double getCovariance0Simpleton()
    {
        assert series1.size() == series2.size();
        double total = 0;
        for (int i = 0; i < series1.size(); i++) {
            Indication indication1 = series1.get(i);
            Indication indication2 = series2.get(i);
            // TODO: use average instead of 0 as reference point? Then handle the case of series.size() == 1
            total += indication1.deriveDouble() * indication2.deriveDouble();
        }
        return total / series1.size();
    }

    public double getCovariance0()
    {
        assert series1.size() == series2.size();
        double total = 0;
        double n = 0;
        for (int i = 0; i < series1.size(); i++) {
            Indication indication1 = series1.get(i);
            Indication indication2 = series2.get(i);
            double jointConfidence = indication1.confidence * indication2.confidence;
            total += indication1.value * indication2.value * jointConfidence;
            n += jointConfidence;
        }
        return total / n;
    }

    // Not doing this, because determining resulting confidence should be up to the caller
    //    /**
//     * @return Indication where confidence is only based on the total sum of confidences in the series (the more high-confidence
//     * indications there are, the more confidence we have in the covariance).
//     * 
//     */
//    public Indication getCovariance0AsIndication()
//    {
//        assert series1.size() == series2.size();
//        
//        int numPairs = series1.size();
//        double cumValue = 0;
//        double cumJointConfidence = 0;
//        double cumSumConfidence = 0;
//        for (int i = 0; i < numPairs; i++) {
//            val indication1 = series1.get(i);
//            val indication2 = series2.get(i);
//            double jointConfidence = indication1.confidence * indication2.confidence;
//            cumValue += indication1.value * indication2.value * jointConfidence;
//            cumJointConfidence += jointConfidence;
////            cumSumConfidence += indication1.confidence + indication2.confidence;
//        }
//        double finalConfidence = SigmoidMath.toMinusOneOneInterval(cumJointConfidence, .1);
//        return new Indication(cumValue / cumJointConfidence, finalConfidence);
//    }
    
    public double getPearsonSimilarity()
    {
        double cov = getCovariance0();
        double sd1 = IndicationStats.standardDeviation(series1);
        double sd2 = IndicationStats.standardDeviation(series2);
        log.trace("cov = {}", cov);
        log.trace("sd1 = {}", sd1);
        log.trace("sd2 = {}", sd2);
        return cov / (sd1 * sd2);
    }
    
    public long getN()
    {
        return series1.size();
    }

    @Override
    public String toString()
    {
        return "Cov{" +
                "s1=" + series1 +
                ", s2=" + series2 +
                '}';
    }
}
