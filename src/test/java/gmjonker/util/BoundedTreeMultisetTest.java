package gmjonker.util;

import org.junit.*;

public class BoundedTreeMultisetTest
{
    @Test
    public void test() throws Exception
    {
        BoundedTreeMultiset<Integer> set = new BoundedTreeMultiset<>(3);

        System.out.println("set = " + set); set.add(1);
        System.out.println("set = " + set); set.add(5);
        System.out.println("set = " + set); set.add(3);
        System.out.println("set = " + set); set.add(7);
        System.out.println("set = " + set); set.add(4);
        System.out.println("set = " + set); set.add(8);
        System.out.println("set = " + set); set.add(1);
        System.out.println("set = " + set); set.add(8);
        System.out.println("set = " + set); set.add(8);
        System.out.println("set = " + set); set.add(8);
    }
}