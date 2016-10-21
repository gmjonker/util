package gmjonker.math;

import gmjonker.util.LambdaLogger;

/**
 * Provides special NA values for double and int. These can be used to indicate missing values.
 * Using primitive types is more memory and cpu efficient than using boxed types. In our case, this is especially useful
 * when building a recommendation.
 */
public class NaType
{
    // NA = Not Available
    public static final double NA = Double.NaN;
    public static final Double NAA = Double.NaN;
    public static final int NA_I = Integer.MIN_VALUE + 936; // Because who ever uses THAT number!

    private static final LambdaLogger log = new LambdaLogger(NaType.class);

    /** Converts an object to a double, and null to NA. **/
    public static double toSpecialdouble(Object o)
    {
        if (o == null)
            return NA;
        if (o instanceof Integer) return ((Integer)o).doubleValue();
        if (o instanceof Double) return (Double)o;
        log.error("Unhandled type {} in toSpecialdouble (with value {})", o.getClass(), o);
        return NA;
    }

    /** Converts a double to a Double, or to null if equal to NA. **/
    public static Double fromSpecialdouble(double d) {
        if (isValue(d))
            return d;
        else
            return null;
    }

    /** Converts an object to an int, and null to NA_I. **/
    public static int toSpecialint(Object o)
    {
        if (o == null)
            return NA_I;
        if (o instanceof Integer) return (Integer)o;
        if (o instanceof Double) {
            log.warn("Casting double {} to int", o);
            return (int) Math.round((Double)o);
        }
        log.error("Unhandled type {} in toSpecialint (with value {})", o.getClass(), o);
        return NA_I;
    }

    /** Converts an int to a Integer, or to null if equal to NA_I. **/
    public static Integer fromSpecialint(int i)
    {
        if (isValue(i))
            return i;
        else
            return null;
    }

    public static boolean isValue(int i)
    {
        return i != NA_I;
    }

    public static boolean isValue(double d)
    {
        return ! Double.isNaN(d);
    }

    public static boolean isValue(String s)
    {
        return s != null;
    }

    /** Gets value of d, or naSubst if d == NA. **/
    public static double getValueOr(double d, double naSubst)
    {
        if (isValue(d))
            return d;
        else
            return naSubst;
    }
}
