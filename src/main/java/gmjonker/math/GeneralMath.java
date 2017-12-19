package gmjonker.math;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import gmjonker.util.LambdaLogger;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static gmjonker.math.NaType.NA;
import static gmjonker.math.NaType.NA_I;
import static gmjonker.math.NaType.isValue;
import static gmjonker.util.CollectionsUtil.filterNulls;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * All methods are thread-safe.
 */
@SuppressWarnings("WeakerAccess")
public class GeneralMath
{
    public static final long KILO = 1000;
    public static final long MEGA = 1000000;
    public static final long GIGA = 1000000000;
    
    public static final double MILIS_TO_NANOS = 1000 * 1000;
    public static final double NANOS_TO_MILIS = .001 * .001;
    public static final double SECONDS_TO_NANOS = 1000 * 1000 * 1000;
    public static final double NANOS_TO_SECONDS = .001 * .001 * .001;

    private static final LambdaLogger log = new LambdaLogger(GeneralMath.class);

    public static int sign(double x)
    {
        return (int) Math.signum(x);
    }

    public static double abs(double x)
    {
        return Math.abs(x);
    }

    public static double pow(double x, double exponent)
    {
        return Math.pow(x, exponent);
    }

    public static double powSignSafe(double x, double exponent)
    {
        int sign = sign(x);
        x *= sign;
        double result = Math.pow(x, exponent);
        return result * sign;
    }

    @Deprecated // Clashes with Logger log
    public static double log(double x)
    {
        return Math.log(x);
    }

    public static double ln(double x)
    {
        return Math.log(x);
    }

    public static double log(double base, double x)
    {
        return Math.log(x) / Math.log(base);
    }

    public static double sqrt(double x)
    {
        return Math.sqrt(x);
    }

    public static double sqrtSigned(double x)
    {
        int sign = sign(x);
        return Math.sqrt(x * sign) * sign;
    }

    public static double cbrt(double x)
    {
        return Math.cbrt(x);
    }

    public static long round(double x)
    {
        return Math.round(x);
    }

    public static long floor(double x)
    {
        return (long) Math.floor(x);
    }

    /**
     * @param decimals decimals after the dot.
     */
    public static double round(double value, int decimals)
    {
        if ( ! isValue(value))
            return NA;

        return Math.round(value * pow(10, decimals)) / pow(10, decimals);
    }

    /**
     * @param decimals decimals after the dot.
     */
    public static Iterable<Double> round(Iterable<Double> values, int decimals)
    {
//        return () -> new Iterator<Double>() {
//            @Override
//            public boolean hasNext() {
//                return values.iterator().hasNext();
//            }
//            @Override
//            public Double next() {
//                return round(values.iterator().next(), decimals);
//            }
//        };
        return StreamSupport.stream(values.spliterator(), false)
                .map(x -> round(x, decimals))
                .collect(Collectors.toList());
    }

    public static double max(double x1, double x2)
    {
        return Math.max(x1, x2);
    }

    public static double max(double... values)
    {
        return StatUtils.max(values);
    }

    public static int max(int x1, int x2)
    {
        return Math.max(x1, x2);
    }

    public static int max(int... values)
    {
        return Ints.max(values);
    }

    public static long max(long x1, long x2)
    {
        return Math.max(x1, x2);
    }

    public static long max(long... values)
    {
        return Longs.max(values);
    }

    @Nullable
    public static <T extends Object & Comparable<? super T>> T max(Collection<? extends T> coll)
    {
        if (isEmpty(filterNulls(coll)))
            return null;
        return Collections.max(coll);
    }

    @Nonnull
    public static <T extends Object & Comparable<? super T>> T maxOr(Collection<? extends T> coll, @Nonnull T defaultValue)
    {
        coll = filterNulls(coll);
        if (isEmpty(coll))
            return defaultValue;
        return Collections.max(coll);
    }

    public static <V> double maxBy(Iterable<V> iterable, Function<V, Double> valueExtractor)
    {
        double max = Double.MIN_VALUE;
        for (V v : iterable) {
            double value = valueExtractor.apply(v);
            if (value > max)
                max = value;
        }
        if (max == Double.MIN_VALUE)
            max = NA;
        return max;
    }

