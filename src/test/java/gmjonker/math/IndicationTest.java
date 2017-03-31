package gmjonker.math;

import org.hamcrest.CoreMatchers;
import org.junit.*;

import java.util.Collections;
import java.util.List;

import static gmjonker.TestUtil.ind;
import static gmjonker.matchers.ScoreValueEqualityMatcher.equalsScoreValue;
import static gmjonker.math.NaType.NA;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
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
        assertThat(new Indication( 1 , 1 ).deriveDouble01(neutralValue), equalsScoreValue(1));
        assertThat(new Indication( 0 , 1 ).deriveDouble01(neutralValue), equalsScoreValue(neutralValue));
        assertThat(new Indication(-1 , 1 ).deriveDouble01(neutralValue), equalsScoreValue(0));

        assertThat(new Indication( 1 , 0 ).deriveDouble01(neutralValue), equalsScoreValue(neutralValue));
        assertThat(new Indication( 0 , 0 ).deriveDouble01(neutralValue), equalsScoreValue(neutralValue));
        assertThat(new Indication(-1 , 0 ).deriveDouble01(neutralValue), equalsScoreValue(neutralValue));

        assertThat(new Indication( 1 ,.5 ).deriveDouble01(.5), equalsScoreValue(.75));
        assertThat(new Indication( 0 ,.5 ).deriveDouble01(.5), equalsScoreValue(.5));
        assertThat(new Indication(-1 ,.5 ).deriveDouble01(.5), equalsScoreValue(.25));

        assertThat(new Indication( .5, 1 ).deriveDouble01(.2), equalsScoreValue(.6));
        assertThat(new Indication( .1, 1 ).deriveDouble01(.2), equalsScoreValue(.28));
        assertThat(new Indication(-.5, 1 ).deriveDouble01(.2), equalsScoreValue(.1));
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
        assertThat(indications.get(2), CoreMatchers.is(new Indication(.1, .1)));
        assertThat(indications.get(3), CoreMatchers.is(new Indication(.2, .2)));
        assertThat(indications.get(4), CoreMatchers.is(new Indication(.1, 1)));
        assertThat(indications.get(5), CoreMatchers.is(Indication.CERTAINTY));
        System.out.println("indications = " + indications);
    }

    @Test
    public void match() throws Exception
    {
        assertThat(ind(1, 1).match(ind(1, 1)), equalTo(ind(1, 1)));
        assertThat(ind(1, 1).match(ind(1, 0.2)), equalTo(ind(1, 0.2)));
        assertThat(ind(1, 1).match(ind(-1, 1)), equalTo(ind(-1, 1)));
        assertThat(ind(1, 1).match(ind(0.2, 1)), equalTo(ind(0.2, 1)));
        assertThat(ind(1, 1).match(ind(0, 1)), equalTo(ind(0, 1)));
        assertThat(ind(1, 1).match(ind(0.2, 0.2)), equalTo(ind(0.2, 0.2)));
        assertThat(ind(0.5, 0.5).match(ind(-0.5, 0.5)), equalTo(ind(0, 0.25)));
        assertThat(ind(1, 0.8).match(ind(0.8, 1)), equalTo(ind(0.8, 0.8)));
        assertThat(ind(0, 1).match(ind(0, 1)), equalTo(ind(1, 1)));
        assertThat(ind(1, 0).match(ind(1, 0)), equalTo(ind(1, 0)));
        assertThat(ind(0, 1).match(ind(0.5, 1)), equalTo(ind(0.5, 1)));
    }

    @Test
    public void matchPositiveBiased() throws Exception
    {
        assertThat(ind(1, 1).matchPositivelyBiased(ind(1, 1)), equalTo(ind(1, 1)));
        assertThat(ind(1, 1).matchPositivelyBiased(ind(1, 0.2)), equalTo(ind(1, 0.2)));
        assertThat(ind(1, 1).matchPositivelyBiased(ind(-1, 1)), equalTo(ind(-1, 1)));
        assertThat(ind(1, 1).matchPositivelyBiased(ind(0.2, 1)), equalTo(ind(0.12, 1)));
        assertThat(ind(1, 1).matchPositivelyBiased(ind(0, 1)), equalTo(ind(0, 1)));
        assertThat(ind(1, 1).matchPositivelyBiased(ind(0.2, 0.2)), equalTo(ind(0.12, 0.2)));
        assertThat(ind(0.5, 0.5).matchPositivelyBiased(ind(-0.5, 0.5)), equalTo(ind(0, 0.25)));
        assertThat(ind(1, 0.8).matchPositivelyBiased(ind(0.8, 1)), equalTo(ind(0.72, 0.8)));
        assertThat(ind(0, 1).matchPositivelyBiased(ind(0, 1)), equalTo(ind(0, 1)));
        assertThat(ind(1, 0).matchPositivelyBiased(ind(1, 0)), equalTo(ind(1, 0)));
        assertThat(ind(0, 1).matchPositivelyBiased(ind(0.5, 1)), equalTo(ind(0.125, 1)));
    }

    @Test
    public void corr() throws Exception
    {
        assertThat(ind(1  , 1  ).corr(ind(1   , 1  )), equalTo(ind(1    , 1   )));
        assertThat(ind(1  , 1  ).corr(ind(1   , 0.2)), equalTo(ind(1    , 0.2 )));
        assertThat(ind(1  , 1  ).corr(ind(-1  , 1  )), equalTo(ind(-1   , 1   )));
        assertThat(ind(1  , 1  ).corr(ind(0.2 , 1  )), equalTo(ind(0.2  , 1   )));
        assertThat(ind(1  , 1  ).corr(ind(0   , 1  )), equalTo(ind(0    , 1   )));
        assertThat(ind(1  , 1  ).corr(ind(0.2 , 0.2)), equalTo(ind(0.2  , 0.2 )));
        assertThat(ind(0.5, 0.5).corr(ind(-0.5, 0.5)), equalTo(ind(-.25 , 0.25)));
        assertThat(ind(1  , 0.8).corr(ind(0.8 , 1  )), equalTo(ind(0.8  , 0.8 )));
        assertThat(ind(0  , 1  ).corr(ind(0   , 1  )), equalTo(ind(0    , 1   )));
        assertThat(ind(1  , 0  ).corr(ind(1   , 0  )), equalTo(ind(1    , 0   )));
        assertThat(ind(0  , 1  ).corr(ind(0.5 , 1  )), equalTo(ind(0    , 1   )));
    }
}