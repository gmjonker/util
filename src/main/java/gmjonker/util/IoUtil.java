package gmjonker.util;

import com.google.common.collect.*;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * - 'vanilla' methods throw an exception if something goes wrong
 * - tryX methods log an error if something goes wrong and return an empty object.
 * - XorRTE methods throw a runtime exception if something goes wrong. Handy for use in field declarations.
 */
@SuppressWarnings("WeakerAccess")
public class IoUtil
{
    protected static final LambdaLogger log = new LambdaLogger(IoUtil.class);

    public static List<String> readFile(String name) throws IOException
    {
        return getFileAsStreamOfLines(name).collect(Collectors.toList());
    }

    public static List<String> tryReadFile(String name)
    {
        try {
            return readFile(name);
        } catch (Exception e) {
            log.error("Error while reading file {}", name, e);
        }
        return Collections.emptyList();
    }

    public static List<String> readFileOrRTE(String name)
    {
        try {
            return readFile(name);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String readFileAsOneString(String name) throws IOException
    {
        return getFileAsStreamOfLines(name).collect(Collectors.joining(System.lineSeparator()));
    }

    private static Stream<String> getFileAsStreamOfLines(String name) throws IOException
    {
        log.debug("Getting file {}", name);
        BufferedReader reader;
        // This way of getting to a resource seems to work in Docker, Maven and Intellij IDEA.
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (resourceAsStream != null) {
            reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        } else {
            log.trace("Could not find resource '{}', will now attempt to read in working directory '{}'...", name,
                    System.getProperty("user.dir"));
            File file = new File(name);
            if ( ! file.exists()) {
                log.warn("Couldn't find find resource '{}' in the resource folder(s) or the working directory", name);
                throw new IOException("Could not find resource '" + name + "'");
            } else {
                reader = new BufferedReader(new FileReader(file));
            }
        }
        return reader.lines();
    }

    public static void writeToFile(Set<String> strings, String filename) throws IOException
    {
        @Cleanup FileWriter writer = new FileWriter(filename);
        for (String str: strings)
            writer.write(str + "\n");
    }

    //
    // CSV
    ///

    public static CSVParser readCsvFileWithHeaders(String fileName) throws IOException
    {
        String fileContent = readFileAsOneString(fileName);
        return CSVParser.parse(fileContent, CSVFormat.EXCEL.withHeader().withAllowMissingColumnNames()
                .withIgnoreEmptyLines().withIgnoreSurroundingSpaces());
    }

    public static CSVParser readCsvFileWithoutHeaders(String fileName) throws IOException
    {
        String fileContent = readFileAsOneString(fileName);
        return CSVParser.parse(fileContent, CSVFormat.EXCEL.withIgnoreEmptyLines().withIgnoreSurroundingSpaces());
    }

    public static CSVParser readCsvFileWithHeadersOrRTE(String fileName)
    {
        try {
            return readCsvFileWithHeaders(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public static CSVParser readCsvFileWithoutHeadersSneaky(String fileName)
    {
        System.out.println("sneaky");
        String fileContent = readFileAsOneString(fileName);
        return CSVParser.parse(fileContent, CSVFormat.EXCEL.withIgnoreEmptyLines().withIgnoreSurroundingSpaces());
    }

    /**
     * Reads from a CSV file that has row and column headers.
     */
    public static Table<String, String, String> readCsvIntoTable(String fileName) throws IOException
    {
        return _readCsvIntoTable(fileName, o -> o, o -> o, o -> o, DefaultingHashBasedTable.create(null));
    }

    /**
     * Reads from a CSV file that has row and column headers.
     */
    public static DefaultingHashBasedTable<String, String, String> readCsvIntoTableOrRTE(String fileName)
    {
        try {
            return _readCsvIntoTable(fileName, o -> o, o -> o, o -> o, DefaultingHashBasedTable.create(null));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads from a CSV file that has row and column headers.
     */
    public static <R, C, T> Table<R, C, T> readCsvIntoTable(String fileName, Function<String, R> rowTypeMapper,
            Function<String, C> columnTypeMapper, Function<String, T> cellTypeMapper) throws IOException
    {
        return _readCsvIntoTable(fileName, rowTypeMapper, columnTypeMapper, cellTypeMapper, DefaultingHashBasedTable.create(null));
    }

    /**
     * Reads from a CSV file that has row and column headers.
     */
    public static <R, C, T> Table<R, C, T> tryReadCsvIntoTable(String fileName, Function<String, R> rowTypeMapper,
            Function<String, C> columnTypeMapper, Function<String, T> cellTypeMapper)
    {
        try {
            return _readCsvIntoTable(fileName, rowTypeMapper, columnTypeMapper, cellTypeMapper, DefaultingHashBasedTable.create(null));
        } catch (IOException e) {
            return HashBasedTable.create();
        }
    }

    /**
     * Reads from a CSV file that has row and column headers.
     */
    public static <R, C, T> Table<R, C, T> readCsvIntoTableOrRTE(String fileName, Function<String, R> rowTypeMapper,
            Function<String, C> columnTypeMapper, Function<String, T> cellTypeMapper)
    {
        try {
            return _readCsvIntoTable(fileName, rowTypeMapper, columnTypeMapper, cellTypeMapper, DefaultingHashBasedTable.create(null));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads from a CSV file that has row and column headers.
     */
    public static <R, C, T> DefaultingHashBasedTable<R, C, T> readCsvIntoDefaultingTable(String fileName,
            Function<String, R> rowTypeMapper, Function<String, C> columnTypeMapper, Function<String, T> cellTypeMapper,
            T defaultValue) throws IOException
    {
        return _readCsvIntoTable(fileName, rowTypeMapper, columnTypeMapper, cellTypeMapper, DefaultingHashBasedTable.create(defaultValue));
    }

    private static <R, C, T> DefaultingHashBasedTable<R, C, T> _readCsvIntoTable(String fileName,
            Function<String, R> rowTypeMapper, Function<String, C> columnTypeMapper, Function<String, T> cellTypeMapper,
            DefaultingHashBasedTable<R, C, T> table) throws IOException
    {
        CSVParser csvParser = readCsvFileWithHeaders(fileName);
        Set<String> headers = csvParser.getHeaderMap().entrySet().stream()
                .filter(entry -> entry.getValue() > 0) // skip the first column, it contains row headers
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        for (CSVRecord record : csvParser.getRecords()) {
            String rowHeader = record.get(0);
            R r = rowTypeMapper.apply(rowHeader);
            for (String header : headers) {
                C c = columnTypeMapper.apply(header);
                String cell = record.get(header);
                T t = cellTypeMapper.apply(cell);
                if (t != null) {
                    table.put(r, c, t);
                }
            }
        }
        csvParser.close();
        return table;
    }

    /** CSV file must not have headers. **/
    public static LinkedHashMap<String, String> readTwoColumnCsvIntoMap(String fileName) throws IOException
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        CSVParser csvParser = readCsvFileWithoutHeaders(fileName);
        for (CSVRecord record : csvParser.getRecords()) {
            String key   = record.get(0);
            String value = record.get(1);
            map.put(key, value);
        }
        return map;
    }

    /** CSV file must not have headers. **/
    public static LinkedHashMap<String, String> readTwoColumnCsvIntoMapOrRTE(String fileName)
    {
        try {
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            CSVParser csvParser = readCsvFileWithoutHeaders(fileName);
            for (CSVRecord record : csvParser.getRecords()) {
                String key = record.get(0);
                String value = record.get(1);
                map.put(key, value);
            }
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** CSV file must not have headers. **/
    public static <K, V> LinkedHashMap<K,V> readTwoColumnCsvIntoMap(String fileName, Function<String, K> keyTransform,
            Function<String, V> valueTransform) throws IOException
    {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        CSVParser csvParser = readCsvFileWithoutHeaders(fileName);
        for (CSVRecord record : csvParser.getRecords()) {
            K key = keyTransform.apply(record.get(0));
            V value = valueTransform.apply(record.get(1));
            map.put(key, value);
        }
        return map;
    }

    /** CSV file must not have headers. **/
    public static <K, V> LinkedHashMap<K,V> readTwoColumnCsvIntoMapIgnoreErrors(String fileName, Function<String, K> keyTransform,
            Function<String, V> valueTransform) throws IOException
    {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        CSVParser csvParser = readCsvFileWithoutHeaders(fileName);
        for (CSVRecord record : csvParser.getRecords()) {
            try {
                K key = keyTransform.apply(record.get(0));
                V value = valueTransform.apply(record.get(1));
                map.put(key, value);
            } catch (Exception e) {
                log.warnOnce("Could not process record, continuing with next record ('{}')", record, e);
            }
        }
        return map;
    }

    /** CSV file must not have headers. **/
    public static <K, V> LinkedHashMap<K,V> readTwoColumnCsvIntoMapOrEmptyMap(String fileName,
            Function<String, K> keyTransform, Function<String, V> valueTransform)
    {
        try {
            return readTwoColumnCsvIntoMap(fileName, keyTransform, valueTransform);
        } catch (IOException e) {
            log.error("Could not read file '{}'", fileName,  e);
            return new LinkedHashMap<>();
        }
    }

    /** CSV file must not have headers. **/
    public static <K, V> LinkedHashMap<K,V> readTwoColumnCsvIntoMapOrEmptyMapIgnoreErrorsOrEmptyMap(String fileName,
            Function<String, K> keyTransform, Function<String, V> valueTransform)
    {
        try {
            return readTwoColumnCsvIntoMapIgnoreErrors(fileName, keyTransform, valueTransform);
        } catch (IOException e) {
            log.error("Could not read file '{}'", fileName,  e);
            return new LinkedHashMap<>();
        }
    }

    /** CSV file must not have headers. **/
    public static <K, V> LinkedHashMap<K,V> readTwoColumnCsvIntoMapOrRTE(String fileName,
            Function<String, K> keyTransform, Function<String, V> valueTransform)
    {
        try {
            return readTwoColumnCsvIntoMap(fileName, keyTransform, valueTransform);
        } catch (IOException e) {
            log.error("Could not read file '{}'", fileName,  e);
            throw new RuntimeException(e);
        }
    }

    /** CSV file must have headers. **/
    public static <K, V> LinkedHashMap<K,V> readTwoColumnsOfCsvIntoMap(String fileName, String keyColumn, String valueColumn,
            Function<String, K> keyTransform, Function<String, V> valueTransform) throws IOException
    {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        CSVParser csvParser = readCsvFileWithHeaders(fileName);
        for (CSVRecord record : csvParser.getRecords()) {
            try {
                K key = keyTransform.apply(record.get(keyColumn));
                V value = valueTransform.apply(record.get(valueColumn));
                map.put(key, value);
            } catch (Exception e) {
                log.warn("Error in record '{}'", record);
                throw e;
            }
        }
        return map;
    }

    /** CSV file must have headers. **/
    public static <K, V> LinkedHashMap<K,V> readTwoColumnsOfCsvIntoMapOrRTE(String fileName, String keyColumn, String valueColumn,
            Function<String, K> keyTransform, Function<String, V> valueTransform)
    {
        try {
            return readTwoColumnsOfCsvIntoMap(fileName, keyColumn, valueColumn, keyTransform, valueTransform);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



    /** CSV is assumed to have no headers. **/
    public static ArrayListMultimap<String, String> readCsvIntoMultimapOrRTE(String fileName)
    {
        return readCsvIntoMultimapOrRTE(fileName, k -> k, v -> v);
    }

    public static <K, V> ArrayListMultimap<K, V> readCsvIntoMultimapOrRTE(String fileName, Function<String, K> keyMapper,
            Function<String, V> valueMapper)
    {
        return readCsvIntoMultimapOrRTE(fileName, keyMapper, valueMapper, s -> true);
    }
    
    public static <K, V> ArrayListMultimap<K, V> readCsvIntoMultimapOrRTE(String fileName, Function<String, K> keyMapper,
            Function<String, V> valueMapper, Predicate<String> valueFilter)
    {
        try {
            ArrayListMultimap<K, V> map = ArrayListMultimap.create();
            CSVParser csvParser = readCsvFileWithoutHeaders(fileName);
            for (CSVRecord record : csvParser.getRecords()) {
                String key = record.get(0);
                for (int i = 1; i < record.size(); i++) {
                    String value = record.get(i);
                    if ( ! valueFilter.test(value))
                        continue;
                    log.trace("readCsvIntoMultimapOrRTE: key: '{}', value: '{}'", key, value);
                    map.put(keyMapper.apply(key), valueMapper.apply(value));
                }
            }
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <K, V> void writeMultimapToCsv(Multimap<K, V> multimap, String fileName) throws IOException
    {
        @Cleanup CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(fileName), CSVFormat.EXCEL);
        for (K key : multimap.keySet()) {
            csvPrinter.print(key);
            for (V value : multimap.get(key)) {
                csvPrinter.print(value);
            }
            csvPrinter.println();
        }
        csvPrinter.close();
    }





    public static <R, C, V> void writeTableToCsv(Table<R, C, V> table, String fileName) throws IOException
    {
        writeTableToCsv(table, fileName, FormattingUtil::toStringer);
    }

    public static <R, C, V> void writeTableToCsv(Table<R, C, V> table, String fileName, Function<V, String> valueTransformer) throws IOException
    {
        writeTableToCsv(table, fileName, FormattingUtil::toStringer, FormattingUtil::toStringer, valueTransformer);
    }

    public static <R, C, V> void writeTableToCsv(Table<R, C, V> table, String fileName, 
            BiFunction<R, C, Function<V, String>> valueTransformer) throws IOException
    {
        writeTableToCsv(table, fileName, FormattingUtil::toStringer, FormattingUtil::toStringer, valueTransformer);
    }

    public static <R, C, V> void writeTableToCsv(Table<R, C, V> table, String fileName, Function<R, String> rowHeaderTransformer,
            Function<C, String> columnHeaderTransformer, Function<V, String> valueTransformer) throws IOException
    {
        writeTableToCsv(table, fileName, rowHeaderTransformer, columnHeaderTransformer, valueTransformer, null, null);
    }

    public static <R, C, V> void writeTableToCsv(Table<R, C, V> table, String fileName, Function<R, String> rowHeaderTransformer,
            Function<C, String> columnHeaderTransformer, BiFunction<R, C, Function<V, String>> valueTransformer) throws IOException
    {
        writeTableToCsv(table, fileName, rowHeaderTransformer, columnHeaderTransformer, valueTransformer, null, null);
    }

    public static <R, C, V> void writeTableToCsv(Table<R, C, V> table, String fileName, Function<R, String> rowHeaderTransformer, 
            Function<C, String> columnHeaderTransformer, Function<V, String> valueTransformer, @Nullable Comparator<R> rowComparator, 
            @Nullable Comparator<C> columnComparator) throws IOException
    {
        writeTableToCsv(table, fileName, rowHeaderTransformer, columnHeaderTransformer, (R row, C column) -> valueTransformer, null, null);
    }

    public static <R, C, V> void writeTableToCsv(
            Table<R, C, V> table,
            String fileName,
            Function<R, String> rowHeaderTransformer,
            Function<C, String> columnHeaderTransformer,
            BiFunction<R, C, Function<V, String>> valueTransformer,
            @Nullable Comparator<R> rowComparator,
            @Nullable Comparator<C> columnComparator
    ) throws IOException
    {
        FileWriter fileWriter = new FileWriter(fileName);
        @Cleanup CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.EXCEL);
        csvPrinter.print("");
        // Traversing all column keys is very slow; we do it here once, and reuse the result in an inner loop later
        Set<C> columnKeys = new LinkedHashSet<>();
        columnKeys.addAll(table.columnKeySet());
        Set<R> rowKeys = table.rowKeySet();
        if (columnComparator != null) {
            TreeSet<C> sortedColumnKeys = new TreeSet<>(columnComparator);
            sortedColumnKeys.addAll(columnKeys);
            columnKeys = sortedColumnKeys;
        }
        if (rowComparator != null) {
            TreeSet<R> sortedRowKeys = new TreeSet<>(rowComparator);
            sortedRowKeys.addAll(rowKeys);
            rowKeys = sortedRowKeys;
        }
        for (C columnKey : columnKeys)
            csvPrinter.print(columnHeaderTransformer.apply(columnKey));
        csvPrinter.println();
        for (R rowKey : rowKeys) {
            csvPrinter.print(rowHeaderTransformer.apply(rowKey));
            for (C columnKey : columnKeys)
                csvPrinter.print(valueTransformer.apply(rowKey, columnKey).apply(table.get(rowKey, columnKey)));
            csvPrinter.println();
        }
        csvPrinter.close();
    }

    public static <V> void writeMultisetToCsv(Multiset<V> multiset, String fileName) throws IOException
    {
        writeMultisetToCsv(multiset, el -> el, fileName);
    }

    public static <V> void writeMultisetToCsv(Multiset<V> multiset, Function<V, ?> elementMapper, String fileName) throws IOException
    {
        writeMultisetToCsv(multiset, elementMapper, fileName, 0);
    }
    
    public static <V> void writeMultisetToCsv(Multiset<V> multiset, String fileName, int threshold) throws IOException
    {
        writeMultisetToCsv(multiset, el -> el, fileName, threshold);
    }
    
    public static <V> void writeMultisetToCsv(Multiset<V> multiset, Function<V, ?> elementMapper, String fileName, int threshold) throws IOException
    {
        @Cleanup CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(fileName), CSVFormat.EXCEL);
        for (Multiset.Entry<V> entry : multiset.entrySet()) {
            int count = entry.getCount();
            if (count < threshold)
                continue;
            csvPrinter.printRecord(elementMapper.apply(entry.getElement()), count);
        }
        csvPrinter.close();
    }

    /** Returns relative filenames, e.g. "restaurantTaggings/Amersfoort.csv". **/
    public static List<String> getFilenamesInDirectory(String directoryName) throws IOException
    {
        // Getting filenames within a directory become non-trivial when files are zipped into a jar. Hence this
        // somewhat unelegant code.
        URL resource = IoUtil.class.getClassLoader().getResource(directoryName);
        if (resource == null)
            throw new IOException("Can't find directory '" + directoryName + "'");

        URI uri;
        try {
            uri = resource.toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Cannot translate URL " + resource + " to URI", e);
        }

        Path myPath;
        FileSystem fileSystem = null;
        if (uri.getScheme().equals("jar")) {
            fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            myPath = fileSystem.getPath("/" + directoryName);
        } else {
            myPath = Paths.get(uri);
        }

        Stream<Path> walk = java.nio.file.Files.walk(myPath, 1);
        Iterator<Path> it = walk.iterator();
        List<String> results = new ArrayList<>();
        it.next(); // skip first element, as this is always the root itself
        while (it.hasNext()) {
            Path path = it.next();
            if (uri.getScheme().equals("jar"))
                results.add(path.toString().substring(1)); // peel off first slash
            else
                results.add(directoryName + "/" + path.getFileName());
        }
        if (fileSystem != null)
            fileSystem.close();
        return results;
    }

}
