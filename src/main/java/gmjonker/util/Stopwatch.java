/*
 * Copyright (C) 2008 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package gmjonker.util;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Ticker;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static gmjonker.math.GeneralMath.max;
import static gmjonker.math.NaType.NA_L;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Extended version of Google's Stopwatch, with support for markers and getting time periods between
 * two given markers.
 * <p>
 * <p>
 * An object that measures elapsed time in nanoseconds. It is useful to measure elapsed time using
 * this class instead of direct calls to {@link System#nanoTime} for a few reasons:
 * <p>
 * <ul>
 * <li>An alternate time source can be substituted, for testing or performance reasons.
 * <li>As documented by {@code nanoTime}, the value returned has no absolute meaning, and can only
 * be interpreted as relative to another timestamp returned by {@code nanoTime} at a different time.
 * {@code Stopwatch} is a more effective abstraction because it exposes only these relative values,
 * not the absolute ones.
 * </ul>
 * <p>
 * <p>Basic usage: <pre>   {@code
 * <p>
 *   Stopwatch stopwatch = Stopwatch.createStarted();
 *   doSomething();
 *   stopwatch.stop(); // optional
 * <p>
 *   long millis = stopwatch.elapsed(MILLISECONDS);
 * <p>
 *   log.info("time: " + stopwatch); // formatted string like "12.3 ms"}</pre>
 * <p>
 * <p>Stopwatch methods are not idempotent; it is an error to start or stop a stopwatch that is
 * already in the desired state.
 * <p>
 * <p>When testing code that uses this class, use {@link #createUnstarted(Ticker)} or
 * {@link #createStarted(Ticker)} to supply a fake or mock ticker. This allows you to simulate any
 * valid behavior of the stopwatch.
 * <p>
 * <p><b>Note:</b> This class is not thread-safe.
 * <p>
 * <p><b>Warning for Android users:</b> a stopwatch with default behavior may not continue to keep
 * time while the device is asleep. Instead, create one like this: <pre>   {@code
 * <p>
 *    Stopwatch.createStarted(
 *         new Ticker() {
 *           public long read() {
 *             return android.os.SystemClock.elapsedRealtime();
 *           }
 *         });}</pre>
 *
 * @author Kevin Bourrillion
 * @since 10.0
 */
@GwtCompatible
public final class Stopwatch
{
    private final Ticker ticker;
    private boolean isRunning;
    private long elapsedNanos;
    private long startTick;
    
    private static final LambdaLogger log = new LambdaLogger(Stopwatch.class);
    
    /**
     * Creates (but does not start) a new stopwatch using {@link System#nanoTime} as its time source.
     *
     * @since 15.0
     */
    public static Stopwatch createUnstarted()
    {
        return new Stopwatch();
    }

    /**
     * Creates (but does not start) a new stopwatch, using the specified time source.
     *
     * @since 15.0
     */
    public static Stopwatch createUnstarted(Ticker ticker)
    {
        return new Stopwatch(ticker);
    }

    /**
     * Creates (and starts) a new stopwatch using {@link System#nanoTime} as its time source.
     *
     * @since 15.0
     */
    public static Stopwatch createStarted()
    {
        return new Stopwatch().start();
    }

    /**
     * Creates (and starts) a new stopwatch, using the specified time source.
     *
     * @since 15.0
     */
    public static Stopwatch createStarted(Ticker ticker)
    {
        return new Stopwatch(ticker).start();
    }

    Stopwatch()
    {
        this.ticker = Ticker.systemTicker();
    }

    Stopwatch(Ticker ticker)
    {
        this.ticker = checkNotNull(ticker, "ticker");
    }

    /**
     * Returns {@code true} if {@link #start()} has been called on this stopwatch, and {@link #stop()}
     * has not been called since the last call to {@code
     * start()}.
     */
    public boolean isRunning()
    {
        return isRunning;
    }

    /**
     * Starts the stopwatch.
     *
     * @return this {@code Stopwatch} instance
     * @throws IllegalStateException if the stopwatch is already running.
     */
    @CanIgnoreReturnValue
    public Stopwatch start()
    {
        checkState(!isRunning, "This stopwatch is already running.");
        isRunning = true;
        startTick = ticker.read();
        return this;
    }

    /**
     * Stops the stopwatch. Future reads will return the fixed duration that had elapsed up to this
     * point.
     *
     * @return this {@code Stopwatch} instance
     * @throws IllegalStateException if the stopwatch is already stopped.
     */
    @CanIgnoreReturnValue
    public Stopwatch stop()
    {
        long tick = ticker.read();
        checkState(isRunning, "This stopwatch is already stopped.");
        isRunning = false;
        elapsedNanos += tick - startTick;
        return this;
    }