    public static <V> int maxByI(Iterable<V> iterable, Function<V, Integer> valueExtractor)
    {
        Integer max = Integer.MIN_VALUE;
        for (V v : iterable) {
            int value = valueExtractor.apply(v);
            if (value > max)
                max = value;
        }
        if (max == Integer.MIN_VALUE)
            max = NA_I;
        return max;
    }

    public static double min(double x1, double x2)
    {
        return Math.min(x1, x2);
    }

    public static double min(double... values)
    {
        return StatUtils.min(values);
    }

    public static int min(int x1, int x2)
    {
        return Math.min(x1, x2);
    }

    public static int min(int... values)
    {
        return Ints.min(values);
    }

    @Nonnull
    public static <T extends Object & Comparable<? super T>> T minOr(Collection<? extends T> coll, @Nonnull T defaultValue)
    {
        coll = filterNulls(coll);
        if (isEmpty(coll))
            return defaultValue;
        return Collections.min(coll);
    }

    public static <V> double minBy(Iterable<V> iterable, Function<V, Double> valueExtractor)
    {
        double min = Double.MAX_VALUE;
        for (V v : iterable) {
            double value = valueExtractor.apply(v);
            if (value < min)
                min = value;
        }
        if (min == Double.MAX_VALUE)
            min = NA;
        return min;
    }

    public static <V> int minByI(Iterable<V> iterable, Function<V, Integer> valueExtractor)
    {
        Integer min = Integer.MAX_VALUE;
        for (V v : iterable) {
            int value = valueExtractor.apply(v);
            if (value < min)
                min = value;
        }
        if (min == Integer.MAX_VALUE)
            min = NA_I;
        return min;
    }

    /** Returns max(min(x, max), min). **/
    public static double limit(double x, double min, double max)
    {
        return max(min(x, max), min);
    }

    public static double sum(double... values)
    {
        return StatUtils.sum(values);
    }

    public static double sum(Collection<Double> values)
    {
        double sum = NA;
        if ( ! isEmpty(values) ) {
            sum = 0.0;
            for (Double value : values)
                sum += value;
        }
        return sum;
    }

    public static int sum_i(Collection<Integer> values)
    {
        int sum = NA_I;
        if ( ! isEmpty(values) ) {
            sum = 0;
            for (Integer value : values)
                sum += value;
        }
        return sum;
    }

    public static double sumOr(Collection<Double> values, double defaultValue)
    {
        if (isEmpty(values) ) 
            return defaultValue;
        double sum = 0.0;
        for (Double value : values)
            sum += value;
        return sum;
    }

    public static <T> int sum_i(Collection<T> coll, Function<T,Integer> mapper)
    {
        int sum = 0;
        for (T t : coll)
            sum += mapper.apply(t);
        return sum;
    }

    public static <T> double sumBy(Collection<T> coll, Function<T, Double> mapper)
    {
        double sum = 0.0;
        for (T el : coll)
            sum += mapper.apply(el);
        return sum;
    }
    
    public static <T> int sumBy_I(Collection<T> coll, Function<T, Integer> mapper)
    {
        int sum = 0;
        for (T el : coll)
            sum += mapper.apply(el);
        return sum;
    }
    
    public static <T> double sumByOr(Collection<T> coll, Function<T, Double> mapper, double defaultValue)
    {
        if (isEmpty(coll) )
            return defaultValue;
        double sum = 0.0;
        for (T el : coll)
            sum += mapper.apply(el);
        return sum;
    }
    
    public static double mean(double... values)
    {
        if (values.length == 0)
            return NA;
        return StatUtils.mean(values);
    }

    public static double mean(Collection<Double> values)
    {
        if (isEmpty(values))
            return NA;
        double sum = 0;
        for (Double value : values)
            sum += value;
        return sum / values.size();
    }

    /**
     * @param values values
     * @param weights Positive (or negative) infinity is allowed (replaced for a very large (or small) number)
     * @return weighted mean
     */
    public static double weightedMean(double[] values, double[] weights)
    {
        double sum = 0;
        double totalWeight = 0;
        for (int i = 0; i < values.length; i++) {
            double value = values[i];
            double weight = weights[i];
            if (weight == Double.POSITIVE_INFINITY)
                weight = Double.MAX_VALUE / 1000;
            else if (weight == Double.NEGATIVE_INFINITY)
                weight = Double.MIN_VALUE * 1000;
            sum += weight * value;
            totalWeight += weight;
        }
        if (totalWeight < 0) {
            log.error("sum(weights) must be positive");
            return NA;
        }
        return sum / totalWeight;
    }

