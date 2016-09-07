package gmjonker.math;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import com.google.common.math.DoubleMath;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.junit.*;

import static com.google.common.primitives.Doubles.asList;
import static gmjonker.math.GeneralMath.*;
import static gmjonker.math.NaType.NA;
import static gmjonker.math.NaType.isValue;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.*;

public class GeneralMathTest
{
    @Test
    public void roundd()
    {
        System.out.println("round(1.23, 1) = " + round(1.23, 1));
        System.out.println("round(1.25, 1) = " + round(1.25, 1));
        System.out.println("round(NaN, 1) = " + round(Double.NaN, 1));
    }

    @Test
    public void fastSigmoidAlternativeLooksAlright()
    {
        // These should all look the same:
        TextPlot.plotf(x -> GeneralMath.fastSigmoidAlternative(x), -5, 5, 0, 1, 100, 40);
        TextPlot.plotf(x -> GeneralMath.fastSigmoidAlternative(x, 0, 1), -5, 5, 0, 1, 100, 40);
        TextPlot.plotf(x -> GeneralMath.fastSigmoidAlternative(x, -1, 0), -5, 5, -1, 0, 100, 40);
        TextPlot.plotf(x -> GeneralMath.fastSigmoidAlternative(x, -7, 3), -5, 5, -7, 3, 100, 40);
    }

    @Test
    public void fastLogitAlternativeLooksAlright()
    {
        // Should look equal to http://rechneronline.de/function-graphs/ with a0=2&a1=-log(1/x-1)&a2=(x - .5)/(x +.3)&a3=(.5-x)/(x-1.3)&a4=1&a5=4&a6=8&a7=1&a8=1&a9=1&b0=500&b1=500&b2=-1&b3=2&b4=-5&b5=5&b6=12&b7=10&b8=5&b9=5&c0=3&c1=0&c2=1&c3=1&c4=1&c5=1&c6=1&c7=0&c8=0&c9=0&d0=1&d1=12&d2=10&d3=0&d4=&d5=&d6=-1&d7=.5&d8=.5&d9=2&e0=&e1=&e2=&e3=&e4=14&e5=14&e6=13&e7=12&e8=0&e9=0&f0=0&f1=1&f2=1&f3=0&f4=0&f5=&f6=&f7=&f8=&f9=&g0=&g1=1&g2=1&g3=0&g4=0&g5=0&g6=Y&g7=ffffff&g8=a0b0c0&g9=6080a0&h0=1&z
        TextPlot.plotf(x -> GeneralMath.fastLogitAlternative(x, -.3, 1.3), -1, 2, -5, 5, 80, 50);
        TextPlot.plotf(x -> GeneralMath.fastLogitAlternative(x, -2, 0), -3, 1, -5, 5, 80, 50);
    }

    @Test
    public void fastLogitInversesSigmoidCorrectly()
    {
        // Test that fastLogitAlternative is a correct inverse of fastSigmoidAlternative.
        // We test with several combination of values and ranges.

        // Only when rangeLow is extremely close to rangeHigh, does fastSigmoidAlternative(fastLogitAlternative(x))
        // become a bit imprecise.

        double maxDiff1 = Double.MIN_VALUE;
        double maxDiff2 = Double.MIN_VALUE;

        for (int k = 0; k < 20; k++)
        {
            double x = (double) k / 2 - 5;
            double fastSigmoidAlternative1 = GeneralMath.fastSigmoidAlternative(x);
            double fastLogitAlternative1 = GeneralMath.fastLogitAlternative(fastSigmoidAlternative1);
            double diff1 = abs(x - fastLogitAlternative1);
            if (!Double.isNaN(diff1) && !Double.isInfinite(diff1))
                maxDiff1 = max(diff1, maxDiff1);
            for (int i = 0; i < 20; i++)
            {
                for (int j = 0; j < 20; j++)
                {
                    double rangeLow = (double) i / 10 - 1;
                    double rangeHigh = (double) j / 10;
                    double fastSigmoidAlternative2 = GeneralMath.fastSigmoidAlternative(x, rangeLow, rangeHigh);
                    double fastLogitAlternative2 = GeneralMath.fastLogitAlternative(fastSigmoidAlternative2, rangeLow, rangeHigh);
                    double diff2 = abs(x - fastLogitAlternative2);
                    if (!Double.isNaN(diff2) && !Double.isInfinite(diff2) && ! DoubleMath.fuzzyEquals(rangeLow, rangeHigh, 1E-10))
                        maxDiff2 = max(diff2, maxDiff2);
                }
            }
        }

        // Verify that x = logit(sigmoid(x))
        assertThat(maxDiff1, closeTo(0, .0000000001));
        assertThat(maxDiff2, closeTo(0, .0000000001));
    }

