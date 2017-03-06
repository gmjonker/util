package gmjonker.util;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.*;

import static org.junit.Assert.*;

public class BoundedTreeSetTest
{
    @Test
    public void test()
    {
        {
            BoundedTreeSet<String> bts = new BoundedTreeSet<>(3);
            bts.add("Geert");
            bts.add("Annet");
            bts.add("Leo");
            bts.add("Jetty");
            bts.add("Jos");
            bts.add("Misja");
            bts.add("Hanna");
            System.out.println("bts = " + bts);
        }
        {
            BoundedTreeSet<Pair<Double, String>> bts = new BoundedTreeSet<>(3);
            bts.add(Pair.of(1.0,"Geert"));
            bts.add(Pair.of(5.0,"Annet"));
            bts.add(Pair.of(2.0,"Leo"));
            bts.add(Pair.of(8.0,"Jetty"));
            bts.add(Pair.of(4.0,"Jos"));
            bts.add(Pair.of(9.0,"Misja"));
            bts.add(Pair.of(3.0,"Hanna"));
            System.out.println("bts = " + bts);
        }
    }
}