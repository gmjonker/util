package gmjonker.util;

import com.google.common.base.Strings;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import jdk.nashorn.internal.objects.NativeString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import static gmjonker.math.GeneralMath.*;
import static gmjonker.math.NaType.isValue;
import static java.util.concurrent.TimeUnit.*;

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
    public static String asPercentage(Double d)
    {
        if (d == null)
            return "nul";
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
     * Formats a positive score value as a single character, where 0->0, .1->1, .2->2, ..., .9->9, 1->T, <0 -> '<', >0 -> '>'
     **/
    public static String toMicroFormatM01(double d)
    {
        if ( ! isValue(d))
            return "-";
        if (d < 0) {
            return "<";
        }
        if (d > 1) {
            return ">";
        }
        long rounded = round(d * 10);
        if (rounded < 10)
            return String.valueOf(rounded);
        else if (rounded == 10)
            return "T";
        throw new RuntimeException("This is not supposed to happen");
    }

    /**
     * Formats a value in (-1,1) as a single digit, where -1->0, -.8->2, ..., 0->5, .2->6, ... .8->9, 1->T, >1 -> '>', <-1 -> '<'
     * @param d value in (-1,1)
     **/
    public static String toMicroFormatM11(double d)
    {
        if ( ! isValue(d))
            return "-";
        long rounded = round(d * 5) + 5; // result in (0..10)
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
     * Formats a positive score value as a single character, where 0=A, ... , 1=F, -0=z, ..., -1=v
     **/
    public static String toMicroFormatABC(double d)
    {
        final int numSteps = 5;
        if ( ! isValue(d))
            return "-";
        if (d < -1) {
            return "<";
        }
        if (d > 1) {
            return ">";
        }
        long step = floor(d * numSteps);
        if (d < 0)
            return "" + (char)('z' + step + 1);
        else if (d >= 0)
            return "" + (char)('A' + step);
        return "?";
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

    public static <K> String mapToString(Map<K,?> map, Function<K,?> keyMapper)
    {
        return CollectionsUtil.map(map, keyMapper, v -> v).toString();
    }

    // This method is needed because the mapToString above will always select CollectionsUtil.map(Map...)
    public static <K> String mapToString(DefaultingMap<K,?> map, Function<K,?> keyMapper)
    {
        return CollectionsUtil.map(map, keyMapper, v -> v).toString();
    }

    public static <K,V> String formatMap(Map<K,V> map, BiFunction<K,V,String> formatter)
    {
        return CollectionsUtil.map(IterableUtils.toList(map.entrySet()), entry -> formatter.apply(entry.getKey(), entry.getValue())).toString();
    }

    public static String toWidth(String string, int width)
    {
        width = max(0, width);
        return Strings.padEnd("" + string, width, ' ').substring(0, width);
    }

    @Nonnull
    public static String take(@Nullable String string, int width)
    {
        if (string == null) {
            return "";
        }
        return NativeString.substring(string, 0, width);
    }

    /**
     * Substring of string from fromIndex, inclusive, to toIndex, exclusive.
     * Always returns a string. If fromIndex or toIndex is null, 0 or string.length() is used respectively.
     * Out of bounds indices are brought within range. Indices -1, -2, etc. may be used for
     * string.length()-1, string.length()-2, etc.
     */
    @Nonnull
    public static String substring(String string, @Nullable Integer fromIndex, @Nullable Integer toIndex)
    {
        if (string == null)
            return "";

        int length = string.length();
        if (fromIndex == null) fromIndex = 0;
        if (toIndex == null) toIndex = length;
        if (fromIndex < 0) fromIndex = max(length + fromIndex, 0);
        if (toIndex < 0) toIndex = max(length + toIndex, 0);
        if (toIndex > length) toIndex = length;
        if (fromIndex >= toIndex)
            return "";
        return NativeString.substring(string, fromIndex, toIndex);
    }

    public static String prettyJson(String json)
    {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(json);
        return gson.toJson(je);
    }

    // Implementation is a bit slow, beware.
    public static <R, C, V> String tableToString(Table<R, C, V> table)
    {
        return tableToString(table, FormattingUtil::toStringer, FormattingUtil::toStringer, FormattingUtil::toStringer, null, null);
    }

    // Implementation is a bit slow, beware.
    public static <R, C, V> String tableToString(Table<R, C, V> table, Function<V, String> valueFormatter)
    {
        return tableToString(table, FormattingUtil::toStringer, FormattingUtil::toStringer, valueFormatter, null, null);
    }

    // Implementation is a bit slow, beware.
    public static <R, C, V> String tableToString(Table<R, C, V> table, Function<R, String> rowHeaderFormatter,
            Function<C, String> columnHeaderFormatter, Function<V, String> valueFormatter)
    {
        return tableToString(table, rowHeaderFormatter, columnHeaderFormatter, valueFormatter, null, null);
    }

    // Implementation is a bit slow, beware.
    public static <R, C, V> String tableToString(Table<R, C, V> table, Function<R, String> rowHeaderFormatter,
            Function<C, String> columnHeaderFormatter, Function<V, String> valueFormatter,
            @Nullable Comparator<R> rowComparator, @Nullable Comparator<C> columnComparator)
    {
        String result = "";
        HashMap<C, Integer> maxWidths = new HashMap<>();
        Set<C> columnKeys = table.columnKeySet();
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
        for (C col : columnKeys) {
            Integer width = columnHeaderFormatter.apply(col).length();
            maxWidths.compute(col, (k, v) -> (v == null) ? width : max(v, width));
        }
        int maxRowHeaderWidth = Integer.MIN_VALUE;
        for (R rowKey : rowKeys) {
            if (rowHeaderFormatter.apply(rowKey).length() > maxRowHeaderWidth)
                maxRowHeaderWidth = rowHeaderFormatter.apply(rowKey).length();
            Map<C, V> row = table.row(rowKey);
            for (C col : row.keySet()) {
                V value = row.get(col);
                Integer width = valueFormatter.apply(value).length();
                maxWidths.compute(col, (k, v) -> (v == null) ? width : max(v, width));
            }
        }
        for (C col : columnKeys) {
            log.trace("Max width for col '{}': {}", col, maxWidths.get(col));
        }
        result += toWidth("", maxRowHeaderWidth + 1);
        for (C col : columnKeys) {
            String string = toWidth(columnHeaderFormatter.apply(col), maxWidths.get(col) + 1);
            result += string;
        }
        result += System.lineSeparator();
        int count = 0;
        for (R rowKey : rowKeys) {
            result += toWidth(rowHeaderFormatter.apply(rowKey), maxRowHeaderWidth) + " ";
            Map<C, V> row = table.row(rowKey);
            for (C col : columnKeys) {
                V value = row.get(col);
                String string = toWidth(valueFormatter.apply(value), maxWidths.get(col) + 1);
                result += string;
            }
            result += System.lineSeparator();
//            if (count++ % 100 == 0)
//                System.out.print(".");
        }
        return result;
    }

    public static String toStringer(Object o)
    {
        return o == null ? "-" : o.toString();
    }
}