    @Test
    public void testMean()
    {
        Assert.assertThat(mean(asList(1, 2, 3.3)), closeTo(6.3/3, .000001));
    }

    @Test
    public void weightedMean()
    {
        double eps = 0.00001;
        assertEquals(4.2/6, GeneralMath.weightedMean(new double[]{0.2, 0.5, 1.0}, new double[]{1d, 2d, 3d}), eps);
    }

    @Test
    public void weightedMeanWithDefaults()
    {
        double eps = 0.00001;
        assertEquals(4.2/6, GeneralMath.weightedMeanWithDefaults(new double[]{0.2, 0.5, 1.0}, new double[]{1d, 2d, 3d}, new double[]{.5, .5, .5}), eps);
        assertEquals(4.2/6, GeneralMath.weightedMeanWithDefaults(new double[]{0.2, NA, 1.0}, new double[]{1d, 2d, 3d}, new double[]{.5, .5, .5}), eps);
        assertEquals(
                GeneralMath.weightedMeanWithDefaults(new double[]{0.2, NA, 1.0}, new double[]{1d, 2d, 3d}, new double[]{.5, .5, .5}),
                GeneralMath.weightedMeanWithDefaults(new double[]{0.2, NA, 1.0}, new double[]{2d, 4d, 6d}, new double[]{.5, .5, .5}),
                eps
        );
    }

    @Test
    public void weightedMeanWithDefaultWithExcludes()
    {
        double eps = 0.00001;
        assertEquals( .2, GeneralMath.weightedMeanWithDefaults(new double[]{0.2, 0.5, 1.0}, new double[]{1d, 2d, 3d},
                new boolean[]{true, false, false}, new double[]{.5, .5, .5}), eps);
        assertEquals( .4, GeneralMath.weightedMeanWithDefaults(new double[]{0.2, 0.5, 1.0}, new double[]{1d, 2d, 3d},
                new boolean[]{true, true, false}, new double[]{.5, .5, .5}), eps);
        assertEquals(1.0, GeneralMath.weightedMeanWithDefaults(new double[]{0.2, 0.5, 1.0}, new double[]{1d, 2d, 3d},
                new boolean[]{false, false, true}, new double[]{.5, .5, .5}), eps);
    }

    @Test
    public void meanAbsoluteError()
    {
        assertEquals(GeneralMath.meanAbsoluteError(new double[]{1.0, .9, .5}), .2, 0.00001);
    }

    @Test
    public void weightedMeanAbsoluteError()
    {
        assertEquals(GeneralMath.weightedMeanAbsoluteError(new double[]{1.0, .5, .2}, new double[]{1, 2, 5}), 5.0/8, 0.00001);
    }

    public static void powerMean()
    {
        int p = 25;
        System.out.println(GeneralMath.powerMean(new double[]{0, 0, 0}, p));
        System.out.println(GeneralMath.powerMean(new double[]{1, 0, 0}, p));
        System.out.println(GeneralMath.powerMean(new double[]{1, 1, 0}, p));
        System.out.println(GeneralMath.powerMean(new double[]{1, 1, 1}, p));
        System.out.println(GeneralMath.powerMean(new double[]{.1, .2, .3}, p));
        System.out.println();
        System.out.println(GeneralMath.powerMean(new double[]{.5, .5, .5}, p));
        System.out.println(GeneralMath.powerMean(new double[]{1, .5, .5}, p));
        System.out.println(GeneralMath.powerMean(new double[]{1, 1, .5}, p));
        System.out.println(GeneralMath.powerMean(new double[]{1, 1, 1}, p));
        System.out.println(GeneralMath.powerMean(new double[]{0, .5, .5}, p));
        System.out.println(GeneralMath.powerMean(new double[]{0, 1, .5}, p));
        System.out.println();
        System.out.println(GeneralMath.powerMean(new double[]{0, -.5, .5}, 2));
        System.out.println(GeneralMath.powerMean(new double[]{0, -1, .5}, p));
        System.out.println();
        System.out.println(GeneralMath.powerMean(new double[]{.8}, p));
        System.out.println(GeneralMath.powerMean(new double[]{.8, .5}, p));
        System.out.println(GeneralMath.powerMean(new double[]{.8, .5, .5}, p));
    }

