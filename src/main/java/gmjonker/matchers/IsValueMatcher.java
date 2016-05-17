package gmjonker.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static gmjonker.math.NaType.isValue;

/**
 * Verifies that a double is a value, i.e., not NA
 */
public class IsValueMatcher extends TypeSafeMatcher<Double>
{
    @Override
    public boolean matchesSafely(Double value)
    {
        return isValue(value);
    }

    public void describeTo(Description description)
    {
        description.appendText("a not-NA double");
    }

    @Factory
    public static Matcher<Double> isValueMatch()
    {
        return new IsValueMatcher();
    }

}
