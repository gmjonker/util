package gmjonker.util;

import com.google.common.base.Strings;

import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import static gmjonker.math.GeneralMath.round;
import static gmjonker.math.NaType.NA_I;

/**
 * Utility methods that do not fall in any of the other categories.
 */
public class Util
{
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
}