    @Test
    public void rootMeanSquare()
    {
        assertEquals(Math.sqrt(14.0 / 3), GeneralMath.rootMeanSquare(new double[]{1, 2, 3}), .00001);
    }

    @Test
    public void rootMeanSquareError()
    {
        assertEquals(GeneralMath.rootMeanSquareError(new double[]{.3, .5, .7}), 0.5259911, 0.00001);
    }

    @Test
    public void rootWeightedMeanSquareError()
    {
        assertEquals(GeneralMath.rootWeightedMeanSquareError(new double[]{.3, .5, .7}, new double[]{.5, 1., 2}), 0.439155, 0.00001);
        assertEquals(GeneralMath.rootWeightedMeanSquareError(new double[]{.3, .5, .7}, new double[]{.5, 1., 2}),
                GeneralMath.rootWeightedMeanSquareError(asList(.3, .5, .7), asList(.5, 1., 2)), 0.00001);
    }

    @Test
    public void harmonicMean()
    {
        double eps = 0.00001;
        assertThat(GeneralMath.harmonicMean(new double[]{1, .5}), closeTo(2d/3, eps));
        assertThat(GeneralMath.harmonicMean(new double[]{2, 3}), closeTo(2.4, eps));
    }

    @Test
    public void f1Measure()
    {
        double eps = 0.00001;
        assertThat(GeneralMath.f1Measure(1, .5), closeTo(GeneralMath.harmonicMean(new double[]{1, .5}), eps));
    }

    @Test
    public void variance()
    {
        double eps = 0.00001;
        // Check our variance function against Colt
        assertThat(GeneralMath.variance(1), closeTo(getColtVariance(1), eps));
        assertThat(GeneralMath.variance(1, .5), closeTo(getColtVariance(1, .5), eps));
        assertThat(GeneralMath.variance(1, .5, .1), closeTo(getColtVariance(1, .5, .1), eps));

        // Check our variance function against commons math.
        Variance variance = new Variance(false);
        assertThat(GeneralMath.variance(1), closeTo(variance.evaluate(new double[]{1}), eps));
        assertThat(GeneralMath.variance(1, .5), closeTo(variance.evaluate(new double[]{1, .5}), eps));
        assertThat(GeneralMath.variance(1, .5, .1), closeTo(variance.evaluate(new double[]{1, .5, .1}), eps));
    }

    private double getColtVariance(double... values)
    {
        DoubleArrayList doubleArrayList = new DoubleArrayList(values);
        double sum = Descriptive.sum(doubleArrayList);
        double sumOfSquares = Descriptive.sumOfSquares(doubleArrayList);
        return Descriptive.variance(values.length, sum, sumOfSquares);
    }

    @Test
    public void exponentialMovingAverageV1()
    {
        double eps = 0.0001;
        assertTrue( ! isValue(GeneralMath.exponentialMovingAverageV1(new double[]{}, .7)));
        assertEquals(1  , GeneralMath.exponentialMovingAverageV1(new double[]{1}, .7), eps);
        assertEquals(.7 , GeneralMath.exponentialMovingAverageV1(new double[]{0, 1}, .7), eps);
        assertEquals(.7 , GeneralMath.exponentialMovingAverageV1(new double[]{0, 0, 1}, .7), eps);
        assertEquals(.21, GeneralMath.exponentialMovingAverageV1(new double[]{0, 1, 0}, .7), eps);
        assertEquals(.09, GeneralMath.exponentialMovingAverageV1(new double[]{1, 0, 0}, .7), eps);
    }

