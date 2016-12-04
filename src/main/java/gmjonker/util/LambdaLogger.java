package gmjonker.util;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static gmjonker.util.CollectionsUtil.getOr;
import static gmjonker.util.CollectionsUtil.map;
import static gmjonker.util.FormattingUtil.getIndentation;
import static gmjonker.util.FormattingUtil.take;

/**
 * An extension of org.slf4j.Logger that also supports lambda functions as arguments.
 *
 * <p>When you have:
 * <pre>
 *     log.trace("Object has info {}", myObject.constructSomeInfoString())
 * </pre>
 * you have the problem that {@code constructSomeInfoString()} is always resolved, even when the info level is not trace.
 * The official solution is:
 * <pre>
 *     if (log.isTraceEnabled())
 *          log.trace("Object has info {}", myObject.constructSomeInfoString())
 * </pre>
 * This is rather verbose. And {@code isTraceEnabled()} is surprisingly slow. We can store its value, but that means
 * we can't change the log level on the fly anymore, and it is still verbose.
 *
 * <p>LambdaLogger solves this, kind of, by accepting lambda methods</p>
 * <pre>
 *     log.trace("Object has info {}", () -> myObject.constructSomeInfoString())
 *     log.trace("Object has info {}", myObject::constructSomeInfoString)
 * </pre>
 * Once you use one lambda method, you must use only lambda methods.
 *
 * TODO: have a look at http://stackoverflow.com/a/9039954/1901037
 *
 * TODO: use SubstituteLogger as delegate logger?
 */
public class LambdaLogger implements Logger
{
    private final Logger logger;

    private final Set<String> rememberedErrors = new HashSet<>();
    private final Set<String> rememberedWarnings = new HashSet<>();
    private final Set<String> rememberedInfos = new HashSet<>();
    private final Set<String> rememberedDebugs = new HashSet<>();
    private final Set<String> rememberedTraces = new HashSet<>();

