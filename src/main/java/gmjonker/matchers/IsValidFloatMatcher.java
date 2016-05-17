package gmjonker.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Verifies that a float is not NaN
 */
public class IsValidFloatMatcher extends TypeSafeMatcher<Float>
{
    @Override
    public boolean matchesSafely(Float value)
    {
        return ! Float.isNaN(value);
    }

    public void describeTo(Description description)
    {
        description.appendText("a float with a value");
    }

    @Factory
    public static Matcher<Float> isValidFloat()
    {
        return new IsValidFloatMatcher();
    }
}
