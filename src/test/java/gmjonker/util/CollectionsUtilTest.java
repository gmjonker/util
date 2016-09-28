package gmjonker.util;

import org.junit.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class CollectionsUtilTest
{
    @Test
    public void sublist()
    {
        assertThat(CollectionsUtil.sublist(asList(1, 2, 3, 4, 5), 2, 4), contains(3, 4));
        assertThat(CollectionsUtil.sublist(asList(1, 2, 3, 4, 5), 2, 7), contains(3, 4, 5));
        assertThat(CollectionsUtil.sublist(asList(1, 2, 3, 4, 5), -2, 7), contains(4, 5));
        assertThat(CollectionsUtil.sublist(asList(1, 2, 3, 4, 5), 3, null), contains(4, 5));
        assertThat(CollectionsUtil.sublist(asList(1, 2, 3, 4, 5), null, -2), contains(1, 2, 3));
        assertThat(CollectionsUtil.sublist(asList(1, 2, 3, 4, 5), 4, 2), empty());
        assertThat(CollectionsUtil.sublist(asList(1, 2, 3, 4, 5), 4, 6), contains(5));
        assertThat(CollectionsUtil.sublist(asList(1, 2, 3, 4, 5), 5, 6), empty());
        assertThat(CollectionsUtil.sublist(asList(), 4, 2), empty());
        assertThat(CollectionsUtil.sublist(asList(), 4, 2), empty());
    }

    @Test
    public void take()
    {
        Assert.assertThat(CollectionsUtil.take(asList(1, 2, 3, 4, 5, 6, 7, 8), 3), contains(1, 2, 3));
        Assert.assertThat(CollectionsUtil.take(asList(1, 2), 3), contains(1, 2));
        Assert.assertThat(CollectionsUtil.take(asList(), 3), empty());
    }

    // Test taken from http://stackoverflow.com/a/2581754/1901037
    @Test
    public void sortMapByValue()
    {
        Random random = new Random(System.currentTimeMillis());
        Map<String, Integer> testMap = new HashMap<String, Integer>(1000);
        for(int i = 0 ; i < 1000 ; ++i) {
            testMap.put( "SomeString" + random.nextInt(), random.nextInt());
        }

        testMap = CollectionsUtil.sortMapByValue(testMap, true);
        Assert.assertEquals(1000, testMap.size());

        Integer previous = null;
        for(Map.Entry<String, Integer> entry : testMap.entrySet()) {
            Assert.assertNotNull( entry.getValue() );
            if (previous != null)
                assertTrue(entry.getValue() >= previous);
            previous = entry.getValue();
        }

        previous = null;
        for(Integer value : testMap.values()) {
            Assert.assertNotNull(value);
            if (previous != null)
                assertTrue(value >= previous);
            previous = value;
        }
    }

    @Test
    public void map()
    {
        DefaultingHashmap<Integer, Double> map = new DefaultingHashmap<>(new Double(2));
        map.put(1, 1.1);
        map.put(2, 2.2);
        DefaultingHashmap<Integer, Double> mappedmap = CollectionsUtil.map(map, integer -> integer + 2, dbl -> dbl + .2);
        System.out.println("mappedmap = " + mappedmap);
    }

    @Test
    public void shuffleDeeMap()
    {
        LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < 100; i++) {
            map.put(i, i);
        }
        System.out.println("map = " + map);
        Map<Integer, Integer> shuffledMap = CollectionsUtil.shuffleMap(map);
        System.out.println("map = " + shuffledMap);
    }

}