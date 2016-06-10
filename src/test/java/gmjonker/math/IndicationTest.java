package gmjonker.math;

import org.hamcrest.CoreMatchers;
import org.junit.*;

import java.util.Collections;
import java.util.List;

import static gmjonker.matchers.ScoreValueEqualityMatcher.equalsScoreValue;
import static gmjonker.math.NaType.NA;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class IndicationTest
{
    @Test
    public void isValidWorksCorrectly()
    {
        assertTrue(new Indication(.1, .2).isValid());
        assertFalse(Indication.NA_INDICATION.isValid());
        assertFalse(new Indication(1, NA).isValid());
        assertFalse(new Indication(NA, 1).isValid());
    }

    @Test
    public void isIndication()
    {
        assertTrue(new Indication(.1, .2).indicatesSomething());
        assertFalse(new Indication(.1, 0).indicatesSomething());
        assertFalse(new Indication(.1, -1).indicatesSomething());
        assertFalse(new Indication(.1, NA).indicatesSomething());
        assertFalse(Indication.NA_INDICATION.indicatesSomething());
    }

    @Test
    public void deriveDoubleWorksCorrectly()
    {
        double neutralValue = .3;
        assertThat(new Indication(1 , 1 ).deriveDouble01(neutralValue), equalsScoreValue(1));
        assertThat(new Indication(1 , 0 ).deriveDouble01(neutralValue), equalsScoreValue(neutralValue));
        assertThat(new Indication(0 , 1 ).deriveDouble01(neutralValue), equalsScoreValue(0));
        assertThat(new Indication(.8, .3).deriveDouble01(neutralValue), equalsScoreValue(neutralValue + (.8 - neutralValue) * .3));
        assertThat(new Indication(.2, .3).deriveDouble01(neutralValue), equalsScoreValue(neutralValue + (.2 - neutralValue) * .3));
    }

    @Test
    public void isWeakOrNeutralWorksCorrectly()
    {
        assertTrue(new Indication(.1, .5).isWeakOrNeutral());
        assertTrue(new Indication(1, 0).isWeakOrNeutral());
        assertTrue(new Indication(1, 0.2).isWeakOrNeutral());
    }

    @Test
    public void equalsWorksCorrectly()
    {
        double a = 1.000001;
        double b = 0.000001;
        assertFalse((a - b) == 1.0);
        assertTrue(new Indication(0, 1).equals(new Indication(0, 1)));
        assertTrue(new Indication(1, 1).equals(new Indication(a - b, a - b)));
        assertTrue(new Indication(NA, NA).equals(new Indication(NA, NA)));
    }

    @Test
    public void isSortable()
    {
        List<Indication> indications = asList(new Indication(.2, .2), new Indication(.1, 1), new Indication(.1, .1),
                Indication.CERTAINTY, Indication.UNKNOWN, Indication.NA_INDICATION);
        Collections.sort(indications);
        Assert.assertThat(indications.get(2), CoreMatchers.is(new Indication(.1, .1)));
        Assert.assertThat(indications.get(3), CoreMatchers.is(new Indication(.2, .2)));
        Assert.assertThat(indications.get(4), CoreMatchers.is(new Indication(.1, 1)));
        Assert.assertThat(indications.get(5), CoreMatchers.is(Indication.CERTAINTY));
        System.out.println("indications = " + indications);
    }
}