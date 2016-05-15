package gmjonker.matchers;

import gmjonker.util.ScoreValueUtil;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static gmjonker.util.ScoreValueUtil.scoreValueEquals;

/**
 * Verifies that a score double is equal to a value. Using this matcher saves having to supply an epsilon all the time.
 */
@SuppressWarnings("WeakerAccess")
public class ScoreValueEqualityMatcher extends TypeSafeMatcher<Double>
{
    public final double value;

    public ScoreValueEqualityMatcher(double value)
    {
        this.value = value;
    }

    @Override
    public boolean matchesSafely(Double value)
    {
        return scoreValueEquals(value, this.value);
    }

    public void describeTo(Description description)
    {
        description.appendText("a value between " + (value - ScoreValueUtil.SCORE_VALUE_EPSILON) + " and " +
                (value + ScoreValueUtil.SCORE_VALUE_EPSILON));
    }

    /**
     * Verifies that a score double is equal to a value. Using this matcher saves having to supply an epsilon all the time.
     */
     @Factory
    public static <T> Matcher<Double> equalsScoreValue(double value)
    {
        return new ScoreValueEqualityMatcher(value);
    }

}
