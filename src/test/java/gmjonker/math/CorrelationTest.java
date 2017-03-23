package gmjonker.math;

import com.google.common.primitives.Doubles;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.junit.*;

import java.util.List;

import static java.util.Arrays.asList;

public class CorrelationTest
{
    @Test
    public void covariance() throws Exception
    {
        {
            List<Double> s1 = asList(0.0, 1.0);
            List<Double> s2 = asList(0.0, 1.0);
            double cov1 = new Covariance().covariance(Doubles.toArray(s1), Doubles.toArray(s2), false);
            double cov2 = Correlation.covariance(s1, s2);
            double cov0 = Correlation.covariance0(s1, s2);
            double covi = Correlation.covariance0inflated(s1, s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            System.out.println("cov1 = " + cov1);
            System.out.println("cov2 = " + cov2);
            System.out.println("cov0 = " + cov0);
            System.out.println("covi = " + covi);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList(0.0, 1.0);
            List<Double> s2 = asList(1.0, 0.0);
            double cov1 = new Covariance().covariance(Doubles.toArray(s1), Doubles.toArray(s2), false);
            double cov2 = Correlation.covariance(s1, s2);
            double cov0 = Correlation.covariance0(s1, s2);
            double covi = Correlation.covariance0inflated(s1, s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            System.out.println("cov1 = " + cov1);
            System.out.println("cov2 = " + cov2);
            System.out.println("cov0 = " + cov0);
            System.out.println("covi = " + covi);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList(0.0, 1.0, .2);
            List<Double> s2 = asList(0.0, 1.0, -.2);
            double cov1 = new Covariance().covariance(Doubles.toArray(s1), Doubles.toArray(s2), false);
            double cov2 = Correlation.covariance(s1, s2);
            double cov0 = Correlation.covariance0(s1, s2);
            double covi = Correlation.covariance0inflated(s1, s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            System.out.println("cov1 = " + cov1);
            System.out.println("cov2 = " + cov2);
            System.out.println("cov0 = " + cov0);
            System.out.println("covi = " + covi);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList(0.0, 1.0, .2, .1, .1, .1);
            List<Double> s2 = asList(0.0, 1.0, -.2, .1, .1, .1);
            double cov1 = new Covariance().covariance(Doubles.toArray(s1), Doubles.toArray(s2), false);
            double cov2 = Correlation.covariance(s1, s2);
            double cov0 = Correlation.covariance0(s1, s2);
            double covi = Correlation.covariance0inflated(s1, s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            System.out.println("cov1 = " + cov1);
            System.out.println("cov2 = " + cov2);
            System.out.println("cov0 = " + cov0);
            System.out.println("covi = " + covi);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList(0.0, -1.0, .2, .1, .1, .1);
            List<Double> s2 = asList(0.0, -1.0, -.2, .1, .1, .1);
            double cov1 = new Covariance().covariance(Doubles.toArray(s1), Doubles.toArray(s2), false);
            double cov2 = Correlation.covariance(s1, s2);
            double cov0 = Correlation.covariance0(s1, s2);
            double covi = Correlation.covariance0inflated(s1, s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            System.out.println("cov1 = " + cov1);
            System.out.println("cov2 = " + cov2);
            System.out.println("cov0 = " + cov0);
            System.out.println("covi = " + covi);
            System.out.println("proc = " + proc);
            System.out.println();
        }
    }
    
    @Test
    public void profileCor()
    {
        {
            List<Double> s1 = asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            List<Double> s2 = asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            List<Double> s2 = asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList(1.0, 1.0, 0.0, 0.0, 0.0, 0.0);
            List<Double> s2 = asList(1.0, 1.0, 0.0, 0.0, 0.0, 0.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList(1.0, 1.0, 1.0, 1.0, 0.0, 0.0);
            List<Double> s2 = asList(1.0, 1.0, 1.0, 1.0, 0.0, 0.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList(1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
            List<Double> s2 = asList(1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        
        System.out.println();
        {
            List<Double> s1 = asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            List<Double> s2 = asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            List<Double> s2 = asList(0.0, 1.0, 0.0, 0.0, 0.0, 0.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList(0.0, 0.0, 1.0, 0.0, 0.0, 0.0);
            List<Double> s2 = asList(0.0, 1.0, 0.0, 1.0, 0.0, 0.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList(0.0, 0.0, 1.0, 0.0, 1.0, 0.0);
            List<Double> s2 = asList(0.0, 1.0, 0.0, 1.0, 0.0, 1.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }

        System.out.println();
        {
            List<Double> s1 = asList(1.0, 1.0, 1.0, 0.0, 0.0, 0.0);
            List<Double> s2 = asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList( 1.0,  1.0,  1.0, 0.0, 0.0, 0.0);
            List<Double> s2 = asList(-1.0,  0.0,  0.0, 0.0, 0.0, 0.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList( 1.0,  1.0,  1.0, 0.0, 0.0, 0.0);
            List<Double> s2 = asList(-1.0, -1.0,  0.0, 0.0, 0.0, 0.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
        {
            List<Double> s1 = asList( 1.0,  1.0,  1.0, 0.0, 0.0, 0.0);
            List<Double> s2 = asList(-1.0, -1.0, -1.0, 0.0, 0.0, 0.0);
            System.out.println("s1 = " + s1);
            System.out.println("s2 = " + s2);
            double proc = Correlation.profileCorrelation(s1, s2);
            System.out.println("proc = " + proc);
            System.out.println();
        }
    }
}