    /**
     * Sets the elapsed time for this stopwatch to zero, and places it in a stopped state.
     *
     * @return this {@code Stopwatch} instance
     */
    @CanIgnoreReturnValue
    public Stopwatch reset()
    {
        elapsedNanos = 0;
        isRunning = false;
        return this;
    }
    
    Map<String, Long> markers = new LinkedHashMap<>();
    public Stopwatch mark(String marker)
    {
//        long now = elapsedNanos();
        long now = ticker.read();
        markers.put(marker, now);
        return this;
    }

    private long elapsedNanos()
    {
        return isRunning ? ticker.read() - startTick + elapsedNanos : elapsedNanos;
    }

    /**
     * Returns the current elapsed time shown on this stopwatch, expressed in the desired time unit,
     * with any fraction rounded down.
     * <p>
     * <p>Note that the overhead of measurement can be more than a microsecond, so it is generally not
     * useful to specify {@link TimeUnit#NANOSECONDS} precision here.
     *
     * @since 14.0 (since 10.0 as {@code elapsedTime()})
     */
    public long elapsed(TimeUnit desiredUnit)
    {
        return desiredUnit.convert(elapsedNanos(), NANOSECONDS);
    }
    
    public long elapsedBetween(String marker1, String marker2)
    {
        Long time1 = markers.get(marker1);
        Long time2 = markers.get(marker2);
        if (time1 == null) {
            log.warn("Marker '{}' not set...", marker1);
            return NA_L;
        }
        if (time2 == null) {
            log.warn("Marker '{}' not set...", marker2);
            return NA_L;
        }
        return time2 - time1;
    }

    public String elapsedBetweenToString(String marker1, String marker2)
    {
        return nanosToString(elapsedBetween(marker1, marker2));
    }

    /**
     * Returns a string representation of the current elapsed time.
     */
    @Override
    public String toString()
    {
        if (markers.isEmpty())
            return nanosToString(elapsedNanos());
        
        StringBuilder s = new StringBuilder();
        String marker1 = "START";
        int maxWidth = marker1.length();
        for (String marker2 : markers.keySet()) {
            maxWidth = max(maxWidth, marker1.length() + marker2.length());
            marker1 = marker2;
        }
        maxWidth = max(maxWidth, marker1.length() + "END".length());

        marker1 = "START";
        long tick1 = this.startTick;
        for (String marker2 : markers.keySet()) {
            Long tick2 = markers.get(marker2);
            s
                    .append(marker1)
                    .append(" - ")
                    .append(marker2)
                    .append(StringUtils.repeat(" ", max(0, maxWidth - marker1.length() - marker2.length())))
                    .append(": ")
                    .append(nanosToString(tick2 - tick1))
                    .append("\n");
            tick1 = tick2;
            marker1 = marker2;
        }
        long tick2 = this.startTick + this.elapsedNanos;
        String marker2 = "END";
        s
                .append(marker1)
                .append(" - ")
                .append(marker2)
                .append(StringUtils.repeat(" ", max(0, maxWidth - marker1.length() - marker2.length())))
                .append(": ")
                .append(nanosToString(tick2 - tick1))
                .append("\n");
        s
                .append("Total: ")
                .append(nanosToString(elapsedNanos));
        return s.toString();        
    }

    public String nanosToString(long nanos)
    {
        TimeUnit unit = chooseUnit(nanos);
        double value = (double) nanos / NANOSECONDS.convert(1, unit);

        // Too bad this functionality is not exposed as a regular method call
        //        return Platform.formatCompact4Digits(value) + " " + abbreviate(unit);
        return String.format(Locale.ROOT, "%.4g", value) + " " + abbreviate(unit);
    }

    private static TimeUnit chooseUnit(long nanos)
    {
        if (DAYS.convert(nanos, NANOSECONDS) > 0) {
            return DAYS;
        }
        if (HOURS.convert(nanos, NANOSECONDS) > 0) {
            return HOURS;
        }
        if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
            return MINUTES;
        }
        if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
            return SECONDS;
        }
        if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MILLISECONDS;
        }
        if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MICROSECONDS;
        }
        return NANOSECONDS;
    }

    private static String abbreviate(TimeUnit unit)
    {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "\u03bcs"; // μs
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new AssertionError();
        }
    }
}
