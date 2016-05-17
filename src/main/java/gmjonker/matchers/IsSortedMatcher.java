package gmjonker.matchers;

import com.google.common.collect.Ordering;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

/**
 * Verifies that a double is not NaN
 */
public class IsSortedMatcher<T extends Comparable> extends TypeSafeMatcher<List<T>>
{
    @Override
    public boolean matchesSafely(List<T> list)
    {
        return Ordering.natural().isOrdered(list);
    }

    public void describeTo(Description description)
    {
        description.appendText("a sorted list");
    }

    @Factory
    public static IsSortedMatcher isSorted()
    {
        return new IsSortedMatcher<>();
    }
}
