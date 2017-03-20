package gmjonker.math;

import gmjonker.util.LambdaLogger;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;

import static gmjonker.math.GeneralMath.min;
import static gmjonker.math.GeneralMath.sqrt;
import static gmjonker.math.GeneralMath.weightedStandardDeviation;

/**
 * See also IndicationCovarianceOnline.
 */
public class IndicationStats
{
    private static final LambdaLogger log = new LambdaLogger(IndicationStats.class);
    
    public static double standardDeviation(Collection<Indication> indications)
    {
        double[] values  = new double[indications.size()];
        double[] weights = new double[indications.size()];
        int i = 0;
        for (Indication indication : indications) {
            values[i]  = indication.value;
            weights[i] = indication.confidence;
            i++;
        }
        return weightedStandardDeviation(values, weights);
    }

    /**
     * For every indication pair:
     * - squares the diff in value
     * - derives the joint confidence
     * - multiply these two
     * Then sums and square-roots the results.
     */
    public static double euclideanDistance1(List<Indication> indications1, List<Indication> indications2)
    {
        double total = 0;
        for (int i = 0; i < indications1.size(); i++) {
            Indication ind1 = indications1.get(i);
            Indication ind2 = indications2.get(i);
            double diff = ind1.value - ind2.value;
            double weight = IndicationMath.combine(ind1, ind2).confidence;
            total += diff * diff * weight;
            log.trace("diff = {}", diff);
            log.trace("weight = {}", weight);
        }
        return sqrt(total);
    }

    /**
     * For every indication pair:
     * - squares the diff in value
     * - take the minimum confidence of the two
     * - multiply these two
     * Then sums and square-roots the results.
     * @return Pair of distance and confidence
     */
    public static Pair<Double, Double> euclideanDistanceWithConfidence(List<Indication> indications1, List<Indication> indications2)
    {
        double total = 0;
        double totalConfidence = 0;
        int n = indications1.size();
        for (int i = 0; i < n; i++) {
            Indication ind1 = indications1.get(i);
            Indication ind2 = indications2.get(i);
            double diff = ind1.value - ind2.value;
            double weight = min(ind1.confidence, ind2.confidence);
            total += diff * diff * weight;
            totalConfidence += weight;
            log.trace("diff = {}", diff);
            log.trace("weight = {}", weight);
        }
        double valueDist = sqrt(total);
        double confidence = totalConfidence / n;
        return Pair.of(valueDist, confidence);
    }
}
