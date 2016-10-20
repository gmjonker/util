package gmjonker.matchers;

import gmjonker.math.Score;
import org.junit.*;

import static org.hamcrest.core.IsNot.not;

public class ScoreMatcherTest
{
    @Test
    public void isUnknownn() throws Exception
    {
        Assert.assertThat(Score.NA_SCORE, ScoreMatcher.isUnknown());
        Assert.assertThat(null, not(ScoreMatcher.isUnknown()));
        Assert.assertThat(Score.MIN, not(ScoreMatcher.isUnknown()));
        Assert.assertThat(Score.MAX, not(ScoreMatcher.isUnknown()));
    }

}