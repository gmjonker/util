package gmjonker.util;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static gmjonker.math.GeneralMath.round;
import static gmjonker.math.NaType.NA_I;
import static gmjonker.util.FormattingUtil.nanosToString;

/**
 * Utility methods that do not fall in any of the other categories.
 */
@SuppressWarnings("WeakerAccess")
public class Util
{
    public static final Function<Object, Object> identity = Function.identity();

    protected static final LambdaLogger log = new LambdaLogger(Util.class);

    public static void simpleSleep(long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void logMemory()
    {
        log.info(getConciseMemoryInfo());
    }

    public static String getConciseMemoryInfo()
    {
        Runtime runtime = Runtime.getRuntime();
        long max       = round(1.0 * runtime.maxMemory()   / 1024 / 1024);
        long allocated = round(1.0 * runtime.totalMemory() / 1024 / 1024);
        long free      = round(1.0 * runtime.freeMemory()  / 1024 / 1024);
        long used = allocated - free;
        return String.format("Mem used: %s, free: %s (alloc: %s, max: %s)", used, max - used, allocated, max);
    }

    public static String getVeryConciseMemoryInfo()
    {
        Runtime runtime = Runtime.getRuntime();
        long allocated = round(1.0 * runtime.totalMemory() / 1024 / 1024);
        long free      = round(1.0 * runtime.freeMemory()  / 1024 / 1024);
        long used = allocated - free;
        return String.format("Mem used: %s", used);
    }

    public static String getEnvOrFail(String name)
    {
        String value = System.getenv(name);
        // If docker is told to copy an env var from host to container, and the var is not set on the host, it will
        // set the var on the container to ''
        if (Strings.isNullOrEmpty(value)) {
            log.error("Environment variable {} not set, exiting...", name);
            System.exit(-1);
        }
        log.debug("{}={} (env)", name, value);
        return value;
    }

    public static String getEnvOrDefault(String name, String defaultValue)
    {
        String value = System.getenv(name);
        // If docker is told to copy an env var from host to container, and the var is not set on the host, it will
        // set the var on the container to ''
        if (Strings.isNullOrEmpty(value)) {
            value = defaultValue;
            log.debug("{}={} (default)", name, value);
        } else {
            log.debug("{}={} (env)", name, value);
        }
        return value;
    }

    public static int getEnvOrDefault(String name, int defaultValue)
    {
        String env = System.getenv(name);
        Integer result;
        // If docker is told to copy an env var from host to container, and the var is not set on the host, it will
        // set the var on the container to ''
        if (Strings.isNullOrEmpty(env)) {
            result = defaultValue;
            log.debug("{}={} (default)", name, result);
        } else {
            result = tryParseInt(env);
            if (result == null) {
                log.error("Could not parse '{}' for env variable '{}', exiting", env, name);
                System.exit(-1);
            }
            log.debug("{}={} (env)", name, result);
        }
        return result;
    }

    public static void printAllEnvironmentVariables()
    {
        Map<String, String> getenv = System.getenv();
        for (Map.Entry<String, String> entry : getenv.entrySet())
            System.out.println(entry.getKey() + "=" + entry.getValue());
    }

    public static void logEnvironmentVariable(String name)
    {
        String value = System.getenv(name);
        if (value != null)
            log.info(name + "=" + value);
        else
            log.info(name + " not set");
    }

    public static Integer tryParseInt(String s)
    {
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return NA_I;
        }
    }

    public static void continuationPrompt(String message)
    {
        System.out.println(message + " Continue? [Yn]");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine().toLowerCase();
        if (!Objects.equals(answer, "y") && !Objects.equals(answer, "")) {
            System.out.println("Exiting.");
            System.exit(-1);
        }
    }

    public static String executeCommandAndCaptureResult(String command) throws IOException
    {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        log.trace("Output of running '{}' is:", command);
        String result = "";
        String line;
        while ((line = br.readLine()) != null) {
            log.trace(line);
            result += line;
        }
        return result;
    }

    public static String getCurrentGitBranch()
    {
        try {
            return Util.executeCommandAndCaptureResult("git rev-parse --abbrev-ref HEAD");
        } catch (IOException e) {
            return "Error: Could not get current git branch";
        }
    }

    public static String getRemainingTime(Stopwatch stopwatch, int counter, int total)
    {
        if (counter > 0) {
            long elapsed = stopwatch.elapsed(TimeUnit.NANOSECONDS);
            long averageNanosPerUploadSoFar = elapsed / counter;
            long expectedTimeRemaining = averageNanosPerUploadSoFar * (total - counter);
            return nanosToString(expectedTimeRemaining);
        }
        return "?";
    }
}
