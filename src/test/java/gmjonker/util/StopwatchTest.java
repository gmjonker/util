package gmjonker.util;

import org.junit.*;

public class StopwatchTest
{
    @Test
    public void test()
    {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Util.simpleSleep(20);
        stopwatch.mark("A");
        Util.simpleSleep(30);
        stopwatch.mark("B");
        Util.simpleSleep(40);
        stopwatch.mark("C");
        stopwatch.stop();
        
//        Assert.assertThat(stopwatch.elapsedBetween("A", "B"), closeTo(30 * MILIS_TO_NANOS, 5 * MILIS_TO_NANOS));
        System.out.println(stopwatch.elapsedBetween("A", "B"));
        System.out.println(stopwatch.elapsedBetween("B", "C"));
        System.out.println(stopwatch.elapsedBetween("A", "C"));
        System.out.println(stopwatch.elapsedBetweenToString("A", "B"));
        System.out.println(stopwatch.elapsedBetweenToString("B", "C"));
        System.out.println(stopwatch.elapsedBetweenToString("A", "C"));
    }
}