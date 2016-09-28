package gmjonker.math;

import org.apache.commons.math3.analysis.function.Logit;
import org.apache.commons.math3.analysis.function.Sigmoid;

import static gmjonker.math.GeneralMath.abs;

public class SigmoidMath
{
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
     * This sigmoid alternative has a slope of .5 at 0, whereas the normal sigmoid has a slope of .25 at 0.
     *
     * This sigmoid alternative has a slope of .25 at 0:
     * a0=2&a1=(1/-(x+2) + .5) + .5&a2=sgm(x)&a3=&a4=1&a5=4&a6=8&a7=1&a8=1&a9=1&b0=500&b1=500&b2=-1&b3=20&b4=-.5&b5=1.5&b6=21&b7=20&b8=5&b9=5&c0=3&c1=0&c2=1&c3=1&c4=1&c5=1&c6=1&c7=0&c8=0&c9=0&d0=1&d1=21&d2=20&d3=0&d4=&d5=&d6=&d7=&d8=&d9=&e0=&e1=&e2=&e3=&e4=14&e5=14&e6=13&e7=12&e8=0&e9=0&f0=0&f1=1&f2=1&f3=0&f4=0&f5=&f6=&f7=&f8=&f9=&g0=&g1=1&g2=1&g3=0&g4=0&g5=0&g6=Y&g7=ffffff&g8=a0b0c0&g9=6080a0&h0=1&z
     **/
    public static double fastSigmoidAlternative(double x)
    {
        if (x == Double.POSITIVE_INFINITY) return 1;
        if (x == Double.NEGATIVE_INFINITY) return 0;
        return .5 * (1 + x / (1 + abs(x)));
    }

    public static double fastSigmoidAlternative2(double x)
    {
        if (x == Double.POSITIVE_INFINITY) return 1;
        if (x == Double.NEGATIVE_INFINITY) return 0;
        return 1 - 1 / (x + 2);
    }

    /**
     * A fast alternative to the sigmoid function, output in range (rangeLow, rangeHi).
     * See also {@code fastSigmoidAlternative(x)}.
     **/
    public static double fastSigmoidAlternative(double x, double rangeLow, double rangeHigh)
    {
        return rangeLow + (rangeHigh - rangeLow) * fastSigmoidAlternative(x);
    }

    /**
     * http://rechneronline.de/function-graphs/
     * a0=2&a1=-1/(x+1) + 1&a2=&a3=&a4=1&a5=4&a6=8&a7=1&a8=1&a9=1&b0=500&b1=500&b2=-5&b3=5&b4=-5&b5=5&b6=10&b7=10&b8=5&b9=5&c0=3&c1=0&c2=1&c3=1&c4=1&c5=1&c6=1&c7=0&c8=0&c9=0&d0=1&d1=20&d2=20&d3=0&d4=&d5=&d6=&d7=&d8=&d9=&e0=&e1=&e2=&e3=&e4=14&e5=14&e6=13&e7=12&e8=0&e9=0&f0=0&f1=1&f2=1&f3=0&f4=0&f5=&f6=&f7=&f8=&f9=&g0=&g1=1&g2=1&g3=0&g4=0&g5=0&g6=Y&g7=ffffff&g8=a0b0c0&g9=6080a0&h0=1&z
     * @param sampleSize The number of samples you have used to get a result. Must be > 0.
     * @param growthParameter The higher this value, the faster this function will approach 1 for increasing sample size
     * @return A value in [0,1)
     */
    public static double getConfidence(double sampleSize, double growthParameter)
    {
        if (sampleSize < 0)
            throw new RuntimeException("getConfidence not defined for sampleSize < 0");
        return -1.0 / (growthParameter * sampleSize + 1) + 1;
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
}