    public static double weightedMean(Collection<Pair<Double, Double>> valueWeightPairs)
    {
        double sum = 0;
        double totalWeight = 0;
        for (Pair<Double, Double> valueWeightPair : valueWeightPairs) {
            double value  = valueWeightPair.getLeft();
            double weight = valueWeightPair.getRight();
            if (weight == Double.POSITIVE_INFINITY)
                weight = Double.MAX_VALUE / 1000;
            else if (weight == Double.NEGATIVE_INFINITY)
                weight = Double.MIN_VALUE * 1000;
            sum += weight * value;
            totalWeight += weight;
        }
        if (totalWeight < 0) {
            log.error("sum(weights) must be positive");
            return NA;
        }
        return sum / totalWeight;
    }

    /**
     * NA values or values with NA weights are ignored.
     * @param values values
     * @param weights Positive (or negative) infinity is allowed (replaced for a very large (or small) number)
     * @return weighted mean
     */
    public static double weightedMeanIgnoreNAs(double[] values, double[] weights)
    {
        double sum = 0;
        double totalWeight = 0;
        for (int i = 0; i < values.length; i++) {
            double value = values[i];
            double weight = weights[i];
            if ( ! isValue(value) || ! isValue(weight))
                continue;
            if (weight == Double.POSITIVE_INFINITY)
                weight = Double.MAX_VALUE / 1000;
            else if (weight == Double.NEGATIVE_INFINITY)
                weight = Double.MIN_VALUE * 1000;
            sum += weight * value;
            totalWeight += weight;
        }
        if (totalWeight < 0) {
            log.error("sum(weights) must be positive");
            return NA;
        }
        return sum / totalWeight;
    }

    /**
     * Calculates a weighted mean of values. This is a weighted sum divided by sum(weights).
     * If any value is Constants.NA, then the default value is used instead.
     * The weights must be non-negative, and sum(weights) must be positive.
     * @param values values
     * @param weights Positive (or negative) infinity is allowed (replaced for a very large (or small) number)
     * @param include use this to exclude values completely, i.e. don't use any popularity values
     * @param defaultValues these values will be used if individual values are missing (NA)
     */
    public static double weightedMeanWithDefaults(double[] values, double[] weights, boolean[] include,
            double[] defaultValues)
    {
        double sum = 0;
        double totalWeight = 0;
        for (int i = 0; i < values.length; i++) {
            if (!include[i])
                continue;
            double value = values[i];
            double weight = weights[i];
            if (weight == Double.POSITIVE_INFINITY)
                weight = Double.MAX_VALUE / 1000;
            else if (weight == Double.NEGATIVE_INFINITY)
                weight = Double.MIN_VALUE * 1000;
            if (isValue(value))
                sum += weight * value;
            else
                sum += weight * defaultValues[i];
            totalWeight += weight;
        }
        if (totalWeight < 0) {
            log.error("sum(weights) must be positive");
            return NA;
        }
        return sum / totalWeight;
    }

    public static double weightedMeanWithDefaults(double[] values, double[] weights, double[] defaultValues)
    {
        boolean[] include = new boolean[values.length];
        Arrays.fill(include, true);
        return weightedMeanWithDefaults(values, weights, include, defaultValues);
    }

    /* Streams are cool and all, but not performance-wise. Kept for reference. */
    @Deprecated
    public static double meanAbsoluteErrorStreamed(List<Double> values)
    {
        return values.stream()
                .mapToDouble(v -> Math.abs(1 - v))
                .average()
                .orElseThrow(() -> new RuntimeException("No values supplied"));
    }

    public static double meanAbsoluteError(double[] values)
    {
        double temp = 0;
        for (double value : values) {
            temp += (Math.abs(1 - value));
        }
        return temp / values.length;
    }

    public static double meanDeviation(Collection<Double> values)
    {
        double mean = mean(values);
        double temp = 0;
        for (double value : values) {
            temp += abs(mean - value);
        }
        return temp / values.size();
    }

