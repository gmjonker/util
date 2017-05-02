package gmjonker.math;

import org.junit.*;

import static gmjonker.math.SigmoidMath.*;
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
}