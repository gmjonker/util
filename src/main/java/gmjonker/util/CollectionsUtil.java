package gmjonker.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gmjonker.math.GeneralMath.max;
import static gmjonker.math.NaType.getValueOr;
import static gmjonker.math.NaType.isValue;

public class CollectionsUtil
{
    protected static final LambdaLogger log = new LambdaLogger(CollectionsUtil.class);

    public static <T, R> List<R> map(List<T> list, Function<T, R> function)
    {
        return list.stream()
                .filter(o -> o != null)
                .map(function)
                .collect(Collectors.toList());
    }

    public static <T, R> R[] map(T[] inputArray, Function<T, R> function, Class outputClass)
    {
        @SuppressWarnings("unchecked")
        R[] outputArray = (R[]) Array.newInstance(outputClass, inputArray.length);
        for (int i = 0; i < inputArray.length; i++)
             outputArray[i] = function.apply(inputArray[i]);
        return outputArray;
    }

    public static double[] map(double[] inputArray, Function<Double, Double> function)
    {
        double[] outputArray = new double[inputArray.length];
        for (int i = 0; i < inputArray.length; i++)
             outputArray[i] = function.apply(inputArray[i]);
        return outputArray;
    }

    public static <T, R> Set<R> map(Set<T> set, Function<T, R> function)
    {
        return set.stream()
                .filter(o -> o != null)
                .map(function)
                .collect(Collectors.toSet());
    }

    public static <T> List<T> filter(List<T> list, Function<T, Boolean> function)
    {
        return list.stream().filter(function::apply).collect(Collectors.toList());
    }

    /**
     * Returns the results of pairwise applying {@code function} on the elements of {@code list1} and {@code list2}.
     */
    public static <L1, L2, R> List<R> zipWith(List<L1> list1, List<L2> list2, BiFunction<L1, L2, R> function)
    {
        assert list1.size() == list2.size() : "zipWith: lists must be equal size";

        List<R> result = new ArrayList<>();
        for (int i = 0; i < list1.size(); i++) {
            L1 element1 = list1.get(i);
            L2 element2 = list2.get(i);
            result.add(i, function.apply(element1, element2));
        }
        return result;
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... objects)
    {
        Set<T> set = new HashSet<>();
        Collections.addAll(set, objects);
        return set;
    }

