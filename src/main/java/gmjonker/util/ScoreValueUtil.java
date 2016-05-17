package gmjonker.util;

import static gmjonker.math.GeneralMath.abs;
import static gmjonker.math.NaType.isValue;

/**
 * Utility class to work with "score" values in ranges (-1,1), (0,1) and (1,10).
 */
public class ScoreValueUtil
{
    public static final double SCORE_VALUE_EPSILON = 5E-16;

    /**
     * Checks whether value1 is equal to value2, with a tiny error margin to correct for floating-point
     * arithmetic imprecision. Values are in or around range [-1,1].
     **/
    public static boolean scoreValueEquals(double value1, double value2)
    {
        if ( ! isValue(value1) && ! isValue(value2))
            return true;
        return abs(value1 - value2) < SCORE_VALUE_EPSILON;
    }

    /**
     * Checks whether value1 is equal to or less than value2, with a tiny error margin to correct for floating-point
     * arithmetic imprecision. Values are in or around range [-1,1].
     **/
    public static boolean scoreValueEqualToOrLessThan(double value1, double value2)
    {
        if ( ! isValue(value1) || ! isValue(value2))
            return false;
        return value1 <= value2 + SCORE_VALUE_EPSILON;
    }

    /**
     * Checks whether value1 is equal to or greater than value2, with a tiny error margin to correct for floating-point
     * arithmetic imprecision. Values are in or around range [-1,1].
     **/
    public static boolean scoreValueEqualToOrGreaterThan(double value1, double value2)
    {
        if ( ! isValue(value1) || ! isValue(value2))
            return false;
        return value1 >= value2 - SCORE_VALUE_EPSILON;
    }
}
