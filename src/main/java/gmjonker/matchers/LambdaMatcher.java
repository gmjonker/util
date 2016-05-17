package gmjonker.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import java.util.function.Function;

/**
 * Example usage:
 * <pre>assertThat(myValue, satisfies(x -> x >= 0 && x <= 1));</pre>
 * @param <T>
 */
public class LambdaMatcher<T> extends BaseMatcher<T>
{
    private final Function<T, Boolean> matcher;
    private final String description;

    public LambdaMatcher(Function<T, Boolean> matcher, String description)
    {
        this.matcher = matcher;
        this.description = description;
    }

    public LambdaMatcher(Function<T, Boolean> matcher)
    {
        this.matcher = matcher;
        this.description = "";
    }

    @Override
    public boolean matches(Object argument)
    {
        return matcher.apply((T) argument);
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText(this.description);
        description.appendText("value satisfying the lambda function");
    }

    @Factory
    public static <T> Matcher<T> satisfies(Function<T, Boolean> matcher)
    {
        return new LambdaMatcher<>(matcher);
    }
}