    @Test
    public void exponentialMovingAverageV2()
    {
        double eps = 0.0001;
        assertTrue( ! isValue(GeneralMath.exponentialMovingAverageV2(new double[]{}, .5)));
        assertEquals(.5   , GeneralMath.exponentialMovingAverageV2(new double[]{1}, .5), eps);
        assertEquals(.5   , GeneralMath.exponentialMovingAverageV2(new double[]{0, 1}, .5), eps);
        assertEquals(.5   , GeneralMath.exponentialMovingAverageV2(new double[]{0, 0, 1}, .5), eps);
        assertEquals(.25  , GeneralMath.exponentialMovingAverageV2(new double[]{0, 1, 0}, .5), eps);
        assertEquals(.125 , GeneralMath.exponentialMovingAverageV2(new double[]{1, 0, 0}, .5), eps);
        assertEquals(.5   , GeneralMath.exponentialMovingAverageV2(new double[]{0, 0, 1}, .5), eps);
        assertEquals(.75  , GeneralMath.exponentialMovingAverageV2(new double[]{0, 1, 1}, .5), eps);
        assertEquals(.875 , GeneralMath.exponentialMovingAverageV2(new double[]{1, 1, 1}, .5), eps);
    }

    @Test
    public void exponentialMovingAverageV3()
    {
        double eps = 0.0001;
        assertTrue( ! isValue(GeneralMath.exponentialMovingAverageV3(new double[]{}, .5)));
        assertEquals(1      , GeneralMath.exponentialMovingAverageV3(new double[]{1}, .5), eps);
        assertEquals(.625   , GeneralMath.exponentialMovingAverageV3(new double[]{0, 1}, .5), eps);
        assertEquals(13.0/24, GeneralMath.exponentialMovingAverageV3(new double[]{0, 0, 1}, .5), eps);
    }

    @Test
    public void exponentialMovingAverageV4()
    {
        double eps = 0.0001;
        assertEquals(.4       , GeneralMath.exponentialMovingAverageV4(new double[]{}, .4, .5), eps);
        assertEquals(.7       , GeneralMath.exponentialMovingAverageV4(new double[]{.8}, .4, .75), eps);
        assertEquals(.3+.25*.7, GeneralMath.exponentialMovingAverageV4(new double[]{.8, .4}, .4, .75), eps);
    }

    @Test
    public void tryExponentialMovingAverageV1()
    {
        // I'm trying to find the right alpha for the "local" average of recommendation subscores.
        // This would be good if it resembled the moving average of the last ten items
        GeneralMath.printExponentialCoefficients(.1, 20);
        double alpha = .1;
        System.out.println(GeneralMath.exponentialMovingAverageV1(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV1(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV1(new double[]{0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV1(new double[]{0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV1(new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, alpha));
    }

    @Test
    public void tryExponentialMovingAverageV2()
    {
        // Trying to find the right alpha to penalize recent similarity
        // similar item 1 down -> hefty penalty
        // similar item 5 down -> hardly any penalty anymore
        double alpha = .5;
        System.out.println(GeneralMath.exponentialMovingAverageV2(new double[]{0, 0, 0, 0, 1}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV2(new double[]{0, 0, 0, 1, 0}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV2(new double[]{0, 0, 1, 0, 0}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV2(new double[]{0, 1, 0, 0, 0}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV2(new double[]{1, 0, 0, 0, 0}, alpha));
    }

    @Test
    public void tryExponentialMovingAverageV3()
    {
        // Trying to find the right alpha to penalize recent similarity
        // similar item 1 down -> hefty penalty
        // similar item 5 down -> hardly any penalty anymore
        double alpha = .5;
        System.out.println(GeneralMath.exponentialMovingAverageV3(new double[]{0, 0, 0, 0, 1}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV3(new double[]{0, 0, 0, 1, 0}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV3(new double[]{0, 0, 1, 0, 0}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV3(new double[]{0, 1, 0, 0, 0}, alpha));
        System.out.println(GeneralMath.exponentialMovingAverageV3(new double[]{1, 0, 0, 0, 0}, alpha));
    }

    @Test
    public void expand()
    {
        TextPlot.plotf(GeneralMath::expand, 0, 1, -.2, 1, 100, 40);
    }
}
