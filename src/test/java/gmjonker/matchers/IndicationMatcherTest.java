package gmjonker.matchers;

import gmjonker.math.Indication;
import org.junit.*;

import static org.hamcrest.CoreMatchers.is;

public class IndicationMatcherTest
{
    @Test
    public void closeTo() throws Exception
    {
        Assert.assertThat(IndicationMatcher.closeTo(.5, .6, .00001).matches(new Indication(.5, .6)), is(true));
        Assert.assertThat(IndicationMatcher.closeTo(.5, .6, .00001).matches(new Indication(.5000000001, .599999999)), is(true));
        Assert.assertThat(IndicationMatcher.closeTo(.5, .6, .00001).matches(new Indication(.4, .6)), is(false));
        Assert.assertThat(IndicationMatcher.closeTo(.5, .6, .00001).matches(new Indication(.5, .7)), is(false));
    }
}