package gmjonker.util;

import com.google.common.base.Strings;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static gmjonker.math.GeneralMath.*;
import static gmjonker.math.NaType.isValue;
import static java.util.concurrent.TimeUnit.*;
import static jdk.nashorn.internal.objects.NativeString.substring;

public class FormattingUtil
{
    protected static final LambdaLogger log = new LambdaLogger(FormattingUtil.class);

    /** Formats score value with two digits, or " -" for NA. **/
    public static String shortForm(double d)
    {
        return isValue(d) ? ScoreValueUtil.scoreValueEquals(d, 0) ? "0" : String.format("%.2f", d)
                          : " -";
    }

    /** Returns the int as a string, or " -" for NA. **/
    public static String shortForm(int i)
    {
        return isValue(i) ? String.format("%s", i) : " -";
    }

    /** Trims items to first three letters. **/
    @Nonnull
    public static String shortForm(Collection<String> strings)
    {
        if (CollectionUtils.isEmpty(strings))
            return "";
        StringJoiner stringJoiner = new StringJoiner(",");
        for (String string : strings)
            stringJoiner.add(string.substring(0, min(3, string.length())));
        return stringJoiner.toString();
    }

    /**
     * Formats a positive score value as a percentage. Result is not fixed-width.
     **/
    public static String asPercentage(double d)
    {
        if ( ! isValue(d))
            return "-";
        return String.valueOf(round(d * 100));
    }

    /**
     * Formats a positive score value as a percentage. Two spaces wide, no decimals, right aligned, <0..0.005> becomes "0."
     **/
    public static String asPercentageTwoSpaces(double d)
    {
        if ( ! isValue(d))
            return " -";
        if (ScoreValueUtil.scoreValueEquals(d, 1))
            return "HH";
        if (d > 1) {
            log.trace("Score {} is greater than 1", d);
            return "H!";
        }
        if (d > ScoreValueUtil.SCORE_VALUE_EPSILON && d < .005)
            return "0.";
        if (d > .995 && d < 1)
            return "99";
        return String.format("%2.0f", d * 100);
    }

    /**
     * Formats a positive or negative score value as a percentage.
     * Three spaces wide, no decimals, right aligned, <0..0.005> becomes " 0.", <-0.005..0> becomes "-0."
     **/
    public static String asPercentageTwoSpacesNeg(double d)
    {
        if ( ! isValue(d))
            return "  -";
        if (ScoreValueUtil.scoreValueEquals(d, 1))
            return " HH";
        if (ScoreValueUtil.scoreValueEquals(d, -1))
            return "-HH";
        if (d > 1) {
            log.trace("Score {} is greater than 1", d);
            return " H!";
        }
        if (d < -1) {
            log.trace("Score {} is less than -1", d);
            return "-H!";
        }
        if (d > ScoreValueUtil.SCORE_VALUE_EPSILON && d < .005)
            return " 0.";
        if (d < -ScoreValueUtil.SCORE_VALUE_EPSILON && d > -.005)
            return "-0.";
        else
            return String.format("%3.0f", d * 100);
    }

    /**
     * Formats a positive score value as a single character, where 0=0, 1=.1, 2=.2, ..., 9=.1, T=1, +=>1
     **/
    public static String toMicroFormat(double d)
    {
        if ( ! isValue(d))
            return "-";
        long rounded = round(d * 10);
        if (rounded < 0)
            return "<";
        else if (rounded < 10)
            return String.valueOf(rounded);
        else if (rounded == 10)
            return "T";
        else if (rounded > 10)
            return ">";
        throw new RuntimeException("This is not supposed to happen");
    }

    /**
     * Formats a positive score value as a single character, where 0=A, ... , 1=E
     **/
    public static String toMicroFormatABC(double d)
    {
        final int numSteps = 4;
        if ( ! isValue(d))
            return "-";
        long rounded = round(d * numSteps);
        if (rounded < 0)
            return "<";
        else if (rounded <= numSteps)
            return "" + (char)('A' + rounded);
        else if (rounded > numSteps)
            return ">";
        throw new RuntimeException("This is not supposed to happen");
    }

    /**
     * Produces string representations of numbers like 1, 11, 111, 1.1k, 1m, etc.
     */
    public static String toHumanReadableNumber(int number)
    {
        if (number > -9999 && number < 9999)
            return "" + number;
        return toHumanReadableNumber(number, 0);
    }

    /**
     * Produces string representations of numbers like 1, 11, 111, 1.1k, 1m, etc.
     */
    public static String toHumanReadableNumber(long number)
    {
        if (number > -9999 && number < 9999)
            return "" + number;
        return toHumanReadableNumber(number, 0);
    }

