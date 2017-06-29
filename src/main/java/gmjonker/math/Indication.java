package gmjonker.math;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import com.google.common.primitives.Doubles;
import gmjonker.util.LambdaLogger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static gmjonker.math.GeneralMath.*;
import static gmjonker.math.NaType.NA;
import static gmjonker.math.NaType.isValue;
import static gmjonker.util.FormattingUtil.*;
import static gmjonker.util.ScoreValueUtil.scoreValueEquals;

/**
 * A tuple of a value in range (-1,1) and a confidence in range (0,1).
 *
 * <p>The value is a point estimation of the true value of some variable. The range is (-1,1) is chosen so that Indication
 * can easily be used as a preference or a correlation, with value 0 meaning neutral preference or no correlation.
 *
 * <p>Confidence is a measure of the probability of the point estimation being true.
 * <ul>
 *     <li>confidence = 0: no indication</li>
 *     <li>0 &lt; confidence &lt;= .25: weak indication</li>
 *     <li>.25 &lt; confidence &lt;= .75: medium indication</li>
 *     <li>.75 &lt; confidence $lt; 1: strong indication</li>
 *     <li>confidence = 1: certainty</li>
 * </ul>
 *
 * Indication replaces Score, which didn't have a range defined which was inconvenient w.r.t. the neutral score.
 */
@SuppressWarnings("WeakerAccess")
@NoArgsConstructor
public class Indication implements Comparable<Indication>
{
    public static final Indication NA_INDICATION = new Indication(NA, NA);
    public static final Indication UNKNOWN = new Indication(NA, 0);
    public static final Indication NONE = new Indication(0, 0);
    public static final Indication CERTAINTY = new Indication(1, 1);

    @Getter
    @Setter
    public double value;

    @Getter
    @Setter
    public double confidence;

    @Getter
    @Setter
    @JsonIgnore
    public String comment; // can be handy for explanations

    protected static final LambdaLogger log = new LambdaLogger(Indication.class);

    public Indication(double value, double confidence, String comment)
    {
        this.value = value;
        this.confidence = confidence;
        this.comment = comment;
    }

    public Indication(double value, double confidence)
    {
        this(value, confidence, "");
    }
    
    public Indication correct()
    {
        value = limit(value, -1, 1);
        confidence = limit(confidence, 0, 1);
        return this;
    }

    /**
     * Has valid value and valid confidence.
     */
    public static boolean isValidIndication(Indication indication)
    {
        return indication != null && indication.isValid();
    }

    /**
     * Has valid value and valid confidence.
     */
    @JsonIgnore
    public boolean isValid()
    {
        return isValue(value) && isValue(confidence);
    }

    /** Is valid and confidence > 0. **/
    public boolean indicatesSomething()
    {
        return isValid() && value > 0.000000000000001 &&  confidence > 0.000000000000001;
    }

    @JsonIgnore
    public boolean isNa()
    {
        return this.equals(NA_INDICATION);
    }

    @JsonIgnore
    public boolean isMaximal()
    {
        return value >= 1 && confidence >= 1;
    }

    @JsonIgnore
    public boolean isWeakOrNeutral()
    {
        return abs(deriveDouble()) < .25;
    }

    public Indication withConfidence(double confidence)
    {
        return new Indication(this.value, confidence, this.comment);
    }

    public Indication withComment(String comment)
    {
        return new Indication(value, confidence, comment);
    }

    public Indication multiplyConfidence(double factor)
    {
        return new Indication(value, confidence * factor, this.comment);
    }

    public Indication multiply(double valueFactor, double confidenceFactor)
    {
        return new Indication(this.value * valueFactor, this.confidence * confidenceFactor, this.comment);
    }

    public void multiplyInPlace(double valueFactor, double confidenceFactor)
    {
        this.value *= valueFactor;
        this.confidence *= confidenceFactor;
    }

    /**
     * Simply multiplies respective values and indications.
     */
    public Indication multiplyWith(Indication indication)
    {
        return new Indication(this.value * indication.value, this.confidence * indication.confidence, this.comment);
    }
    
    public Indication diffWith(Indication indication)
    {
        return new Indication(this.value - indication.value, this.confidence - indication.confidence);
    }

    /**
     * Derives a double in range (0,1), or NA if indication doesn't have a value or confidence.
     * @param neutralPoint Value in (0,1) that corresponds with the neutral point (0 in range (-1,1)). For instance: .5
     */
    public double deriveDouble01(double neutralPoint)
    {
        if ( ! isValue(value) || ! isValue(confidence))
            return NA;
        if (value > 0) {
            return neutralPoint + value * confidence * (1 - neutralPoint);
        } else {
            return neutralPoint + value * confidence * neutralPoint;
        }
    }

    /**
     * Derives a double in range (-1,1).
     */
    public double deriveDouble()
    {
        if (confidence == 0)
            return 0;
        if ( ! isValue(value) || ! isValue(confidence))
            return NA;
        return value * confidence;
    }