    public LambdaLogger(Class clazz)
    {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    //
    // Methods that accept lambda methods
    //

    public final void error(Supplier<String> argument)
    {
        if (logger.isErrorEnabled())
            error(argument.get());
    }

    @SafeVarargs
    public final void error(String format, Supplier<Object>... arguments)
    {
        if (logger.isErrorEnabled())
            error(format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }

    public final void errorOnce(String message)
    {
        if (logger.isErrorEnabled()) {
            if (rememberedErrors.contains(message))
                return;
            error(message + " (Won't show this warning again)");
            rememberedErrors.add(message);
        }
    }

    public final void errorOnce(String message, Object... argumentArray)
    {
        if (logger.isErrorEnabled()) {
            FormattingTuple formattingTuple = MessageFormatter.arrayFormat(message, argumentArray);
            String formattedMessage = formattingTuple.getMessage();
            Throwable throwable = formattingTuple.getThrowable();
            if (rememberedErrors.contains(formattedMessage + throwable.getMessage()))
                return;
            error(formattedMessage + " (Won't show this warning again)", throwable);
            rememberedErrors.add(formattedMessage + throwable.getMessage());
        }
    }

    // --------------------------------------------------------------------------------------------

    public final void warn(Supplier<String> argument)
    {
        if (logger.isWarnEnabled())
            warn(argument.get());
    }

    @SafeVarargs
    public final void warn(String format, Supplier<Object>... arguments)
    {
        if (logger.isWarnEnabled())
            warn(format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }

    public final void warnOnce(String message)
    {
        if (logger.isWarnEnabled()) {
            if (rememberedWarnings.contains(message))
                return;
            warn(message + " (Won't show this warning again)");
            rememberedWarnings.add(message);
        }
    }

    public final void warnOnce(String message, Object... argumentArray)
    {
        if (logger.isWarnEnabled()) {
            String formattedMessage = MessageFormatter.arrayFormat(message, argumentArray).getMessage();
            if (rememberedWarnings.contains(formattedMessage))
                return;
            warn(formattedMessage + " (Won't show this warning again)");
            rememberedWarnings.add(formattedMessage);
        }
    }

    // --------------------------------------------------------------------------------------------

    public final void info(Supplier<String> argument)
    {
        if (logger.isInfoEnabled())
            info(argument.get());
    }

    @SafeVarargs
    public final void info(String format, Supplier<Object>... arguments)
    {
        if (logger.isInfoEnabled())
            info(format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }

    public final void infoOnce(String message)
    {
        if (logger.isInfoEnabled()) {
            if (rememberedInfos.contains(message))
                return;
            info(message + " (Won't show this message again)");
            rememberedInfos.add(message);
        }
    }

    public final void infoOnce(String message, Object... argumentArray)
    {
        if (logger.isInfoEnabled()) {
            String formattedMessage = MessageFormatter.arrayFormat(message, argumentArray).getMessage();
            if (rememberedInfos.contains(formattedMessage))
                return;
            info(formattedMessage + " (Won't show this message again)");
            rememberedInfos.add(formattedMessage);
        }
    }

    // --------------------------------------------------------------------------------------------

    /** Log separate lines separately, indents all between first and last line of argument **/
    public void debug2(String format, Object argument)
    {
        if (logger.isDebugEnabled()) {
            String[] formatParts = format.split("\\{\\}");
            int indentation = getIndentation(format);
            String begin = getOr(formatParts, 0, "");
            String end = getOr(formatParts, 1, "");
            if (begin.trim().length() > 0)
                logger.debug(begin);
            String s = argument.toString();
            String[] lines = s.split("\n");
            for (String line : lines) {
                logger.debug(Strings.repeat(" ", indentation) + "{}", line);
            }
            if (end.trim().length() > 0)
                logger.debug(end);
        }
    }


    public final void debug(Supplier<String> argument)
    {
        if (logger.isDebugEnabled())
            debug(argument.get());
    }

    @SafeVarargs
    public final void debug(String format, Supplier<Object>... arguments)
    {
        if (logger.isDebugEnabled()) {
            Function<Supplier<Object>, Object> get = Supplier::get;
//            debug(format, Arrays.stream(arguments).map(get).toArray());
            debug(format, map(arguments, get, Object.class));
        }
    }

    public final void debugOnce(String message)
    {
        if (logger.isDebugEnabled()) {
            if (rememberedDebugs.contains(message))
                return;
            debug(message + " (Won't show this message again)");
            rememberedDebugs.add(message);
        }
    }

    public final void debugOnce(String message, Object... argumentArray)
    {
        if (logger.isDebugEnabled()) {
            String formattedMessage = MessageFormatter.arrayFormat(message, argumentArray).getMessage();
            if (rememberedDebugs.contains(formattedMessage))
                return;
            debug(formattedMessage + " (Won't show this message again)");
            rememberedDebugs.add(formattedMessage);
        }
    }

    @SafeVarargs
    public final void debug(Marker marker, String format, Supplier<Object>... arguments)
    {
        if (logger.isDebugEnabled()) {
            Function<Supplier<Object>, Object> get = Supplier::get;
//            debug(format, Arrays.stream(arguments).map(get).toArray());
            debug(marker, format, map(arguments, get, Object.class));
        }
    }

    // --------------------------------------------------------------------------------------------

    public final void trace(Supplier<String> argument)
    {
        if (logger.isTraceEnabled())
            trace(argument.get());
    }

    @SafeVarargs
    public final void trace(String format, Supplier<Object>... arguments)
    {
        if (logger.isTraceEnabled())
            trace(format, Arrays.stream(arguments).map(Supplier::get).toArray());
    }

    public final void traceOnce(String message)
    {
        if (logger.isTraceEnabled()) {
            if (rememberedTraces.contains(message))
                return;
            trace(message + " (Won't show this message again)");
            rememberedTraces.add(message);
        }
    }

    public final void traceOnce(String message, Object... argumentArray)
    {
        if (logger.isTraceEnabled()) {
            String formattedMessage = MessageFormatter.arrayFormat(message, argumentArray).getMessage();
            if (rememberedTraces.contains(formattedMessage))
                return;
            trace(formattedMessage + " (Won't show this message again)");
            rememberedTraces.add(formattedMessage);
        }
    }




    // --------------------------------------------------------------------------------------------

    public void indent()
    {
        String indent = MDC.get("indent");
        if (indent == null) indent = "";
        MDC.put("indent", indent + "  ");
    }

    public void unindent()
    {
        String indent = MDC.get("indent");
        MDC.put("indent", take(indent, -2, false));
    }


    //
    // Delegate all standard methods ------------------------------------------------------------------
    //

    @Override
    public String getName()
    {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled()
    {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg)
    {
        logger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg)
    {
        logger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2)
    {
        logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments)
    {
        logger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t)
    {
        logger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker)
    {
        return logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg)
    {
        logger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg)
    {
        logger.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2)
    {
        logger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray)
    {
        logger.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t)
    {
        logger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg)
    {
        logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg)
    {
        logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2)
    {
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments)
    {
        logger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t)
    {
        logger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker)
    {
        return logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg)
    {
        logger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg)
    {
        logger.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2)
    {
        logger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments)
    {
        logger.debug(marker, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t)
    {
        logger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled()
    {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg)
    {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg)
    {
        logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2)
    {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments)
    {
        logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t)
    {
        logger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker)
    {
        return logger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg)
    {
        logger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg)
    {
        logger.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2)
    {
        logger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments)
    {
        logger.info(marker, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t)
    {
        logger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled()
    {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg)
    {
        logger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg)
    {
        logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments)
    {
        logger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2)
    {
        logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t)
    {
        logger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker)
    {
        return logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg)
    {
        logger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg)
    {
        logger.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2)
    {
        logger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments)
    {
        logger.warn(marker, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t)
    {
        logger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled()
    {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg)
    {
        logger.error(msg);
    }

    @Override
    public void error(String format, Object arg)
    {
        logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2)
    {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments)
    {
        logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t)
    {
        logger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker)
    {
        return logger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg)
    {
        logger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg)
    {
        logger.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2)
    {
        logger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments)
    {
        logger.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t)
    {
        logger.error(marker, msg, t);
    }
}
