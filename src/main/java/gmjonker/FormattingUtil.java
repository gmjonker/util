package gmjonker;

import com.google.common.base.Strings;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import static gmjonker.math.GeneralMath.min;
import static gmjonker.math.GeneralMath.round;
import static gmjonker.math.NaType.isValue;
import static java.util.concurrent.TimeUnit.*;

public class FormattingUtil
{
    protected static final LambdaLogger log = new LambdaLogger(FormattingUtil.class);

    /** Formats score value with two digits, or " -" for NA. **/
    public static String shortForm(double d)
    {
        return isValue(d) ? ScoreUtil.scoreValueEquals(d, 0) ? "0" : String.format("%.2f", d)
                          : " -";
    }

    /** Returns the int as a string, or " -" for NA. **/
    public static String shortForm(int i)
    {
        return isValue(i) ? String.format("%s", i) : " -";
    }

    /** Trims items to first three letters. **/
    public static String shortForm(Collection<String> strings)
    {
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
        if (ScoreUtil.scoreValueEquals(d, 1))
            return "HH";
        if (d > 1) {
            log.trace("Score {} is greater than 1", d);
            return "H!";
        }
        if (d > ScoreUtil.SCORE_VALUE_EPSILON && d < .005)
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
        if (ScoreUtil.scoreValueEquals(d, 1))
            return " HH";
        if (ScoreUtil.scoreValueEquals(d, -1))
            return "-HH";
        if (d > 1) {
            log.trace("Score {} is greater than 1", d);
            return " H!";
        }
        if (d < -1) {
            log.trace("Score {} is less than -1", d);
            return "-H!";
        }
        if (d > ScoreUtil.SCORE_VALUE_EPSILON && d < .005)
            return " 0.";
        if (d < -ScoreUtil.SCORE_VALUE_EPSILON && d > -.005)
            return "-0.";
        else
            return String.format("%3.0f", d * 100);
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
}
