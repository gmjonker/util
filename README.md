  
# gmjonker/util  
  
Various utilities.  
  
[GeneralMath](src/main/java/gmjonker/math/GeneralMath.java) 

```java
public static double sum(double... values)
public static double min(double... values)  
public static double max(double... values)
public static <V> double minBy(Iterable<V> iterable, Function<V, Double> valueExtractor)  
public static <V> double maxBy(Iterable<V> iterable, Function<V, Double> valueExtractor)  
public static <T> double sumBy(Collection<T> coll, Function<T, Double> mapper)  
public static double weightedMean(double[] values, double[] weights)  
public static double powerMean(double[] values, double exponent)  
public static double rootMeanSquare(double[] values)  
public static double rootWeightedMeanSquare(List<Double> values, List<Double> weights)  
public static double exponentialMovingAverageV1(double[] values, double alpha)  
...  
...  
```
  
[CollectionsUtil](src/main/java/gmjonker/util/CollectionsUtil.java)  
  
    public static <T, R> Iterable<R> map(Iterable<T> collection, Function<T, R> function)  
    public static <T, R> Collection<R> map(Collection<T> collection, Function<T, R> function)  
    public static <T, R> List<R> map(List<T> list, Function<T, R> function)  
    public static <K1,V1,K2,V2> Map<K2,V2> map(Map<K1,V1> map, Function<K1,K2> keyMapper, Function<V1,V2> valueMapper)  
    public static <K1,V1,K2,V2> DefaultingHashmap<K2,V2> map(DefaultingMap<K1,V1> defaultingMap, Function<K1,K2> keyMapper, Function<V1,V2> valueMapper)  
    public static <T> List<T> filter(List<T> list, Function<T, Boolean> function)  
    public static <T> List<T> remove(List<T> list, Function<T, Boolean> function)  
    public static <T> List<T> filterNulls(List<T> list)  
    public static <K, V> Map<K, V> filter(Map<K, V> map, Function<K, Boolean> keyFilter)  
    public static <K, V> Map<K, V> filter(Map<K, V> map, Function<K, Boolean> keyFilter, Function<V, Boolean> valueFilter)  
    public static <T> boolean hasItemSatisfying(Collection<T> collection, Function<T, Boolean> condition)  
    public static <L1, L2, R> List<R> zipWith(List<L1> list1, List<L2> list2, BiFunction<L1, L2, R> function)  
    public static <K, V> Map<V, K> invertMap(Map<K, V> map)  
    public static <K, V> Multimap<V, K> invertMultimap(Multimap<K, V> map)  
    public static <T> List<T> take(List<T> list, int max)  
    public static <T extends Comparable<? super T>> List<T> sortAsc(Collection<T> collection)   
    public static <T extends Comparable<? super T>> List<T> sortDesc(Collection<T> collection)   
    public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValueAscending(Map<K, V> map)  
    public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValueDescending(Map<K, V> map)  
    public static <K, V> Map<K, V> shuffleMap(Map<K, V> map)  
    public static <T> List<Multiset.Entry<T>> sortMultisetByCounts(Multiset<T> multiSet)  
    public static <T> Collection<T> max(Collection<T> coll, Comparator<T> comparator)  
    public static boolean isEqualCollection(Collection<?> a, Collection<?> b)  
    public static <E> E randomElement(Collection<? extends E> coll, Random rand)  
    ...  
    ...  
  
[BoundedTreeSet](src/main/java/gmjonker/util/BoundedTreeSet.java)  
[BoundedTreeMultiset](src/main/java/gmjonker/util/BoundedTreeMultiset.java)  
[MultiTable](src/main/java/cn/yxffcode/freetookit/collection/MultiTable.java)    
[ConcurrentMultimap](src/main/java/com/google/common/collect/ConcurrentMultimap.java)  
[DefaultingHashBasedTable](src/main/java/gmjonker/util/DefaultingHashBasedTable.java)  
[DefaultingMap](src/main/java/gmjonker/util/DefaultingMap.java)  
[GenericTrie](src/main/java/gmjonker/util/GenericTrie.java)  
  
