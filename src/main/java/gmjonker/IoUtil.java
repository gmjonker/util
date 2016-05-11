package gmjonker;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
