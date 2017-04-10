package gmjonker.math;

import java.util.List;

import static gmjonker.math.GeneralMath.mean;
import static gmjonker.math.GeneralMath.standardDeviation;
import static gmjonker.util.CollectionsUtil.map;

public class ToBeOrganized
{
    public static List<Double> changeMeanAndStdDev(List<Double> values, double newMean, double newStdDev)
    {
        double currentMean = mean(values);
        double currentStdDev = standardDeviation(values);
        return changeMeanAndStdDev(values, currentMean, currentStdDev, newMean, newStdDev);
    }

    public static List<Double> changeMeanAndStdDev(List<Double> values, double currentMean, double currentStdDev, double newMean,
            double newStdDev)
    {
        double factor = newStdDev / currentStdDev;
        return map(values, x -> newMean + (x - currentMean) * factor);
    }
}
