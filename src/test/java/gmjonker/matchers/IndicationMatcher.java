package gmjonker.matchers;

import gmjonker.math.Indication;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.StringJoiner;

import static gmjonker.util.ScoreValueUtil.scoreValueEqualToOrGreaterThan;
import static gmjonker.util.ScoreValueUtil.scoreValueEqualToOrLessThan;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AnyOf.anyOf;

/**
 */
@SuppressWarnings("WeakerAccess")
public class IndicationMatcher extends TypeSafeMatcher<Indication>
{
    public static final double VALUE_MIN = -1;
    public static final double LOW_VALUE_MAX = -.4;
    public static final double MEDIUM_VALUE_MIN = -.4;
    public static final double MEDIUM_VALUE_MAX = .4;
    public static final double NEUTRAL_VALUE_MIN = -.2;
    public static final double NEUTRAL_VALUE_MAX = .2;
    public static final double HIGH_VALUE_MIN = .4;
    public static final double VALUE_MAX = 1;
    public static final double CONFIDENCE_MIN = 0;
    public static final double WEAK_INDICATION_MIN_CONFIDENCE = .01;
    public static final double WEAK_INDICATION_MAX_CONFIDENCE = .25;
    public static final double MEDIUM_INDICATION_MIN_CONFIDENCE = .25;
    public static final double MEDIUM_INDICATION_MAX_CONFIDENCE = .75;
    public static final double STRONG_INDICATION_MIN_CONFIDENCE = .75;
    public static final double CONFIDENCE_MAX = 1;

    final Double valueMin;
    final Double valueMax;
    final Double confidenceMin;
    final Double confidenceMax;

    public IndicationMatcher(Double valueMin, Double valueMax, Double confidenceMin, Double confidenceMax)
    {
        this.valueMin = valueMin;
        this.valueMax = valueMax;
        this.confidenceMin = confidenceMin;
        this.confidenceMax = confidenceMax;
    }

    @Override
    public boolean matchesSafely(Indication indication)
    {
        if (valueMin != null && ! scoreValueEqualToOrGreaterThan(indication.value, valueMin)) return false;
        if (valueMax != null && ! scoreValueEqualToOrLessThan(indication.value, valueMax)) return false;
        if (confidenceMin != null && ! scoreValueEqualToOrGreaterThan(indication.confidence, confidenceMin)) return false;
        if (confidenceMax != null && ! scoreValueEqualToOrLessThan(indication.confidence, confidenceMax)) return false;
        return true;
    }

    public void describeTo(Description description)
    {
        StringJoiner stringJoiner = new StringJoiner(", ");
        if (valueMin != null) stringJoiner.add("a value equal to or greater than " + valueMin);
        if (valueMax != null) stringJoiner.add("a value equal to or less than " + valueMax);
        if (confidenceMin != null) stringJoiner.add("a confidence equal to or greater than " + confidenceMin);
        if (confidenceMax != null) stringJoiner.add("a confidence equal to or less than " + confidenceMax);
        description.appendText("A indication with " + stringJoiner.toString());
    }

    @Factory
    public static <T> Matcher<Indication> isValidIndication()
    {
        return new IndicationMatcher(VALUE_MIN, VALUE_MAX, CONFIDENCE_MIN, CONFIDENCE_MAX);
    }

    @Factory
    public static <T> Matcher<Indication> hasMaxValue()
    {
        return new IndicationMatcher(1d, null, null, null);
    }

    @Factory
    public static <T> Matcher<Indication> hasHighValue()
    {
        return new IndicationMatcher(HIGH_VALUE_MIN, null, null, null);
    }

    @Factory
    public static <T> Matcher<Indication> hasMediumValue()
    {
        return new IndicationMatcher(MEDIUM_VALUE_MIN, MEDIUM_VALUE_MAX, null, null);
    }

    @Factory
    public static <T> Matcher<Indication> hasNeutralValue()
    {
        return new IndicationMatcher(NEUTRAL_VALUE_MIN, NEUTRAL_VALUE_MAX, null, null);
    }

    @Factory
    public static <T> Matcher<Indication> hasLowValue()
    {
        return new IndicationMatcher(null, LOW_VALUE_MAX, null, null);
    }

    @Factory
    public static <T> Matcher<Indication> hasZeroValue()
    {
        return new IndicationMatcher(null, 0d, null, null);
    }

    @Factory
    public static <T> Matcher<Indication> hasMaxConfidence()
    {
        return new IndicationMatcher(null, null, 1d, null);
    }

    @Factory
    public static <T> Matcher<Indication> hasHighConfidence()
    {
        return new IndicationMatcher(null, null, STRONG_INDICATION_MIN_CONFIDENCE, null);
    }

    @Factory
    public static <T> Matcher<Indication> hasMediumConfidence()
    {
        return new IndicationMatcher(null, null, MEDIUM_INDICATION_MIN_CONFIDENCE, MEDIUM_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Indication> hasLowConfidence()
    {
        return new IndicationMatcher(null, null, WEAK_INDICATION_MIN_CONFIDENCE, WEAK_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Indication> hasZeroOrLowConfidence()
    {
        return new IndicationMatcher(null, null, null, MEDIUM_INDICATION_MIN_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Indication> hasZeroConfidence()
    {
        return new IndicationMatcher(null, null, null, 0d);
    }

    @Factory
    public static <T> Matcher<Indication> isIndicationOfPositivePreference()
    {
        return new IndicationMatcher(HIGH_VALUE_MIN, null, WEAK_INDICATION_MIN_CONFIDENCE, null);
    }

    @Factory
    public static <T> Matcher<Indication> isWeakIndicationOfPositivePreference()
    {
        return new IndicationMatcher(HIGH_VALUE_MIN, null, WEAK_INDICATION_MIN_CONFIDENCE, WEAK_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Indication> isMediumIndicationOfPositivePreference()
    {
        return new IndicationMatcher(HIGH_VALUE_MIN, null, MEDIUM_INDICATION_MIN_CONFIDENCE, MEDIUM_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Indication> isStrongIndicationOfPositivePreference()
    {
        return new IndicationMatcher(HIGH_VALUE_MIN, null, STRONG_INDICATION_MIN_CONFIDENCE, null);
    }

    @Factory
    public static <T> Matcher<Indication> isIndicationOfNegativePreference()
    {
        return new IndicationMatcher(null, LOW_VALUE_MAX, WEAK_INDICATION_MIN_CONFIDENCE, null);
    }

    @Factory
    public static <T> Matcher<Indication> isWeakIndicationOfNegativePreference()
    {
        return new IndicationMatcher(null, LOW_VALUE_MAX, WEAK_INDICATION_MIN_CONFIDENCE, WEAK_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Indication> isMediumIndicationOfNegativePreference()
    {
        return new IndicationMatcher(null, LOW_VALUE_MAX, MEDIUM_INDICATION_MIN_CONFIDENCE, MEDIUM_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Indication> isStrongIndicationOfNegativePreference()
    {
        return new IndicationMatcher(null, LOW_VALUE_MAX, STRONG_INDICATION_MIN_CONFIDENCE, null);
    }

    @Factory
    public static <T> Matcher<Indication> isNeglegibleIndicationOfAnything()
    {
        return anyOf(not(isValidIndication()), new IndicationMatcher(null, null, null, .05));
    }

    @Factory
    public static <T> Matcher<Indication> isMaxIndication()
    {
        return new IndicationMatcher(1.0, null, 1.0, null);
    }

    @Factory
    public static <T> Matcher<Indication> isUnknown()
    {
        return new IndicationMatcher(null, null, 0.0, 0.0);
    }
}
