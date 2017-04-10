package gmjonker.math;

import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static gmjonker.math.GeneralMath.mean;
import static gmjonker.math.GeneralMath.standardDeviation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class ToBeOrganizedTest
{
    @Test
    public void changeMeanAndStdDevTest() throws Exception
    {
        Random random = new Random();
        List<Double> xs = new ArrayList<>();
        for (int i = 0; i < 100; i++) 
            xs.add(random.nextGaussian());

        double mean = mean(xs);
        double sd = standardDeviation(xs);
        System.out.println("mean = " + mean);
        System.out.println("sd = " + sd);

        List<Double> ys = ToBeOrganized.changeMeanAndStdDev(xs, 2.3, 4.5);

        double newMean = mean(ys);
        double newSd = standardDeviation(ys);
        System.out.println("newMean = " + newMean);
        System.out.println("newSd = " + newSd);
        
        assertThat(newMean, closeTo(2.3, .000001));
        assertThat(newSd, closeTo(4.5, .000001));
    }

    @Test
    public void changeMeanAndStdDev2Test() throws Exception
    {
        Random random = new Random();
        List<Double> xs = new ArrayList<>();
        for (int i = 0; i < 100; i++) 
            xs.add(random.nextGaussian());

        double mean = mean(xs);
        double sd = standardDeviation(xs);
        System.out.println("mean = " + mean);
        System.out.println("sd = " + sd);

        List<Double> ys = ToBeOrganized.changeMeanAndStdDev(xs, mean, sd, 2.3, 4.5);

        double newMean = mean(ys);
        double newSd = standardDeviation(ys);
        System.out.println("newMean = " + newMean);
        System.out.println("newSd = " + newSd);
        
        assertThat(newMean, closeTo(2.3, .000001));
        assertThat(newSd, closeTo(4.5, .000001));
    }

}