    /**
     * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
     * Taken from http://stackoverflow.com/a/4753866/1901037
     * @param n the number to format
     * @param iteration in fact this is the class from the array c
     * @return a String representing the number n formatted in a cool looking way.
     */
    private static String toHumanReadableNumber(double n, int iteration)
    {
        final char[] c = new char[]{'k', 'm', 'b', 't'};
        double d = ((long) n / 100) / 10.0;
        boolean isRound = (d * 10) % 10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
        return (d < 1000 ? //this determines the class, i.e. 'k', 'm' etc
                ((d > 99.9 || isRound || d > 9.99 ? //this decides whether to trim the decimals
                        (int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
                ) + "" + c[iteration])
                : toHumanReadableNumber(d, iteration + 1));
    }

    /**
     * Print out a timespan in nanoseconds in a pleasantly readable format, for instance 2.34 µs.
     *
     * <p>Copied from Guava's Stopwatch class.</p>
     * @param nanos Timespan in nanos
     * @return Human-readable string representation of timespan.
     */
    public static String nanosToString(long nanos)
    {
        TimeUnit unit = chooseUnit(nanos);
        double value = (double) nanos / NANOSECONDS.convert(1, unit);
        return String.format("%.4g %s", value, abbreviate(unit));
    }

    /** Needed for nanosToString. Copied from Guava's Stopwatch class. */
    private static TimeUnit chooseUnit(long nanos)
    {
        if (DAYS.convert(nanos, NANOSECONDS) > 0) return DAYS;
        if (HOURS.convert(nanos, NANOSECONDS) > 0) return HOURS;
        if (MINUTES.convert(nanos, NANOSECONDS) > 0) return MINUTES;
        if (SECONDS.convert(nanos, NANOSECONDS) > 0) return SECONDS;
        if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) return MILLISECONDS;
        if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) return MICROSECONDS;
        return NANOSECONDS;
    }

    /** Needed for nanosToString. Copied from Guava's Stopwatch class. */
    private static String abbreviate(TimeUnit unit)
    {
        switch (unit) {
            case NANOSECONDS: return "ns";
            case MICROSECONDS: return "\u03bcs"; // μs
            case MILLISECONDS: return "ms";
            case SECONDS: return "s";
            case MINUTES: return "min";
            case HOURS: return "h";
            case DAYS: return "d";
            default: throw new AssertionError();
        }
    }

    public static <T> String listToStringLineByLine(List<T> list)
    {
        String result = "[\n";
        for (T t : list)
            result += "  " + t.toString() + "\n";
        result += "]";
        return result;
    }

    public static <K,V> String mapToStringLineByLine(Map<K,V> map)
    {
        String result = "[\n";
        for (Map.Entry<K, V> entry : map.entrySet())
            result += "  " + entry.getKey().toString() + " -> " + entry.getValue() + "\n";
        result += "]";
        return result;
    }

    public static String toWidth(String string, int width)
    {
        return Strings.padEnd("" + string, width, ' ').substring(0, width);
    }

    public static String take(String string, int width)
    {
        return substring(string, 0, width);
    }

    public static String prettyJson(String json)
    {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(json);
        return gson.toJson(je);
    }

    public static <R, C, T> String format(Table<R, C, T> table)
    {
        String result = "";
        HashMap<C, Integer> maxWidths = new HashMap<>();
        for (C col : table.columnKeySet()) {
            Integer width = col.toString().length();
            maxWidths.compute(col, (k, v) -> (v == null) ? width : max(v, width));
        }
        int maxRowHeaderWidth = Integer.MAX_VALUE;
        for (R rowKey : table.rowKeySet()) {
            if (rowKey.toString().length() < maxRowHeaderWidth)
                maxRowHeaderWidth = rowKey.toString().length();
            Map<C, T> row = table.row(rowKey);
            for (C col : row.keySet()) {
                T value = row.get(col);
                Integer width = value.toString().length();
                maxWidths.compute(col, (k, v) -> (v == null) ? width : max(v, width));
            }
        }
        result += toWidth("", maxRowHeaderWidth + 1);
        for (C col : table.columnKeySet()) {
            String string = toWidth(col.toString(), maxWidths.get(col) + 1);
            result += string;
        }
        result += System.lineSeparator();
        for (R rowKey : table.rowKeySet()) {
            result += toWidth(rowKey.toString(), maxRowHeaderWidth) + " ";
            Map<C, T> row = table.row(rowKey);
            for (C col : row.keySet()) {
                T value = row.get(col);
                String string = toWidth(value.toString(), maxWidths.get(col) + 1);
                result += string;
            }
            result += System.lineSeparator();
        }
        return result;
    }
}
