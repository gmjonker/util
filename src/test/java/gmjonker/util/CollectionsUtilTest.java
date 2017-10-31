package gmjonker.util;

import cn.yxffcode.freetookit.collection.MultiTable;
import cn.yxffcode.freetookit.collection.MultiTables;
import com.google.common.collect.Table;
import gmjonker.math.GeneralMath;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.util.*;
import java.util.List;

import static gmjonker.math.GeneralMath.sum_i;
import static gmjonker.util.CollectionsUtil.filterByType;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class CollectionsUtilTest
{
    @Test
    public void filterByTyp()
    {
        List<Component> list = asList(
                new Label("A"),
                new Button("B"),
                new Canvas()
        );
        List<Label> labels = filterByType(list, Label.class);
        System.out.println("labels = " + labels);
    }
    
    @Test
    public void asMapp()
    {
        LinkedHashMap<Integer, Integer> map = CollectionsUtil.asMap(1, 2, 3, 4);
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            System.out.println("entry = " + entry);
        }
        System.out.println();
        LinkedHashMap<Integer, Integer> map2 = CollectionsUtil.asMap(1, 2, 3, 4, 5, 6, 7, 8);
        for (Map.Entry<Integer, Integer> entry : map2.entrySet()) {
            System.out.println("entry = " + entry);
        }
    }

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
    public void sortMapByValueThenKey()
    {
        Random random = new Random(System.currentTimeMillis());
        Map<Integer, Integer> testMap = new HashMap<>(1000);
        for(int i = 0 ; i < 1000 ; ++i) {
            testMap.put( random.nextInt(20), random.nextInt(20));
        }
        System.out.println("testMap = " + FormattingUtil.toStringLineByLine(testMap));

        testMap = CollectionsUtil.sortMapByValueThenKey(testMap);
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("testMap = " + FormattingUtil.toStringLineByLine(testMap));
    }

    @Test
    public void sortMapByValueThenKeyMapped()
    {
        Random random = new Random(System.currentTimeMillis());
        Map<Integer, Integer> testMap = new HashMap<>(1000);
        for(int i = 0 ; i < 1000 ; ++i) {
            testMap.put( random.nextInt(20), random.nextInt(20));
        }
        System.out.println("testMap = " + FormattingUtil.toStringLineByLine(testMap));

        testMap = CollectionsUtil.sortMapByValueThenKey(testMap, x -> -x, x -> -x);
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("testMap = " + FormattingUtil.toStringLineByLine(testMap));
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
    public void reduceMultiTable()
    {
        MultiTable<Integer, String, Integer> multiTable = MultiTables.newListHashMultiTable();
        multiTable.put(1, "1", 1);
        multiTable.put(1, "2", 12);
        multiTable.put(1, "2", 120);
        multiTable.put(1, "2", 1200);
        multiTable.put(2, "1", 21);
        multiTable.put(2, "1", 21);

        System.out.println("multiTable = " + multiTable);

        Table<Integer, String, Integer> table = CollectionsUtil.reduce(multiTable, GeneralMath::sum_i);

        System.out.println("table = " + table);
        assertThat(table.get(1, "1"), equalTo(sum_i(multiTable.get(1, "1"))));
        assertThat(table.get(1, "2"), equalTo(sum_i(multiTable.get(1, "2"))));
        assertThat(table.get(2, "1"), equalTo(sum_i(multiTable.get(2, "1"))));
        assertThat(table.get(2, "2"), nullValue());
        assertThat(table.size(), equalTo(3));
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
    
    @Test
    public void maxess()
    {
        System.out.println("strings = " + CollectionsUtil.max(
                asList("asdf", "qwer", "zxcv", "qwer", "zxcv", "zxc"), Comparator.naturalOrder()));
        System.out.println("strings = " + CollectionsUtil.max(
                asList("asdf", "qwer", "zxcv", "qwer", "zxcv", "zxc"), Comparator.reverseOrder()));
    }

}