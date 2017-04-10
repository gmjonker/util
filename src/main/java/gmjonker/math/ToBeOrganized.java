package gmjonker.math;

import java.util.List;

import static gmjonker.math.GeneralMath.mean;
import static gmjonker.math.GeneralMath.standardDeviation;
import static gmjonker.util.CollectionsUtil.map;

public class ToBeOrganized
{
    public static List<Double> changeMeanAndStdDev(List<Double> values, double newMean, double newStdDev)
    {
        double mean = mean(values);
        double stdDev = standardDeviation(values);
        double factor = newStdDev / stdDev;
        return map(values, x -> newMean + (x - mean) * factor);
    }
}