[IoUtil](src/main/java/gmjonker/util/IoUtil.java)  
  
    public static List<String> readFile(String name) throws IOException  
    public static Stream<String> getFileAsStreamOfLines(String name) throws IOException  
    public static void writeToFile(String string, String filename) throws IOException  
    public static CSVParser readCsvFile(String fileName, boolean hasHeaders) throws IOException  
    public static Table<String, String, String> readCsvIntoTable(String fileName) throws IOException  
    public static LinkedHashMap<String, String> readTwoColumnCsvIntoMap(String fileName) throws IOException  
    public static <T> void writeCollectionToCsv(String filename, Collection<T> collection, Function<T, String>... columnMappers) throws IOException  
    public static <K, V> void writeMapToCsv(Map<K, V> map, String fileName) throws IOException  
    public static LinkedListMultimap<String, String> readCsvIntoMultimapOrRTE(String fileName, boolean hasHeaders, int keyColumn, int valueColumn)  
    public static <R, C, V> void writeTableToCsv(Table<R, C, V> table, String fileName) throws IOException  
    public static List<String> getFilenamesInDirectory(String directoryName) throws IOException  
    public static int countLines(File file) throws IOException  
    ...  
    ...  
  
  
[FormattingUtil](src/main/java/gmjonker/util/FormattingUtil.java)  
  
    public static String rounded(double d, int decimals)  
    public static String asPercentage(Double d)  
    public static String toHumanReadableNumber(int number)  
    public static String nanosToString(long nanos)  
    public static String durationToString(Duration duration)  
    public static <T> String toStringLineByLine(Iterable<T> collection)  
    public static <K,V> String toStringLineByLine(Map<K,V> map)  
    public static String toWidth(String string, int width)  
    public static String prettyJson(String json)  
    public static <R, C, V> String tableToString(Table<R, C, V> table)  
    ...  
  
  
[StringNormalization](src/main/java/gmjonker/util/StringNormalization.java)  
  
    public static String removeUnprintableCharacters(String myString)  
    public static String removePunctuation(String text)  
    public static String flattenToAscii(String string)  
    ...  
  
  
[TriFunction](src/main/java/gmjonker/util/TriFunction.java)   
[TextHistogram](src/main/java/gmjonker/math/TextHistogram.java)  
[TextPlot](src/main/java/gmjonker/math/TextPlot.java)  
[DaemonThreadFactory](src/main/java/gmjonker/util/DaemonThreadFactory.java)  
  
[Util](src/main/java/gmjonker/util/Util.java)  
  
    public static String getConciseMemoryInfo()  
    public static boolean isEnvSet(String name)  
    public static String getEnv(String name)  
    public static String getEnvOrDefault(String name, String defaultValue)  
    public static int getEnvOrDefault(String name, int defaultValue)  
    public static URL getEnvOrDefault(String name, URL defaultValue)  
    public static void continuationPrompt(String message)  
    public static String executeCommandAndCaptureResult(String command) throws IOException  
    public static String getCurrentGitBranch()  
    public static boolean isAssertionsEnabled()  
    ...  
  
[LambdaLogger](src/main/java/gmjonker/util/LambdaLogger.java)  (Wrapper around logback)
  
### Hamcrest matchers:  
  
[IsSortedByMatcher](src/main/java/gmjonker/matchers/IsSortedByMatcher.java)    
[IsSortedMatcher](src/main/java/gmjonker/matchers/IsSortedMatcher.java)    
[IsValidDoubleMatcher](src/main/java/gmjonker/matchers/IsValidDoubleMatcher.java)    
[IsValidFloatMatcher](src/main/java/gmjonker/matchers/IsValidFloatMatcher.java)    
[LambdaMatcher](src/main/java/gmjonker/matchers/LambdaMatcher.java)    
    ...  
  
  