package gmjonker.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Verifies that a double is a score, i.e., it is in [0..1]
 */
public class IsValidScoreValueMatcher extends TypeSafeMatcher<Double>
{
    @Override
    public boolean matchesSafely(Double value)
    {
        return value >= 0.0 && value <= 1.0;
    }

    public void describeTo(Description description)
    {
        description.appendText("a value between 0 and 1");
    }

    /** Verifies that a double is a score, i.e., it is in [0..1] **/
     @Factory
    public static <T> Matcher<Double> isValidScoreValue()
    {
        return new IsValidScoreValueMatcher();
    }

}
