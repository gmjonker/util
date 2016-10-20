package gmjonker.matchers;

import gmjonker.math.Score;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.StringJoiner;

import static gmjonker.math.NaType.NA;
import static gmjonker.math.NaType.isValue;
import static gmjonker.math.Score.NEUTRAL_SCORE;
import static gmjonker.util.ScoreValueUtil.scoreValueEqualToOrGreaterThan;
import static gmjonker.util.ScoreValueUtil.scoreValueEqualToOrLessThan;
import static java.lang.Double.isNaN;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AnyOf.anyOf;

/**
 */
@Deprecated
@SuppressWarnings("WeakerAccess")
public class ScoreMatcher extends TypeSafeMatcher<Score>
{
    public static final double VALUE_MIN = 0;
    public static final double LOW_VALUE_MAX = .4;
    public static final double MEDIUM_VALUE_MIN = .4;
    public static final double MEDIUM_VALUE_MAX = .8;
    public static final double NEUTRAL_VALUE_MIN = NEUTRAL_SCORE - .2;
    public static final double NEUTRAL_VALUE_MAX = NEUTRAL_SCORE + .2;
    public static final double HIGH_VALUE_MIN = .8;
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

    public ScoreMatcher(Double valueMin, Double valueMax, Double confidenceMin, Double confidenceMax)
    {
        this.valueMin = valueMin;
        this.valueMax = valueMax;
        this.confidenceMin = confidenceMin;
        this.confidenceMax = confidenceMax;
    }

    @Override
    public boolean matchesSafely(Score score)
    {
        if (valueMin != null && valueMax != null && isNaN(valueMin) && isNaN(valueMax) && ! isNaN(score.value)) return false;
        if (confidenceMin != null && confidenceMax != null && isNaN(confidenceMin) && isNaN(confidenceMax) && ! isNaN(score.confidence)) return false;
        if (valueMin != null && isValue(valueMin) && ! scoreValueEqualToOrGreaterThan(score.value, valueMin)) return false;
        if (valueMax != null && isValue(valueMax) && ! scoreValueEqualToOrLessThan(score.value, valueMax)) return false;
        if (confidenceMin != null && isValue(confidenceMin) && ! scoreValueEqualToOrGreaterThan(score.confidence, confidenceMin)) return false;
        if (confidenceMax != null && isValue(confidenceMax) && ! scoreValueEqualToOrLessThan(score.confidence, confidenceMax)) return false;
        return true;
    }

    public void describeTo(Description description)
    {
        StringJoiner stringJoiner = new StringJoiner(", ");
        if (valueMin != null) stringJoiner.add("a value equal to or greater than " + valueMin);
        if (valueMax != null) stringJoiner.add("a value equal to or less than " + valueMax);
        if (confidenceMin != null) stringJoiner.add("a confidence equal to or greater than " + confidenceMin);
        if (confidenceMax != null) stringJoiner.add("a confidence equal to or less than " + confidenceMax);
        description.appendText("A score with " + stringJoiner.toString());
    }

    @Factory
    public static <T> Matcher<Score> isValidScore()
    {
        return new ScoreMatcher(VALUE_MIN, VALUE_MAX, CONFIDENCE_MIN, CONFIDENCE_MAX);
    }

    @Factory
    public static <T> Matcher<Score> hasMaxValue()
    {
        return new ScoreMatcher(1d, null, null, null);
    }

    @Factory
    public static <T> Matcher<Score> hasHighValue()
    {
        return new ScoreMatcher(HIGH_VALUE_MIN, null, null, null);
    }

    @Factory
    public static <T> Matcher<Score> hasMediumValue()
    {
        return new ScoreMatcher(MEDIUM_VALUE_MIN, MEDIUM_VALUE_MAX, null, null);
    }

    @Factory
    public static <T> Matcher<Score> hasNeutralValue()
    {
        return new ScoreMatcher(NEUTRAL_VALUE_MIN, NEUTRAL_VALUE_MAX, null, null);
    }

    @Factory
    public static <T> Matcher<Score> hasLowValue()
    {
        return new ScoreMatcher(null, LOW_VALUE_MAX, null, null);
    }

    @Factory
    public static <T> Matcher<Score> hasZeroValue()
    {
        return new ScoreMatcher(null, 0d, null, null);
    }

    @Factory
    public static <T> Matcher<Score> hasMaxConfidence()
    {
        return new ScoreMatcher(null, null, 1d, null);
    }

    @Factory
    public static <T> Matcher<Score> hasHighConfidence()
    {
        return new ScoreMatcher(null, null, STRONG_INDICATION_MIN_CONFIDENCE, null);
    }

    @Factory
    public static <T> Matcher<Score> hasMediumConfidence()
    {
        return new ScoreMatcher(null, null, MEDIUM_INDICATION_MIN_CONFIDENCE, MEDIUM_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Score> hasLowConfidence()
    {
        return new ScoreMatcher(null, null, WEAK_INDICATION_MIN_CONFIDENCE, WEAK_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Score> hasZeroOrLowConfidence()
    {
        return new ScoreMatcher(null, null, null, MEDIUM_INDICATION_MIN_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Score> hasZeroConfidence()
    {
        return new ScoreMatcher(null, null, null, 0d);
    }

    @Factory
    public static <T> Matcher<Score> isIndicationOfPositivePreference()
    {
        return new ScoreMatcher(HIGH_VALUE_MIN, null, WEAK_INDICATION_MIN_CONFIDENCE, null);
    }

    @Factory
    public static <T> Matcher<Score> isWeakIndicationOfPositivePreference()
    {
        return new ScoreMatcher(HIGH_VALUE_MIN, null, WEAK_INDICATION_MIN_CONFIDENCE, WEAK_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Score> isMediumIndicationOfPositivePreference()
    {
        return new ScoreMatcher(HIGH_VALUE_MIN, null, MEDIUM_INDICATION_MIN_CONFIDENCE, MEDIUM_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Score> isStrongIndicationOfPositivePreference()
    {
        return new ScoreMatcher(HIGH_VALUE_MIN, null, STRONG_INDICATION_MIN_CONFIDENCE, null);
    }

    @Factory
    public static <T> Matcher<Score> isIndicationOfNegativePreference()
    {
        return new ScoreMatcher(null, LOW_VALUE_MAX, WEAK_INDICATION_MIN_CONFIDENCE, null);
    }

    @Factory
    public static <T> Matcher<Score> isWeakIndicationOfNegativePreference()
    {
        return new ScoreMatcher(null, LOW_VALUE_MAX, WEAK_INDICATION_MIN_CONFIDENCE, WEAK_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Score> isMediumIndicationOfNegativePreference()
    {
        return new ScoreMatcher(null, LOW_VALUE_MAX, MEDIUM_INDICATION_MIN_CONFIDENCE, MEDIUM_INDICATION_MAX_CONFIDENCE);
    }

    @Factory
    public static <T> Matcher<Score> isStrongIndicationOfNegativePreference()
    {
        return new ScoreMatcher(null, LOW_VALUE_MAX, STRONG_INDICATION_MIN_CONFIDENCE, null);
    }

    @Factory
    public static <T> Matcher<Score> isNeglegibleIndicationOfAnything()
    {
        return anyOf(not(isValidScore()), new ScoreMatcher(null, null, null, .05));
    }

    @Factory
    public static <T> Matcher<Score> isMaxScore()
    {
        return new ScoreMatcher(1.0, null, 1.0, null);
    }

    @Factory
    public static <T> Matcher<Score> isUnknown()
    {
        return new ScoreMatcher(NA, NA, NA, NA);
    }
}
