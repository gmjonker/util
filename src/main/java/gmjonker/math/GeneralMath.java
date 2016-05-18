package gmjonker.math;

import com.google.common.primitives.Ints;
import gmjonker.util.LambdaLogger;
import org.apache.commons.math3.analysis.function.Logit;
import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.util.Arrays;
import java.util.List;

import static gmjonker.math.NaType.NA;
import static gmjonker.math.NaType.isValue;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * All methods are thread-safe.
 */
public class GeneralMath
{
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

    public static double log(double base)
    {
        return Math.log(base);
    }

    public static double log(double base, double x)
    {
        return Math.log(x) / Math.log(base);
    }

    public static double sqrt(double x)
    {
        return Math.sqrt(x);
    }

    public static long round(double x)
    {
        return Math.round(x);
    }

    public static double round(double value, int decimals)
    {
        return Math.round(value * pow(10, decimals)) / pow(10, decimals);
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

    /** Returns max(min(x, max), min). **/
    public static double limit(double x, double min, double max)
    {
        return max(min(x, max), min);
    }

    public static double sum(double... values)
    {
        return StatUtils.sum(values);
    }

    public static double mean(double... values)
    {
        if (values.length == 0)
            return NA;
        return StatUtils.mean(values);
    }

    public static double weightedMean(double[] values, double[] weights)
    {
        double sum = 0;
        double totalWeight = 0;
        for (int i = 0; i < values.length; i++) {
            double value = values[i];
            double weight = weights[i];
            sum += weight * value;
            totalWeight += weight;
        }
        if (totalWeight < 0) {
            log.error("sum(weights) must be positive");
            return NA;
        }
        return sum / totalWeight;
    }

    public static double weightedMeanIgnoreNAs(double[] values, double[] weights)
    {
        double sum = 0;
        double totalWeight = 0;
        for (int i = 0; i < values.length; i++) {
            double value = values[i];
            if ( ! isValue(value))
                continue;
            double weight = weights[i];
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

    public static double rootMeanSquare(double[] values)
    {
        return powerMean(values, 2);
    }

    public static double rootMeanSquareError(double[] values)
    {
        double temp = 0;
        for (double value : values) {
            temp += Math.pow(1 - value, 2);
        }
        return Math.sqrt(temp / values.length);
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
    public static double weightedStandardDeviation(double[] values, double[] weights)
    {
        boolean isBiasCorrected = false;
        return sqrt(new Variance(isBiasCorrected).evaluate(values, weights));
    }

    /** The logistic or standard sigmoid function, output in range (0,1) **/
    public static double sigmoid(double x)
    {
        return new Sigmoid().value(x);
    }

    /** Sigmoid function, output in range (rangeLow, rangeHi) **/
    public static double sigmoid(double x, double rangeLow, double rangeHigh)
    {
        return new Sigmoid(rangeLow, rangeHigh).value(x);
    }

    /**
     * A fast alternative to the sigmoid function, output in range (0,1).
     * For the difference in shape with the sigmoid function, go to http://rechneronline.de/function-graphs/ and load
     * a0=2&a1=sgm(x)&a2=.5 * (1 + x/(1+abs(x)))&a3=&a4=1&a5=4&a6=8&a7=1&a8=1&a9=1&b0=500&b1=500&b2=-5&b3=5&b4=-1&b5=2&b6=10&b7=12&b8=5&b9=5&c0=3&c1=0&c2=1&c3=1&c4=1&c5=1&c6=1&c7=0&c8=0&c9=0&d0=1&d1=10&d2=12&d3=0&d4=&d5=&d6=&d7=&d8=&d9=&e0=&e1=&e2=&e3=&e4=14&e5=14&e6=13&e7=12&e8=0&e9=0&f0=0&f1=1&f2=1&f3=0&f4=0&f5=&f6=&f7=&f8=&f9=&g0=&g1=1&g2=1&g3=0&g4=0&g5=0&g6=Y&g7=ffffff&g8=a0b0c0&g9=6080a0&h0=1&z
     **/
    public static double fastSigmoidAlternative(double x)
    {
        return .5 * (1 + x / (1 + abs(x)));
    }

    /**
     * A fast alternative to the sigmoid function, output in range (rangeLow, rangeHi).
     * See also {@code fastSigmoidAlternative(x)}.
     **/
    public static double fastSigmoidAlternative(double x, double rangeLow, double rangeHigh)
    {
        return rangeLow + (rangeHigh - rangeLow) * fastSigmoidAlternative(x);
    }

    /** The inverse logistic function, input in range (0,1) **/
    public static double logit(double x)
    {
        return new Logit().value(x);
    }

    /** Inverse sigmoid function, input in range(rangeLow, rangeHi) **/
    public static double logit(double x, double rangeLow, double rangeHigh)
    {
        return new Logit(rangeLow, rangeHigh).value(x);
    }

    /**
     * A fast alternative to the inverse logistic/sigmoid function, input in range (0,1)
     * For the difference in shape with the logit function, go to http://rechneronline.de/function-graphs/ and load
     * a0=2&a1=-log(1/x-1)&a2=(.5-x)/(x-1)&a3=(x - .5)/x&a4=1&a5=4&a6=8&a7=1&a8=1&a9=1&b0=500&b1=500&b2=-1&b3=2&b4=-5&b5=5&b6=10&b7=10&b8=5&b9=5&c0=3&c1=0&c2=1&c3=1&c4=1&c5=1&c6=1&c7=0&c8=0&c9=0&d0=1&d1=20&d2=20&d3=0&d4=&d5=&d6=.5&d7=1&d8=0&d9=.5&e0=&e1=&e2=&e3=&e4=14&e5=14&e6=13&e7=12&e8=0&e9=0&f0=0&f1=1&f2=1&f3=0&f4=0&f5=&f6=&f7=&f8=&f9=&g0=&g1=1&g2=1&g3=0&g4=0&g5=0&g6=Y&g7=ffffff&g8=a0b0c0&g9=6080a0&h0=1&z
     **/
    public static double fastLogitAlternative(double x)
    {
        if (x <= .5)
            return (x - .5) / x;
        else
            return (x - .5) / (1 - x);
    }

    /**
     * A fast alternative to the inverse logistic/sigmoid function, input in range (rangeLow,rangeHigh).
     * For the difference in shape with the logit function, go to http://rechneronline.de/function-graphs/ and load
     * a0=2&a1=-log(1/x-1)&a2=(x - .5)/(x +.3)&a3=(.5-x)/(x-1.3)&a4=1&a5=4&a6=8&a7=1&a8=1&a9=1&b0=500&b1=500&b2=-1&b3=2&b4=-5&b5=5&b6=12&b7=10&b8=5&b9=5&c0=3&c1=0&c2=1&c3=1&c4=1&c5=1&c6=1&c7=0&c8=0&c9=0&d0=1&d1=12&d2=10&d3=0&d4=&d5=&d6=-1&d7=.5&d8=.5&d9=2&e0=&e1=&e2=&e3=&e4=14&e5=14&e6=13&e7=12&e8=0&e9=0&f0=0&f1=1&f2=1&f3=0&f4=0&f5=&f6=&f7=&f8=&f9=&g0=&g1=1&g2=1&g3=0&g4=0&g5=0&g6=Y&g7=ffffff&g8=a0b0c0&g9=6080a0&h0=1&z
     * If rangeLow == rangeHigh, the result will be NaN. If rangeLow is extremely close to rangeHigh, the result may be +/-infinity.
     **/
    public static double fastLogitAlternative(double x, double rangeLow, double rangeHigh)
    {
        double middle = (rangeLow + rangeHigh) / 2;
        boolean reversed = rangeLow > rangeHigh;
        if (reversed) {
            if (x >= middle)
                return (x - middle) / (x - rangeLow);
            else
                return (x - middle) / (rangeHigh - x);
        } else {
            if (x <= middle)
                return (x - middle) / (x - rangeLow);
            else
                return (x - middle) / (rangeHigh - x);
        }
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

}
