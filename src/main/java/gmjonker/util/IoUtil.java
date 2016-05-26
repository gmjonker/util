package gmjonker.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IoUtil
{
    protected static final LambdaLogger log = new LambdaLogger(IoUtil.class);

    public static List<String> readFileOrLogError(String name)
    {
        try {
            return readFileOrThrowException(name);
        } catch (Exception e) {
            log.error("Error while reading file {}", name, e);
        }
        return Collections.emptyList();
    }

    public static List<String> readFileOrThrowException(String name) throws IOException
    {
        try {
            // This way of getting to a resource seems to work in Docker, Maven and Intellij IDEA.
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            return reader.lines().collect(Collectors.toList());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static String readFileAsOneStringOrThrowException(String name) throws IOException
    {
        try {
            log.debug("Getting resource {}", name);
            // This way of getting to a resource seems to work in Docker, Maven and Intellij IDEA.
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static CSVParser readCsvFileWithHeaders(String fileName) throws IOException
    {
        String fileContent = readFileAsOneStringOrThrowException(fileName);
        return CSVParser.parse(fileContent, CSVFormat.EXCEL.withHeader().withAllowMissingColumnNames()
                .withIgnoreEmptyLines().withIgnoreSurroundingSpaces());
    }

    public static CSVParser readCsvFileWithoutHeaders(String fileName) throws IOException
    {
        String fileContent = readFileAsOneStringOrThrowException(fileName);
        return CSVParser.parse(fileContent, CSVFormat.EXCEL.withIgnoreEmptyLines().withIgnoreSurroundingSpaces());
    }

    /**
     * Reads from a CSV file that has row and column headers.
     */
    public static <R, C, T> Table<R, C, T> readCsvIntoTable(String fileName, Function<String, R> rowTypeMapper,
            Function<String, C> columnTypeMapper, Function<String, T> cellTypeMapper) throws IOException
    {
        Table<R, C, T> table = HashBasedTable.create();
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
                table.put(r, c, t);
            }
        }
        csvParser.close();
        return table;
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

    public static <R, C, T> void writeTableToCsv(Table<R, C, T> table, String fileName) throws IOException
    {
        CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(fileName), CSVFormat.EXCEL);
        csvPrinter.print("");
        for (C columnKey : table.columnKeySet())
            csvPrinter.print(columnKey);
        csvPrinter.println();
        for (R rowKey : table.rowKeySet()) {
            csvPrinter.print(rowKey);
            for (C columnKey : table.columnKeySet())
                csvPrinter.print(table.get(rowKey, columnKey));
            csvPrinter.println();
        }
        csvPrinter.close();
    }
}
