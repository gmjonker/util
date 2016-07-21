package gmjonker.matchers;

import org.junit.*;

import static gmjonker.matchers.IsSortedByMatcher.isSortedReverselyOn;
import static gmjonker.matchers.IsSortedByMatcher.isSortedOn;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class IsSortedByMatcherTest
{
    @Test
    public void test()
    {
        assertThat(asList("jan", "piet", "karel"), isSortedOn(String::length));
        assertThat(asList("piet", "karel", "jan"), not(isSortedOn(String::length)));
        assertThat(asList("karel", "piet", "jan"), isSortedReverselyOn(String::length));
        assertThat(asList("piet", "karel", "jan"), not(isSortedReverselyOn(String::length)));
    }
}