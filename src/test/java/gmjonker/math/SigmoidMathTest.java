package gmjonker.math;

import org.junit.*;

public class SigmoidMathTest
{
    @Test
    public void toMinusOneOneIntervalTest() throws Exception
    {
        double gp = 1;
        System.out.println(SigmoidMath.toMinusOneOneInterval(1.0  , gp));
        System.out.println(SigmoidMath.toMinusOneOneInterval(10.0 , gp));
        System.out.println(SigmoidMath.toMinusOneOneInterval(20.0 , gp));
        System.out.println(SigmoidMath.toMinusOneOneInterval(100.0, gp));

    }

}