    public static double meanDeviationFrom(Collection<Double> values, double x)
    {
        double temp = 0;
        for (double value : values) {
            temp += abs(x - value);
        }
        return temp / values.size();
    }

    public static double weightedMeanAbsoluteError(double[] values, double[] weights)
    {
        double temp = 0;
        double weightsSum = 0;
        for (int i = 0; i < values.length; i++) {
            temp += weights[i] * (Math.abs(1 - values[i]));
            weightsSum += weights[i];
        }
        return temp / weightsSum;
    }

    public static double powerMean(double[] values, double exponent)
    {
        double temp = 0;
        for (double value : values) {
            temp += Math.pow(value, exponent);
        }
        return Math.pow(temp / values.length, 1.0 / exponent);
    }

    public static double powerMean(Collection<Double> values, double exponent)
    {
        double temp = 0;
        for (double value : values) {
            temp += Math.pow(value, exponent);
        }
        return Math.pow(temp / values.size(), 1.0 / exponent);
    }

    public static double rootMeanSquare(double[] values)
    {
        return powerMean(values, 2);
    }

    public static double rootMeanSquare(Collection<Double> values)
    {
        return powerMean(values, 2);
    }

    public static double rootWeightedMeanSquare(List<Double> values, List<Double> weights)
    {
        double temp = 0;
        double weightsSum = 0;
        for (int i = 0; i < values.size(); i++) {
            temp += weights.get(i) * Math.pow(values.get(i), 2);
            weightsSum += weights.get(i);
        }
        return Math.sqrt(temp / weightsSum);
    }

    public static double rootWeightedMeanSquare(Collection<Pair<Double, Double>> valueWeightPairs)
    {
        double total = 0;
        double weightsSum = 0;
        for (Pair<Double, Double> valueWeightPair : valueWeightPairs) {
            Double value = valueWeightPair.getLeft();
            Double weight = valueWeightPair.getRight();
            if (weight == 0) // for case value = inf, weight = 0
                continue;
            total += weight * Math.pow(value, 2);
            weightsSum += weight;
        }
        return Math.sqrt(total / weightsSum);
    }

    public static double rootWeightedMeanSquareNegSafe(Collection<Pair<Double, Double>> valueWeightPairs)
    {
        double total = 0;
        double weightsSum = 0;
        for (Pair<Double, Double> valueWeightPair : valueWeightPairs) {
            Double value = valueWeightPair.getLeft();
            Double weight = valueWeightPair.getRight();
            if (weight == 0) // for case value = inf, weight = 0
                continue;
            total += weight * Math.pow(value, 2) * sign(value); // retain sign of value
            weightsSum += weight;
        }
        int sign = sign(total);
        return Math.sqrt(sign * total / weightsSum) * sign;
    }

    public static double rootMeanSquareError(double[] values)
    {
        double temp = 0;
        for (double value : values) {
            temp += Math.pow(1 - value, 2);
        }
        return Math.sqrt(temp / values.length);
    }

    public static double rootMeanSquareError(Collection<Double> values)
    {
        double temp = 0;
        for (double value : values) {
            temp += Math.pow(1 - value, 2);
        }
        return Math.sqrt(temp / values.size());
    }

    public static double rootWeightedMeanSquareError(double[] values, double[] weights)
    {
        double temp = 0;
        double weightsSum = 0;
        for (int i = 0; i < values.length; i++) {
            temp += weights[i] * Math.pow(1 - values[i], 2);
            weightsSum += weights[i];
        }
        return Math.sqrt(temp / weightsSum);
    }

    public static double rootWeightedMeanSquareError(List<Double> values, List<Double> weights)
    {
        double temp = 0;
        double weightsSum = 0;
        for (int i = 0; i < values.size(); i++) {
            temp += weights.get(i) * Math.pow(1 - values.get(i), 2);
            weightsSum += weights.get(i);
        }
        return Math.sqrt(temp / weightsSum);
    }

    public static double rootWeightedMeanSquareErrorIgnoreNAs(double[] values, double[] weights)
    {
        double temp = 0;
        double weightsSum = 0;
        for (int i = 0; i < values.length; i++) {
            if ( ! isValue(values[i]) || ! isValue(weights[i]))
                continue;
            temp += weights[i] * Math.pow(1 - values[i], 2);
            weightsSum += weights[i];
        }
        return Math.sqrt(temp / weightsSum);
    }

