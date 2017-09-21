package gmjonker.math;

import org.junit.*;

import static gmjonker.math.SigmoidMath.*;
import static gmjonker.util.CollectionsUtil.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class SigmoidMathTest
{
    @Test
    public void toMinusOneOneIntervalTest() throws Exception
    {
        double gp = 1;
        System.out.println(toMinusOneOneInterval(1.0  , gp));
        System.out.println(toMinusOneOneInterval(10.0 , gp));
        System.out.println(toMinusOneOneInterval(20.0 , gp));
        System.out.println(toMinusOneOneInterval(100.0, gp));
        System.out.println(toMinusOneOneInterval(  3.0, .45));
    }

    @Test
    public void getGrowthParameterTest() throws Exception
    {
        System.out.println(getGrowthParameter(10, .9));
        System.out.println(getGrowthParameter( 1, .9));
        System.out.println(getGrowthParameter(10, .5));
        System.out.println(getGrowthParameter(20, .9));
        
        assertThat(toMinusOneOneInterval( 10, getGrowthParameter( 10, .9)), closeTo(.9, .00001));
        assertThat(toMinusOneOneInterval(  1, getGrowthParameter(  1, .9)), closeTo(.9, .00001));
        assertThat(toMinusOneOneInterval( 10, getGrowthParameter( 10, .5)), closeTo(.5, .00001));
        assertThat(toMinusOneOneInterval(100, getGrowthParameter(100, .5)), closeTo(.5, .00001));
    }
    
    @Test
    public void testLogit()
    {
        System.out.println("logit(.5) = " + logit(.5));
        System.out.println("logit(0) = " + logit(0));
    }
    
    
    @Test
    public void ssl()
    {
//        {
//            double p = -1;
//            System.out.println("p = " + p);
//            System.out.println(asList(0d, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(0d, 0d, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, 0d, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, .1, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, .1, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.8, .8, .8, .8, .8), p));
//            System.out.println(asList(.9, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.9, .8, .8, .8, .8), p));
//            System.out.println();
//        }
//        {
//            double p = 0;
//            System.out.println("p = " + p);
//            System.out.println(asList(0d, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(0d, 0d, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, 0d, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, .1, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, .1, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.8, .8, .8, .8, .8), p));
//            System.out.println(asList(.9, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.9, .8, .8, .8, .8), p));
//            System.out.println();
//        }
//        {
//            double p = .1;
//            System.out.println("p = " + p);
//            System.out.println(asList(0d, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(0d, 0d, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, 0d, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, .1, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, .1, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.8, .8, .8, .8, .8), p));
//            System.out.println(asList(.9, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.9, .8, .8, .8, .8), p));
//            System.out.println();
//        }
//        {
//            double p = 1;
//            System.out.println("p = " + p);
//            System.out.println(asList(0d, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(0d, 0d, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, 0d, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, .1, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, .1, 0d, 0d, 0d), p));
//            System.out.println(asList(.8, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.8, .8, .8, .8, .8), p));
//            System.out.println(asList(.9, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.9, .8, .8, .8, .8), p));
//            System.out.println();
//        }
        {
            double p = 10;
            System.out.println("p = " + p);
            System.out.println(asList(0d, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(0d, 0d, 0d, 0d, 0d), p));
            System.out.println(asList(.8, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, 0d, 0d, 0d, 0d), p));
            System.out.println(asList(.8, .1, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, .1, 0d, 0d, 0d), p));
            System.out.println(asList(.8, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.8, .8, .8, .8, .8), p));
            System.out.println(asList(.9, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.9, .8, .8, .8, .8), p));
            System.out.println("1 - spsl(" + asList( 0, .6) + "): " + (1 - sigmoidPowerSumLogit(asList(0d, .6), p)));
            System.out.println("1 - spsl(" + asList(.6, .6) + "): " + (1 - sigmoidPowerSumLogit(asList(.6, .6), p)));
            System.out.println("spsl(" + asList(- 0, -.6) + "): " + sigmoidPowerSumLogit(asList(-0d, -.6), p));
            System.out.println("spsl(" + asList(-.6, -.6) + "): " + sigmoidPowerSumLogit(asList(-.6, -.6), p));
            System.out.println();
        }
        {
            double p = 100;
            System.out.println("p = " + p);
            System.out.println(asList(0d, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(0d, 0d, 0d, 0d, 0d), p));
            System.out.println(asList(.8, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, 0d, 0d, 0d, 0d), p));
            System.out.println(asList(.8, .1, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, .1, 0d, 0d, 0d), p));
            System.out.println(asList(.8, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.8, .8, .8, .8, .8), p));
            System.out.println(asList(.9, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.9, .8, .8, .8, .8), p));
            System.out.println();
        }
        {
            double p = 1000;
            System.out.println("p = " + p);
            System.out.println(asList(0d, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(0d, 0d, 0d, 0d, 0d), p));
            System.out.println(asList(.8, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, 0d, 0d, 0d, 0d), p));
            System.out.println(asList(.8, .1, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, .1, 0d, 0d, 0d), p));
            System.out.println(asList(.8, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.8, .8, .8, .8, .8), p));
            System.out.println(asList(.9, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.9, .8, .8, .8, .8), p));
            System.out.println();
        }
        {
            double p = 100000;
            System.out.println("p = " + p);
            System.out.println(asList(0d, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(0d, 0d, 0d, 0d, 0d), p));
            System.out.println(asList(.8, 0d, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, 0d, 0d, 0d, 0d), p));
            System.out.println(asList(.8, .1, 0d, 0d, 0d) + ": " + sigmoidPowerSumLogit(asList(.8, .1, 0d, 0d, 0d), p));
            System.out.println(asList(.8, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.8, .8, .8, .8, .8), p));
            System.out.println(asList(.9, .8, .8, .8, .8) + ": " + sigmoidPowerSumLogit(asList(.9, .8, .8, .8, .8), p));
            System.out.println();
        }
    }
}