    @SafeVarargs
    public static <T> List<T> asArrayList(T... objects)
    {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, objects);
        return list;
    }

    public static double[] toPrimitiveDoubleArray(List<Double> doubleList)
    {
        double[] doubles = new double[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            Double aDouble = doubleList.get(i);
            doubles[i] = aDouble;
        }
        return doubles;
    }


    /**
     * Sublist of list from fromIndex, inclusive, to toIndex, exclusive.
     * Always returns a list. If fromIndex or toIndex is null, 0 or list.size() is used respectively.
     * Out of bounds indices are brought within range. Indices -1, -2, etc. may be used for
     * list.size()-1, list.size()-2, etc.
     */
    @Nonnull
    public static <T> List<T> sublist(@Nonnull List<T> list, @Nullable Integer fromIndex, @Nullable Integer toIndex)
    {
        int size = list.size();
        if (fromIndex == null) fromIndex = 0;
        if (toIndex == null) toIndex = size;
        if (fromIndex < 0) fromIndex = max(size + fromIndex, 0);
        if (toIndex < 0) toIndex = max(size + toIndex, 0);
        if (toIndex > size) toIndex = size;
        if (fromIndex >= toIndex)
            return Collections.emptyList();
        return list.subList(fromIndex, toIndex);
    }

    /**
     * Selects the elements from {@code list} for which their corresponding element in {@code mask} is true.
     */
    public static <T> List<T> mask(List<T> list, List<Boolean> mask)
    {
        List<T> maskedList = new ArrayList<>();
        for (int i = 0, listSize = list.size(); i < listSize; i++) {
            T element = list.get(i);
            Boolean bool = mask.get(i);
            if (bool)
                maskedList.add(element);
        }
        return maskedList;
    }

    public static <K,V> List<V> mapGetAll(Map<K,V> map,  List<K> keys)
    {
        List<V> values = new ArrayList<>();
        for (K key : keys) {
            V value = map.get(key);
            if (value == null)
                log.debug("Key {} not found in map", key);
            else
                values.add(value);
        }
        return values;
    }

    /**
     * Sorts a map by value. Adapted from http://stackoverflow.com/a/2581754/1901037
     *
     * @return A map that, when iterated over, returns keys, values or entries sorted by value
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, boolean ascending)
    {
        Map<K,V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K,V>> st = map.entrySet().stream();

        Comparator<Map.Entry<K, V>> comparator = Comparator.comparing(Map.Entry::getValue);
        if ( ! ascending)
            comparator = comparator.reversed();

        st.sorted(comparator).forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    /**
     * Sorts a map by a function on its values. Adapted from http://stackoverflow.com/a/2581754/1901037
     *
     * @return A map that, when iterated over, returns keys, values or entries sorted by value
     */
    public static <K, V> Map<K, V> sortMapByValue(Map<K, V> map, Function<V, Comparable> function, boolean ascending)
    {
        Map<K,V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K,V>> entries = map.entrySet().stream();

        Comparator<Map.Entry<K, V>> comparator = Comparator.comparing(
                (Function<Map.Entry<K, V>, Comparable>) (kvEntry) -> function.apply(kvEntry.getValue())
        );
        if ( ! ascending)
            comparator = comparator.reversed();

        entries.sorted(comparator).forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    /**
     * Sorts a Map by a function on its value, descendingly. Can handle NA.
     *
     * <p>Example usage:</p>
     * <pre>
     *  Map&lt;Integer, ContentBasedScore&gt; sortedContentBasedScoreColumn =
     *      sortMap(recommendation.contentBasedScoreColumn, cbs -> cbs.contentBased);
     * </pre>
     * @return New hash map, sorted.
     */
    public static <K, V> Map<K, V> sortMap(Map<K, V> map, Function<V, Double> function)
    {
        Comparator<Map.Entry<K, V>> comparator =
                Comparator.comparing(
                        (Function<Map.Entry<K, V>, Double>) (kvEntry) -> {
                            Double value = function.apply(kvEntry.getValue());
                            if (!isValue(value))
                                value = Double.MIN_VALUE;
                            return value;
                        }
                ).reversed();

        Stream<Map.Entry<K,V>> entries = map.entrySet().stream();
        Map<K,V> result = new LinkedHashMap<>();
        entries.sorted(comparator).forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    /**
     * Sorts a Map by a function on its value, ascendingly.
     *
     * <p>Example usage:</p>
     * <pre>
     *  Map&lt;Integer, ContentBasedScore&gt; sortedContentBasedScoreColumn =
     *      sortMap(recommendation.contentBasedScoreColumn, cbs -> cbs.contentBasedScore);
     * </pre>
     *
     * @return List of keys
     */
    public static <K,V> List<K> sortMapByValueFunctionAscending(Map<K, V> map, Function<V, Double> function)
    {
        Comparator<Map.Entry<K, V>> comparator =
                Comparator.comparingDouble(e -> getValueOr(function.apply(e.getValue()), Double.MIN_VALUE));
        return map.entrySet().stream()
                .sorted()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Sorts a Map by a function on its value, descendingly.
     *
     * <p>Example usage:</p>
     * <pre>
     *  Map&lt;Integer, ContentBasedScore&gt; sortedContentBasedScoreColumn =
     *      sortMap(recommendation.contentBasedScoreColumn, cbs -> cbs.contentBasedScore);
     * </pre>
     *
     * @return List of keys
     */
    public static <K,V> List<K> sortMapByValueFunctionDescending(Map<K, V> map, Function<V, Double> function)
    {
        Comparator<Map.Entry<K, V>> comparator =
                Comparator.comparingDouble(e -> getValueOr(function.apply(e.getValue()), Double.MIN_VALUE));
        return map.entrySet().stream()
                .sorted(comparator.reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
