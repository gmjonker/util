package gmjonker.math;

import com.google.common.math.DoubleMath;
import org.junit.*;

import static gmjonker.math.GeneralMath.abs;
import static gmjonker.math.GeneralMath.max;
import static gmjonker.math.GeneralMath.pow;
import static gmjonker.math.SigmoidMath.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class SigmoidTest
{
    @Test
    public void fastSigmoidAlternatif()
    {
        System.out.println(Double.POSITIVE_INFINITY * pow(0,2));
        double eps = .000001;
        assertThat(fastSigmoidAlternative(0), closeTo(0.5, eps));
        assertThat(fastSigmoidAlternative(100000), closeTo(1, .0001));
        assertThat(fastSigmoidAlternative(-100000), closeTo(0, .0001));
        assertThat(fastSigmoidAlternative(Double.POSITIVE_INFINITY), closeTo(1, eps));
        assertThat(fastSigmoidAlternative(Double.NEGATIVE_INFINITY), closeTo(0, eps));
    }

    @Test
    public void fastSigmoidAlternativeLooksAlright()
    {
        // These should all look the same:
        TextPlot.plotf(x -> fastSigmoidAlternative(x), -5, 5, 0, 1, 100, 40);
        TextPlot.plotf(x -> fastSigmoidAlternative(x, 0, 1), -5, 5, 0, 1, 100, 40);
        TextPlot.plotf(x -> fastSigmoidAlternative(x, -1, 0), -5, 5, -1, 0, 100, 40);
        TextPlot.plotf(x -> fastSigmoidAlternative(x, -7, 3), -5, 5, -7, 3, 100, 40);
    }

    @Test
    public void fastLogitAlternativeLooksAlright()
    {
        // Should look equal to http://rechneronline.de/function-graphs/ with a0=2&a1=-log(1/x-1)&a2=(x - .5)/(x +.3)&a3=(.5-x)/(x-1.3)&a4=1&a5=4&a6=8&a7=1&a8=1&a9=1&b0=500&b1=500&b2=-1&b3=2&b4=-5&b5=5&b6=12&b7=10&b8=5&b9=5&c0=3&c1=0&c2=1&c3=1&c4=1&c5=1&c6=1&c7=0&c8=0&c9=0&d0=1&d1=12&d2=10&d3=0&d4=&d5=&d6=-1&d7=.5&d8=.5&d9=2&e0=&e1=&e2=&e3=&e4=14&e5=14&e6=13&e7=12&e8=0&e9=0&f0=0&f1=1&f2=1&f3=0&f4=0&f5=&f6=&f7=&f8=&f9=&g0=&g1=1&g2=1&g3=0&g4=0&g5=0&g6=Y&g7=ffffff&g8=a0b0c0&g9=6080a0&h0=1&z
        TextPlot.plotf(x -> fastLogitAlternative(x, -.3, 1.3), -1, 2, -5, 5, 80, 50);
        TextPlot.plotf(x -> fastLogitAlternative(x, -2, 0), -3, 1, -5, 5, 80, 50);
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
            double fastSigmoidAlternative1 = fastSigmoidAlternative(x);
            double fastLogitAlternative1 = fastLogitAlternative(fastSigmoidAlternative1);
            double diff1 = abs(x - fastLogitAlternative1);
            if (!Double.isNaN(diff1) && !Double.isInfinite(diff1))
                maxDiff1 = max(diff1, maxDiff1);
            for (int i = 0; i < 20; i++)
            {
                for (int j = 0; j < 20; j++)
                {
                    double rangeLow = (double) i / 10 - 1;
                    double rangeHigh = (double) j / 10;
                    double fastSigmoidAlternative2 = fastSigmoidAlternative(x, rangeLow, rangeHigh);
                    double fastLogitAlternative2 = fastLogitAlternative(fastSigmoidAlternative2, rangeLow, rangeHigh);
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
    public void getConfidencez()
    {
        System.out.println("sampleSizeToConfidenceFast(0, 1) = " + sampleSizeToConfidenceFast(0d, 1d));
        System.out.println("sampleSizeToConfidenceFast(1, 1) = " + sampleSizeToConfidenceFast(1d, 1d));
        System.out.println("sampleSizeToConfidenceFast(2, 1) = " + sampleSizeToConfidenceFast(2d, 1d));
        System.out.println("sampleSizeToConfidenceFast(4, 1) = " + sampleSizeToConfidenceFast(4d, 1d));
        System.out.println("sampleSizeToConfidenceFast(8, 1) = " + sampleSizeToConfidenceFast(8d, 1d));

        System.out.println("sampleSizeToConfidenceFast(0, .2) = " + sampleSizeToConfidenceFast(0d, .2d));
        System.out.println("sampleSizeToConfidenceFast(1, .2) = " + sampleSizeToConfidenceFast(1d, .2d));
        System.out.println("sampleSizeToConfidenceFast(2, .2) = " + sampleSizeToConfidenceFast(2d, .2d));
        System.out.println("sampleSizeToConfidenceFast(4, .2) = " + sampleSizeToConfidenceFast(4d, .2d));
        System.out.println("sampleSizeToConfidenceFast(8, .2) = " + sampleSizeToConfidenceFast(8d, .2d));
        System.out.println("sampleSizeToConfidenceFast(16, .2) = " + sampleSizeToConfidenceFast(16d, .2d));
        System.out.println("sampleSizeToConfidenceFast(32, .2) = " + sampleSizeToConfidenceFast(32d, .2d));

        System.out.println("sampleSizeToConfidenceFast(0, 5) = " + sampleSizeToConfidenceFast(0d, 5d));
        System.out.println("sampleSizeToConfidenceFast(1, 5) = " + sampleSizeToConfidenceFast(1d, 5d));
        System.out.println("sampleSizeToConfidenceFast(2, 5) = " + sampleSizeToConfidenceFast(2d, 5d));
        System.out.println("sampleSizeToConfidenceFast(4, 5) = " + sampleSizeToConfidenceFast(4d, 5d));
        System.out.println("sampleSizeToConfidenceFast(8, 5) = " + sampleSizeToConfidenceFast(8d, 5d));
    }

    @Test
    public void toMinusOneOneIntervall()
    {
        System.out.println("toMinusOneOneInterval(-2, 1 ) = " + toMinusOneOneInterval( -2d , 1d ));
        System.out.println("toMinusOneOneInterval(-1, 1 ) = " + toMinusOneOneInterval( -1d , 1d ));
        System.out.println("toMinusOneOneInterval( 0, 1 ) = " + toMinusOneOneInterval(  0d , 1d ));
        System.out.println("toMinusOneOneInterval( 1, 1 ) = " + toMinusOneOneInterval(  1d , 1d ));
        System.out.println("toMinusOneOneInterval( 2, 1 ) = " + toMinusOneOneInterval(  2d , 1d ));
        System.out.println("toMinusOneOneInterval( 4, 1 ) = " + toMinusOneOneInterval(  4d , 1d ));
        System.out.println("toMinusOneOneInterval( 8, 1 ) = " + toMinusOneOneInterval(  8d , 1d ));
        System.out.println("toMinusOneOneInterval(-2, .2) = " + toMinusOneOneInterval( -2d , .2d));
        System.out.println("toMinusOneOneInterval(-1, .2) = " + toMinusOneOneInterval( -1d , .2d));
        System.out.println("toMinusOneOneInterval( 0, .2) = " + toMinusOneOneInterval(  0d , .2d));
        System.out.println("toMinusOneOneInterval( 1, .2) = " + toMinusOneOneInterval(  1d , .2d));
        System.out.println("toMinusOneOneInterval( 2, .2) = " + toMinusOneOneInterval(  2d , .2d));
        System.out.println("toMinusOneOneInterval( 4, .2) = " + toMinusOneOneInterval(  4d , .2d));
        System.out.println("toMinusOneOneInterval( 8, .2) = " + toMinusOneOneInterval(  8d , .2d));
        System.out.println("toMinusOneOneInterval(16, .2) = " + toMinusOneOneInterval( 16d, .2d ));
        System.out.println("toMinusOneOneInterval(32, .2) = " + toMinusOneOneInterval( 32d, .2d ));
        System.out.println("toMinusOneOneInterval(-2, 5 ) = " + toMinusOneOneInterval( -2d , 5d ));
        System.out.println("toMinusOneOneInterval(-1, 5 ) = " + toMinusOneOneInterval( -1d , 5d ));
        System.out.println("toMinusOneOneInterval( 0, 5 ) = " + toMinusOneOneInterval(  0d , 5d ));
        System.out.println("toMinusOneOneInterval( 1, 5 ) = " + toMinusOneOneInterval(  1d , 5d ));
        System.out.println("toMinusOneOneInterval( 2, 5 ) = " + toMinusOneOneInterval(  2d , 5d ));
        System.out.println("toMinusOneOneInterval( 4, 5 ) = " + toMinusOneOneInterval(  4d , 5d ));
        System.out.println("toMinusOneOneInterval( 8, 5 ) = " + toMinusOneOneInterval(  8d , 5d ));
    }
}
