package gmjonker.math;

import com.google.common.primitives.Doubles;
import gmjonker.util.LambdaLogger;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static gmjonker.math.GeneralMath.abs;
import static gmjonker.math.GeneralMath.mean;
import static gmjonker.math.GeneralMath.pow;
import static gmjonker.util.CollectionsUtil.map;
import static org.apache.commons.collections4.IterableUtils.toList;

public class Correlation
{
    private static final LambdaLogger log = new LambdaLogger(Correlation.class);
    
    public static double correlation(List<Double> series1, List<Double> series2)
    {
        double[] d1 = Doubles.toArray(series1);
        double[] d2 = Doubles.toArray(series2);
        return new PearsonsCorrelation().correlation(d1, d2);
    }

    public static <T> double correlation(Collection<T> keys, Function<T, Double> f1, Function<T, Double> f2)
    {
        List<T> keyList = toList(keys);
        List<Double> series1 = map(keyList, f1);
        List<Double> series2 = map(keyList, f2);
        double[] d1 = Doubles.toArray(series1);
        double[] d2 = Doubles.toArray(series2);
        return new PearsonsCorrelation().correlation(d1, d2);
    }

    public static double covariance(List<Double> series1, List<Double> series2)
    {
        double mean1 = mean(series1);
        double mean2 = mean(series2);
        double total = 0;
        for (int i = 0; i < series1.size(); i++) {
            total += (series1.get(i) - mean1) * (series2.get(i) - mean2);
        }
        return total / series1.size();
    }

    /**
     * Covariance that uses 0 as reference point, not mean.
     */
    public static double covariance0(List<Double> series1, List<Double> series2)
    {
        double total = 0;
        for (int i = 0; i < series1.size(); i++)
            total += series1.get(i) * series2.get(i);
        return total / series1.size();
    }
    
    /**
     * Covariance that 
     *  * uses 0 as reference point instead of mean.
     *  * weighs high and low values more than neutral values. So, for instance, (0,0) pairs do not have an effect on the outcome.  
     */
    public static double covariance0inflated(List<Double> series1, List<Double> series2)
    {
        double total = 0;
        double n = 0;
        for (int i = 0; i < series1.size(); i++) {
            double v1 = series1.get(i);
            double v2 = series2.get(i);
            double weight = abs(v1) + abs(v2);
            total += v1 * v2 * weight;
            n += weight;
        }
        return total / n;
    }

    /**
     * What we want for profile correlation:
     * * high-info (anti)matches say something, low-info matches don't
     * * longer profiles are better: correlation is derived from more datapoints, so more confidence. But we can't return confidence
     * here, so we should discard this effect.
     * 
     * Profile correlation: a correlation metric that:
     *  - uses 0 as reference point instead of mean
     *  - considers high and low values (1, -1) to carry more information than neutral values (0)
     *  - emphasizes pairs with high joint information.  So, for instance, (0,0) pairs do not have an effect on the outcome.
     *  - favours more high-info pairs over fewer high-info pairs (DEBATABLE)
     *  - returns 1 only if each pair is either (1,1) or (-1,-1)
     * Values must lie between -1 inclusive and 1 inclusive. 
     */
    public static double profileCorrelation(List<Double> series1, List<Double> series2)
    {
        assert series1.size() == series2.size();
        
        double totalValue = 0;
        double totalWeight = 0;
        for (int i = 0; i < series1.size(); i++) {
            double v1 = series1.get(i);
            double v2 = series2.get(i);
            double info = (abs(v1) + abs(v2)) / 2;
            totalValue += v1 * v2 * info;
            totalWeight += info;
        }
        if (totalWeight == 0)
            return 0;
        
        double weightedCovariance = totalValue / totalWeight;
        
        // Multiply with a factor that is 1 if all pairs where high-info, medium if some where, zero if none where.
        double maxWeight = series1.size();
        double relativeWeight = totalWeight / maxWeight;
        double factor = pow(relativeWeight, .5);

        log.trace("totalValue = {}", totalValue);
        log.trace("totalWeight = {}", totalWeight);
        log.trace("weightedCovariance = {}", weightedCovariance);
        log.trace("maxWeight = {}", maxWeight);
        log.trace("relativeWeight = {}", relativeWeight);
        log.trace("factor = {}", factor);
        
        return weightedCovariance * factor;
    }
}
