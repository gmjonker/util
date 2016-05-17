package gmjonker.math;

public class Range
{
    /**
     * Converts a (0,1) value into a (-1,1) value, where
     * <ul>
     * <li>0 -> -1</li>
     * <li>neutralValue -> 0</li>
     * <li>1 -> 1</li>
     * </ul>
     * and the other values are interpolated.
     */
    public static double from01toM11(double value, double neutralValue)
    {
        if (value < neutralValue)
            return (neutralValue - value) / neutralValue * -1;
        else
            return (value - neutralValue) / (1 - neutralValue);
    }

    /**
     * Converts a (-1,1) value into a (0,1) value, where
     * <ul>
     * <li>-1 -> 0</li>
     * <li>0 -> neutralValue</li>
     * <li>1 -> 1</li>
     * </ul>
     * and the other values are linearly interpolated.
     */
    public static double fromM11to01(double value, double neutralValue)
    {
        if (value < 0)
            return (value + 1) * neutralValue;
        else
            return neutralValue + value * (1 - neutralValue);
    }

    /**
     * Converts 1->0, 10->1, and other values interpolated.
     */
    @Deprecated
    public static double tenBasedScoreToScore(double tenBasedScore)
    {
        return (tenBasedScore - 1) / 9.0;
    }

    /**
     * Converts 0->1, 1->10, and other values interpolated.
     */
    @Deprecated
    public static double scoreToTenBasedScore(double score)
    {
        return 1 + 9 * score;
    }
}