    public static double rootWeightedMeanSquareErrorIgnoreNAs(List<Double> values, List<Double> weights)
    {
        double temp = 0;
        double weightsSum = 0;
        for (int i = 0; i < values.size(); i++) {
            if ( ! isValue(values.get(i)) || ! isValue(weights.get(i)))
                continue;
            temp += weights.get(i) * Math.pow(1 - values.get(i), 2);
            weightsSum += weights.get(i);
        }
        return Math.sqrt(temp / weightsSum);
    }

    public static double harmonicMean(double[] values)
    {
        return powerMean(values, -1);
    }

    public static double f1Measure(double precision, double recall)
    {
        // harmonic mean of precision and recall
        return 2 * (precision * recall) / (precision + recall);
    }

    /**
     * "Two other commonly used F measures are the F_{2} measure, which weights recall higher than precision,
     * and the F_{0.5} measure, which puts more emphasis on precision than recall."
     */
    public static double fBetaMeasure(double precision, double recall, double beta)
    {
        return (1 + beta * beta) * (precision * recall) / (beta * beta * precision + recall);
    }

    /** Population variance. **/
    public static double variance(double... values)
    {
        int length = values.length;
        if (length == 0)
            return NA;
        double mean = mean(values);
        double total = 0;
        for (double value : values) {
            double diff = value - mean;
            total += diff * diff;
        }
        return 1.0 / length * total;
    }

    @SuppressWarnings("ConstantConditions")
    public static double weightedVariance(double[] values, double[] weights)
    {
        boolean isBiasCorrected = false;
        return new Variance(isBiasCorrected).evaluate(values, weights);
    }

    @SuppressWarnings("ConstantConditions")
    public static double standardDeviation(double[] values)
    {
        boolean isBiasCorrected = false;
        return sqrt(new Variance(isBiasCorrected).evaluate(values));
    }

    public static double standardDeviation(Collection<Double> values)
    {
        double[] array = Doubles.toArray(values);
        boolean isBiasCorrected = false;
        return sqrt(new Variance(isBiasCorrected).evaluate(array));
    }

    @SuppressWarnings("ConstantConditions")
    public static double weightedStandardDeviation(double[] values, double[] weights)
    {
        boolean isBiasCorrected = false;
        return sqrt(new Variance(isBiasCorrected).evaluate(values, weights));
    }



    /**
     * Calculates the exponential moving average, with S_1 = Y_1
     * (see http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average)
     * TODO: PERFORMANCE: provide option to skip the first part of values, those values that hardly contribute to the result
     **/
    public static double exponentialMovingAverageV1(double[] values, double alpha)
    {
        if (isEmpty(values))
            return NA;
        double av = values[0];
        for (int i = 1; i < values.length; i++) {
            av += alpha * (values[i] - av);
        }
        return av;
    }

    /**
     * Similar to exponentialMovingAverageV1, but with a zero value prepended to values. This means that it assumes that
     * all values before the first one were zero, whereas exponentialMovingAverageV1 assumes they were all similar to Y_1.
     * TODO: PERFORMANCE: provide option to skip the first part of values, those values that hardly contribute to the result
     **/
    public static double exponentialMovingAverageV2(double[] values, double alpha)
    {
        if (isEmpty(values))
            return NA;
        double av = 0;
        for (double v: values)
            av += alpha * (v - av);
        return av;
    }

    /**
     * Similar to exponentialMovingAverageV2, but with S_1 = average value. This assumes that the values before the first
     * one were equal to those in values on average.
     * TODO: PERFORMANCE: provide option to skip the first part of values, those values that hardly contribute to the result
     **/
    public static double exponentialMovingAverageV3(double[] values, double alpha)
    {
        if (isEmpty(values))
            return NA;
        double av = mean(values);
        for (double v: values)
            av += alpha * (v - av);
        return av;
    }