    public Indication combineWith(Indication indication)
    {
        Indication result = IndicationMath.combine(this, indication);
        log.trace("this: {}", this);
        log.trace("indication: {}", indication);
        log.trace("combined Indication: {}", result);
        return result;
    }

    public Indication combineWithNoDisagreementEffect(Indication indication)
    {
        Indication result = IndicationMath.combineNoDisagreementEffect(this, indication);
        log.trace("this: {}", this);
        log.trace("indication: {}", indication);
        log.trace("combined Indication: {}", result);
        return result;
    }

    /**
     * Measure of how well this indication matches with another indication.
     **/
    public Indication match(Indication that)
    {
        return new Indication(
                1 - abs(this.value - that.value),
                this.confidence * that.confidence
        );
    }

    /**
     * Measure of how well this indication matches with another indication, where two indication must have high 
     * (or low) values to achieve a high (or low) match score. In other words, neutral values can't lead to high
     * match score.
     *
     * Consider two persons' preferences for a band:
     *
     *          person A    person B
     * lyrics   +           -    
     * melody   -           +
     * rating   0           0
     *
     * Although the ratings are equal, person A and B are not 'taste neighbours'.
     *
     *          person A    person B
     * lyrics   +           +    
     * melody   +           +
     * rating   +           +
     *
     *          person A    person B
     * lyrics   -           -    
     * melody   -           -
     * rating   -           -
     *
     * In these two examples, the ratings are equal again, but this time person A and B can be considered taste neighbours.
     * 
     * Note that {@code cov} has the same characteristics.
     */
    public Indication matchPositivelyBiased(Indication that)
    {
        return new Indication(
                (1 - abs(this.value - that.value)) * ((abs(this.value) + abs(that.value)) / 2),
                this.confidence * that.confidence
        );
    }

    public Indication cov(Indication that)
    {
        return this.multiplyWith(that);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Indication indication = (Indication) o;
        return scoreValueEquals(indication.value, value) && scoreValueEquals(indication.confidence, confidence);
    }

    /** Hashcode function generated by IntelliJ. **/
    @Override
    public int hashCode()
    {
        int result;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(confidence);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return toShortString();
//        return asPercentage(value).trim() + "/" + asPercentage(confidence).trim() + "(" + comment + ")";
    }

    public String toFullString()
    {
        return String.format("%.22f/%.22f->%f (%s)", value, confidence, deriveDouble(), comment);
    }

    public String toLongString()
    {
        return String.format("%.5f/%.5f->%.5f (%s)", value, confidence, deriveDouble(), comment);
    }

    public String toShortString()
    {
        return asPercentage(value) + "/" + asPercentage(confidence);
    }

    public String toShortStringWithComment()
    {
        return toShortString() + " (" + comment + ")";
    }

    /** 4A, 9F **/
    public String toMicroString()
    {
        return toMicroFormatM11(value) + toMicroFormatABC(confidence);
    }

    public String toMicroStringWithComment()
    {
        return toMicroFormatM11(value) + toMicroFormatABC(confidence) + "(" + comment + ")";
    }

    /** 1, 6 **/
    public String toNanoString()
    {
        return toMicroFormatM01(deriveDouble01(.5));
    }

    public String toAlignedString()
    {
        return asPercentageTwoSpaces(value) + "/" + asPercentageTwoSpaces(confidence);
    }

    public static String printIndicationsAligned(List<Indication> indications)
    {
        String result = "";
        for (Indication indication : indications)
            result += "  " + indication.toAlignedString() + "\n";
        return result;
    }

    public String serialize()
    {
        String v = String.format("%.5f", value);
        String c = String.format("%.5f", confidence);
        v = v.replaceAll("0*$", "").replaceAll("\\.$", "");
        c = c.replaceAll("0*$", "").replaceAll("\\.$", "");
        return v + "/" + c;
    }

    public static Indication deserialize(String s)
    {
        if (Strings.isNullOrEmpty(s))
            return NA_INDICATION;
        try {
            String[] split = s.split("/");
            Double value = Doubles.tryParse(split[0]);
            Double confidence = Doubles.tryParse(split[1]);
            return new Indication(value != null ? value : NA, confidence != null ? confidence : NA);
        } catch (Exception ex) {
            log.error("Could not parse '{}'", s);
            throw ex;
        }
    }

    public static Indication[] toPrimitiveIndicationArray(List<Indication> indicationList)
    {
        Indication[] indications = new Indication[indicationList.size()];
        for (int i = 0; i < indicationList.size(); i++) {
            Indication indication = indicationList.get(i);
            indications[i] = indication;
        }
        return indications;
    }

    @Override
    public int compareTo(Indication indication)
    {
        if ( ! isValid())
            return -1;
        if ( ! indication.isValid())
            return 1;
        return sign(this.deriveDouble() - indication.deriveDouble());
    }

    public Indication copy()
    {
        return new Indication(value, confidence, comment);
    }

    public Score toScore01()
    {
        if (isNa())
            return Score.NA_SCORE;

        return Score.fromMinusOneOneRange(value, confidence);
    }
}