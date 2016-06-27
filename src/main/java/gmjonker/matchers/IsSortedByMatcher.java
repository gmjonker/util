package gmjonker.matchers;

import com.google.common.collect.Ordering;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class IsSortedByMatcher<T, U extends Comparable<U>> extends TypeSafeMatcher<List<T>>
{
    private final Function<T, U> mapper;
    private final boolean reverse;

    public IsSortedByMatcher(Function<T, U> mapper, boolean reverse)
    {
        this.mapper = mapper;
        this.reverse = reverse;
    }

    @Override
    public boolean matchesSafely(List<T> list)
    {
            if (reverse) {
                Comparator<T> comparator = new Comparator<T>()
                {
                    @Override
                    public int compare(T o1, T o2)
                    {
                        return mapper.apply(o1).compareTo(mapper.apply(o2));
                    }
                };
                return Ordering.from(comparator).reverse().isOrdered(list);
            }
            else {
                Comparator<T> comparator = new Comparator<T>()
                {
                    @Override
                    public int compare(T o1, T o2)
                    {
                        return mapper.apply(o1).compareTo(mapper.apply(o2));
                    }
                };
                return Ordering.from(comparator).isOrdered(list);
            }
    }

    public void describeTo(Description description)
    {
        if (reverse)
            description.appendText("a inversly sorted list after mapping" );
        else
            description.appendText("a sorted list after mapping");
    }

    @Factory
    public static <S, U extends Comparable<U>> IsSortedByMatcher<S,U> isSortedOn(Function<S,U> mapper)
    {
        return new IsSortedByMatcher<>(mapper, false);
    }

    @Factory
    public static <S, U extends Comparable<U>> IsSortedByMatcher<S,U> isSortedInverslyOn(Function<S,U> mapper)
    {
        return new IsSortedByMatcher<>(mapper, true);
    }
}