    /**
     * The one exponentialMovingAverage to rule them all. Supply your own S_1.
     * See OnlineMovingExponentialAverage for an online/incremental variant.
     * TODO: remove others
     * TODO: PERFORMANCE: provide option to skip the first part of values, those values that hardly contribute to the result
     **/
    public static double exponentialMovingAverageV4(double[] values, double seed, double alpha)
    {
        double av = seed;
        for (double v: values)
            av += alpha * (v - av);
        return av;
    }

    public static void printExponentialCoefficients(double alpha, int howMuch)
    {
        double coef = alpha;
        for (int i = 0; i < howMuch; i++) {
            System.out.printf("coef %-2d = %.4f%n", i, coef);
            coef *= (1 - alpha);
        }
    }

    /**
     * This function looks a bit like a non-tight rope between (0,0) and (1,1) within that interval.
     * http://rechneronline.de/function-graphs/
     * a0=2&a1=(1/(-1.5x+2) - .5) / 1.5&a2=&a3=&a4=1&a5=4&a6=8&a7=1&a8=1&a9=1&b0=480&b1=480&b2=-1&b3=2&b4=-1&b5=2&b6=12&b7=12&b8=3&b9=3&c0=3&c1=0&c2=1&c3=1&c4=1&c5=1&c6=1&c7=0&c8=0&c9=0&d0=1&d1=12&d2=12&d3=0&d4=&d5=&d6=&d7=&d8=&d9=&e0=&e1=&e2=&e3=&e4=14&e5=14&e6=13&e7=12&e8=0&e9=0&f0=0&f1=1&f2=1&f3=0&f4=0&f5=&f6=&f7=&f8=&f9=&g0=&g1=1&g2=1&g3=0&g4=0&g5=0&g6=Y&g7=ffffff&g8=a0b0c0&g9=6080a0&h0=1&z
     */
    public static double expand(double x)
    {
        return (1.0 /(-1.5 * x + 2) - .5) / 1.5;
    }

    /**
     * This function could also be called 'expand' or 'changeContrast'. It's a S-shaped function meant to expand values in the (-1,1) interval
     * http://rechneronline.de/function-graphs/
     * a0=2&a1=1.1x/(.1+abs(x))&a2=&a3=&a4=1&a5=4&a6=8&a7=1&a8=1&a9=1&b0=480&b1=480&b2=-2&b3=2&b4=-2&b5=2&b6=8&b7=8&b8=5&b9=5&c0=3&c1=0&c2=1&c3=1&c4=1&c5=1&c6=1&c7=0&c8=0&c9=0&d0=1&d1=16&d2=16&d3=0&d4=&d5=&d6=&d7=&d8=&d9=&e0=&e1=&e2=&e3=&e4=14&e5=14&e6=13&e7=12&e8=0&e9=0&f0=0&f1=1&f2=1&f3=0&f4=0&f5=&f6=&f7=&f8=&f9=&g0=&g1=1&g2=1&g3=0&g4=0&g5=0&g6=Y&g7=ffffff&g8=a0b0c0&g9=6080a0&h0=1&h1=&h2=&h3=&h4=0&z
     * <p> 
     * <p>alpha 0  -> maximum expansion, maximum contrast increase, y=-1 before x=0 and y=1 after that
     * <p>alpha .1 -> strong expansion, strong contrast increase, slope is ~ 7 at x=0
     * <p>alpha .5 -> medium expansion, medium contrast increase, slope is ~ 2.7 at x=0
     * <p>alpha 1  -> mild expansion, mild contrast increase, slope is 2 at x=0
     * <p>alpha 2  -> very mild expansion, very mild contrast increase, slope is 1.5 at x=0
     */
    public static double softSign(double x, double alpha)
    {
        return (1 + alpha) * x / (alpha + abs(x));
    }
    
    public static double translate(double x, double fromMin, double fromMax, double toMin, double toMax)
    {
        double fromRange = fromMax - fromMin;
        double toRange = toMax - toMin;
        return (x - fromMin) / fromRange * toRange + toMin;
    }

    /**
     * For a value in (-1,1), transforms it such that -1 remains -1, 1 remains 1, oldCenter becomes 0, and other values
     * are linearly interpolated.
     */
    public static double centerM11(double x, double oldCenter)
    {
        if (x < oldCenter)
            return -1 + (x - -1) / (oldCenter - -1);
        return (x - oldCenter) / (1 - oldCenter);        
    }
}
