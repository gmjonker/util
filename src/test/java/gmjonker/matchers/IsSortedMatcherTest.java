package gmjonker.matchers;

import org.junit.*;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;

public class IsSortedMatcherTest
{
    @Test
    public void isSorted() throws Exception
    {
        assertThat(asList(1, 2, 3), IsSortedMatcher.isSortedNaturally());
        assertThat(asList(3, 2, 1), IsSortedMatcher.isSortedInversly());
    }
}