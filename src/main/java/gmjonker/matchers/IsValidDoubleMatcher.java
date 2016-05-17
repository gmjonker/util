package gmjonker.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Verifies that a double is not NaN
 */
public class IsValidDoubleMatcher extends TypeSafeMatcher<Double>
{
    @Override
    public boolean matchesSafely(Double value)
    {
        return ! Double.isNaN(value);
    }

    public void describeTo(Description description)
    {
        description.appendText("a double with a value");
    }

    @Factory
    public static Matcher<Double> isValidDouble()
    {
        return new IsValidDoubleMatcher();
    }
}
