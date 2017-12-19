package gmjonker.assertjassertions;

import gmjonker.math.Indication;
import org.assertj.core.api.AbstractAssert;

import static gmjonker.math.GeneralMath.abs;

public class IndicationAssert extends AbstractAssert<IndicationAssert, Indication> {
    private static final double EPSILON = .00001;

    public IndicationAssert(Indication actual) {
        super(actual, IndicationAssert.class);
    }

    // 3 - A fluent entry point to your specific assertion class, use it with static import.
    public static IndicationAssert assertThat(Indication actual) {
        return new IndicationAssert(actual);
    }

    // 4 - a specific assertion !
    public IndicationAssert equals(Indication other) {
        // check that actual Indication we want to make assertions on is not null.
        isNotNull();

        // check condition
        if ( ! (abs(actual.getValue() - other.getValue()) < EPSILON &&
                abs(actual.getConfidence() - other.getConfidence()) < EPSILON)) {
            failWithMessage("Expected indication to be <%s> but was <%s>", other.toLongString(), actual.toLongString());
        }

        // return the current assertion for method chaining
        return this;
    }
}
