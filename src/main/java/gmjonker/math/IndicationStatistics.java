package gmjonker.math;

import gmjonker.util.LambdaLogger;

import java.util.Collection;
import java.util.List;

import static gmjonker.math.GeneralMath.sqrt;
import static gmjonker.math.GeneralMath.weightedStandardDeviation;

/**
 * See also IndicationCovariance.
 */
public class IndicationStatistics
{
    private static final LambdaLogger log = new LambdaLogger(IndicationStatistics.class);
    
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
    public static double euclideanDistance(List<Indication> indications1, List<Indication> indications2)
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
}
