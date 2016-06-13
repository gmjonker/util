package gmjonker.matchers;

import gmjonker.math.Indication;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.StringJoiner;

import static gmjonker.util.ScoreValueUtil.scoreValueEqualToOrGreaterThan;
import static gmjonker.util.ScoreValueUtil.scoreValueEqualToOrLessThan;

public class IndicationScoreMatcher extends TypeSafeMatcher<Indication>
{
    public static final double SCORE_MIN = -1;
    public static final double LOW_SCORE_MAX = -.4;
    public static final double NEUTRAL_SCORE_MIN = -.2;
    public static final double NEUTRAL_SCORE_MAX = .2;
    public static final double MIDDLE_SCORE_MIN = -.4;
    public static final double MIDDLE_SCORE_MAX = .4;
    public static final double HIGH_SCORE_MIN = .4;
    public static final double SCORE_MAX = 1;

    final Double valueMin;
    final Double valueMax;

    public IndicationScoreMatcher(Double valueMin, Double valueMax)
    {
        this.valueMin = valueMin;
        this.valueMax = valueMax;
    }

    @Override
    protected boolean matchesSafely(Indication indication)
    {
        double score = indication.deriveDouble();
        if (valueMin != null && ! scoreValueEqualToOrGreaterThan(score, valueMin)) return false;
        if (valueMax != null && ! scoreValueEqualToOrLessThan(score, valueMax)) return false;
        return true;
    }

    @Override
    public void describeTo(Description description)
    {
        StringJoiner stringJoiner = new StringJoiner(", ");
        if (valueMin != null) stringJoiner.add("a derived score equal to or greater than " + valueMin);
        if (valueMax != null) stringJoiner.add("a derived score equal to or less than " + valueMax);
        description.appendText("A indication with " + stringJoiner.toString());
    }

    @Factory
    public static <T> Matcher<Indication> isMaxScore()
    {
        return new IndicationScoreMatcher(SCORE_MAX, null);
    }

    @Factory
    public static <T> Matcher<Indication> isHighScore()
    {
        return new IndicationScoreMatcher(HIGH_SCORE_MIN, null);
    }

    @Factory
    public static <T> Matcher<Indication> isMiddleScore()
    {
        return new IndicationScoreMatcher(MIDDLE_SCORE_MIN, MIDDLE_SCORE_MAX);
    }

    @Factory
    public static <T> Matcher<Indication> isNeutralScore()
    {
        return new IndicationScoreMatcher(NEUTRAL_SCORE_MIN, NEUTRAL_SCORE_MAX);
    }

    @Factory
    public static <T> Matcher<Indication> isLowScore()
    {
        return new IndicationScoreMatcher(null, LOW_SCORE_MAX);
    }

    @Factory
    public static <T> Matcher<Indication> isMinScore()
    {
        return new IndicationScoreMatcher(null, SCORE_MIN);
    }

}
