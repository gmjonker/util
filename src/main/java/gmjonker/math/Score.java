package gmjonker.math;

import gmjonker.util.FormattingUtil;
import gmjonker.util.LambdaLogger;
import lombok.Getter;

import java.util.List;

import static gmjonker.math.GeneralMath.abs;
import static gmjonker.math.NaType.NA;
import static gmjonker.math.NaType.isValue;
import static gmjonker.math.Range.from01toM11;
import static gmjonker.math.ScoreMath.combine01;
import static gmjonker.util.FormattingUtil.asPercentageTwoSpaces;
import static gmjonker.util.FormattingUtil.toMicroFormatABC;
import static gmjonker.util.FormattingUtil.toMicroFormatM01;
import static gmjonker.util.ScoreValueUtil.scoreValueEquals;

/**
 * A tuple of a value and a confidence. The value is a point estimation of the true value of some variable, confidence
 * is a measure of the probability of the point estimation being true.
 * <ul>
 *     <li>confidence = 0: no indication</li>
 *     <li>0 &lt; confidence &lt;= .25: weak indication</li>
 *     <li>.25 &lt; confidence &lt;= .75: medium indication</li>
 *     <li>.75 &lt; confidence: strong indication</li>
 * </ul>
 */
@Deprecated
public class Score
{
    // What we consider the neutral score. Scores above this score indicate a positive preference, scores below this
    // score indicate a negative preference.
    // This is actually problematic, because the neutral score can differ per application.
    // We set it to the "Pimmr" value, as that one is used virtually everywhere.
    @Deprecated
    public static final double NEUTRAL_SCORE = Range.tenBasedScoreToScore(6.5); // = .611111111111111

    public static final Score NA_SCORE = new Score(NA, NA);
    public static final Score UNKNOWN = new Score(NA, 0);
    public static final Score MAX = new Score(1, 1);
    public static final Score MIN = new Score(0, 1);

    @Getter public final double value;
    @Getter public final double confidence;
    
    protected static final LambdaLogger log = new LambdaLogger(Score.class);

    public Score(double value, double confidence)
    {
        this.value = value;
        this.confidence = confidence;
    }

    /** Converts from (-1,1) range to (0,1) range. **/
    public static Score fromMinusOneOneRange(double value, double confidence)
    {
        return new Score(Range.fromM11to01(value, NEUTRAL_SCORE), confidence);
    }

    public static boolean isValidScore(Score score)
    {
        return score != null && score.isValid();
    }

    /**
     * Has valid value and valid confidence.
     */
    public boolean isValid()
    {
        return isValue(value) && isValue(confidence);
    }

    /** Is valid and confidence > 0. **/
    public boolean isIndication()
    {
        return isValid() && confidence > 0;
    }

    /**
     * Values are assumed to be in (-1,1)
     */
    public Score combineWith01(Score score)
    {
        Score result = combine01(this, score);
        log.trace("Combining {} and {} into {}", this, score, result);
        return result;
    }

    /**
     * Derives a double in range (0,1) from a score with value in range (0, 1).
     * Note: will be removed when we go to (-1,1) range
     */
    public double deriveDouble0101()
    {
        if ( ! isValue(value) || ! isValue(confidence))
            return NA;
        return NEUTRAL_SCORE + (value - NEUTRAL_SCORE) * confidence;
    }

    /**
     * Derives a double in range (-1,1) from a score with value in range (0, 1).
     * Note: will be removed when we go to (-1,1) range
     */
    public double deriveDouble01M11()
    {
        if ( ! isValue(value) || ! isValue(confidence))
            return NA;
        return from01toM11(value, NEUTRAL_SCORE) * confidence;
    }

    /**
     * Derives a double in range (0,1) from a score with value in range (-1, 1).
     */
    public double deriveDoubleM1101()
    {
        if ( ! isValue(value) || ! isValue(confidence))
            return NA;
        return Range.fromM11to01(value, NEUTRAL_SCORE) * confidence;
    }

    /**
     * Derives a double in range (-1,1) from a score with value in range (-1, 1).
     */
    public double deriveDoubleM11M11()
    {
        if ( ! isValue(value) || ! isValue(confidence))
            return NA;
        return value * confidence;
    }

    public boolean isWeakOrNeutral()
    {
        return abs(deriveDouble01M11()) < .25;
    }

    public Score withConfidence(double confidence)
    {
        return new Score(this.value, confidence);
    }

    public Score multiplyConfidence(double factor)
    {
        return new Score(value, confidence * factor);
    }

    /**
     * Converts this Score, assumed to have value range (0,1), to an Indication (which has by definition range (-1,1))
     */
    public Indication toIndication01()
    {
        return new Indication(from01toM11(value, NEUTRAL_SCORE), confidence);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return scoreValueEquals(score.value, value) && scoreValueEquals(score.confidence, confidence);
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
        return asPercentageTwoSpaces(value).trim() + "/" + asPercentageTwoSpaces(confidence).trim()
                + "(" + asPercentageTwoSpaces(deriveDouble0101()) + ")";
    }

    public String toShortString()
    {
        return FormattingUtil.asPercentage(value) + "/" + FormattingUtil.asPercentage(confidence);
    }

    public String toAlignedString()
    {
        return asPercentageTwoSpaces(value) + "/" + asPercentageTwoSpaces(confidence);
    }

    public String toPicoString()
    {
        return toMicroFormatM01(value) + toMicroFormatABC(confidence);
    }

    public static String printScoresAligned(List<Score> scores)
    {
        String result = "";
        for (Score score : scores)
            result += "  " + score.toAlignedString() + "\n";
        return result;
    }

    public boolean isNa()
    {
        return this.equals(NA_SCORE);
    }

    public static Score[] toPrimitiveScoreArray(List<Score> scoreList)
    {
        Score[] scores = new Score[scoreList.size()];
        for (int i = 0; i < scoreList.size(); i++) {
            Score aScore = scoreList.get(i);
            scores[i] = aScore;
        }
        return scores;
    }
}