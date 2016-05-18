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
    private final boolean reverse;

    public IsSortedMatcher(boolean reverse)
    {
        this.reverse = reverse;
    }

    @Override
    public boolean matchesSafely(List<T> list)
    {
        if (reverse)
            return Ordering.natural().reverse().isOrdered(list);
        else
            return Ordering.natural().isOrdered(list);
    }

    public void describeTo(Description description)
    {
        if (reverse)
            description.appendText("a inversly sorted list");
        else
            description.appendText("a sorted list");
    }

    @Factory
    public static <S extends Comparable> IsSortedMatcher<S> isSortedNaturally()
    {
        return new IsSortedMatcher<S>(false);
    }

    @Factory
    public static <S extends Comparable> IsSortedMatcher<S> isSortedInversly()
    {
        return new IsSortedMatcher<S>(true);
    }
}
