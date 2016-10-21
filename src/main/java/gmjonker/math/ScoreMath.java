package gmjonker.math;

import gmjonker.util.LambdaLogger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static gmjonker.math.GeneralMath.*;
import static gmjonker.math.NaType.NA;
import static gmjonker.math.NaType.isValue;
import static gmjonker.math.Range.from01toM11;
import static gmjonker.math.Score.NA_SCORE;
import static gmjonker.math.Score.NEUTRAL_SCORE;
import static gmjonker.math.SigmoidMath.logit;
import static gmjonker.math.SigmoidMath.sigmoid;
import static gmjonker.util.CollectionsUtil.allElementsSatisfy;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * Approximate statistical inference on point estimate/confidence measure pairs.
 *
 * <p>The math in this class is not an established model of statistical inference, but rather a common sense approximation
 * of it. It provides a rudimentary way to combine point estimates that are accompanied by a simple confidence measure.
 * Knowledge about underlying probability distributions is not needed. As such, it is not statistically correct,
 * but does provide a significant improvement over not using any measure of confidence at all.
 *
 * <p>Suffixes are used to indicate whether the value is assumed to be in range (-1,1) (M11) or in range (0,1) (01).
 * Note that the (0,1) range methods internally convert to (-1,1) range first.
 */
@Deprecated
public class ScoreMath
{
    private static long totalTime = 0;

    protected static final LambdaLogger log = new LambdaLogger(ScoreMath.class);

    /**
     * Infers a new score based on given scores.
     *
     * <p>Score values in range (0,1)
     **/
    public static Score combine01(Score... scores)
    {
        return combine01(scores, null);
    }

    /**
     * Infers a new score based on given scores.
     *
     * <p>Score values in range (0,1)
     **/
    public static Score combine01(Collection<Score> scores)
    {
        long now = System.nanoTime();
        Score[] scoreArray = new Score[scores.size()];
        Score score = combine01(scores.toArray(scoreArray), null);
        totalTime += System.nanoTime() - now;
        return score;
    }

    /**
     * Infers a new score based on given scores.
     *
     * <p>Score values in range (0,1)
     **/
    public static Score combine01TightAndNoDisagreementEffect(List<Score> scores)
    {
        long now = System.nanoTime();
        Score[] scoreArray = new Score[scores.size()];
        Score score = combine01TightAndNoDisagreementEffect(scores.toArray(scoreArray), null);
        totalTime += System.nanoTime() - now;
        return score;
    }

    /**
     * Infers a new score based on given scores, where scores may be weighted to indicate that some scores should have
     * more weight in the outcome than others.
     *
     * <p>Score values in range (0,1). Weights have no constraints (will be normalized on the fly).
     **/
    public static Score combine01(Score[] scores, @Nullable double[] weights)
    {
        // Convert to (-1,1) range
        Score[] newScores = new Score[scores.length];
        for (int i = 0; i < scores.length; i++) {
            newScores[i] = new Score(from01toM11(scores[i].value, NEUTRAL_SCORE), scores[i].confidence);
        }
        // Combine
        Score combinedScore = combineM11(newScores, weights);
        // Convert back
        return new Score(Range.fromM11to01(combinedScore.value, NEUTRAL_SCORE), combinedScore.confidence);
    }

    /**
     * Infers a new score based on given scores, where scores may be weighted to indicate that some scores should have
     * more weight in the outcome than others.
     *
     * <p>Score values in range (0,1). Weights have no constraints (will be normalized on the fly).
     **/
    public static Score combine01TightAndNoDisagreementEffect(Score[] scores, @Nullable double[] weights)
    {
        // Convert to (-1,1) range
        Score[] newScores = new Score[scores.length];
        for (int i = 0; i < scores.length; i++) {
            newScores[i] = new Score(from01toM11(scores[i].value, NEUTRAL_SCORE), scores[i].confidence);
        }
        // Combine
        Score combinedScore = combineM11TightAndNoDisagreementEffect(newScores, weights);
        // Convert back
        return new Score(Range.fromM11to01(combinedScore.value, NEUTRAL_SCORE), combinedScore.confidence);
    }

    /**
     * Infers a new score based on given scores.
     *
     * <p>Score values in range (-1,1)
     **/
    public static Score combineM11(Score... scores)
    {
        return combineM11(scores, null);
    }

    /**
     * Infers a new score based on given scores.
     *
     * <p>Score values in range (-1,1)
     **/
    public static Score combineM11(List<Score> scores)
    {
        Score[] scoreArray = new Score[scores.size()];
        return combineM11(scores.toArray(scoreArray), null);
    }

    /**
     * Infers a new score based on given scores, where scores may be weighted to indicate that some scores should have
     * more weight in the outcome than others.
     *
     * <p>Disagreement among the scores has a negative effect on confidence.</p>
     *
     * <p>Score values in range (-1,1). Weights have no constraints (will be normalized on the fly).
     **/
    public static Score combineM11(Score[] scores, @Nullable double[] weights)
    {
        if (isEmpty(scores) || allElementsSatisfy(scores, Score::isNa))
            return NA_SCORE;

        log.trace("combineM11({}, {})", () -> Arrays.toString(scores), () -> Arrays.toString(weights));
        double[] values = new double[scores.length];
        double[] confidences = new double[scores.length];
        double[] adjustedWeights = new double[scores.length];
        double maxWeight = weights == null ? NA : max(weights);
        for (int i = 0; i < scores.length; i++) {
            values[i] = scores[i].value;
            // If max weight > 1, adjust all weights such that max weight == 1, otherwise just leave the weights as is
            adjustedWeights[i] = weights == null ? 1
                                                 : maxWeight > 1 ? weights[i] * 1 / maxWeight : weights[i];
            confidences[i] = scores[i].confidence * adjustedWeights[i];
        }

        if (sum(confidences) == 0)
            return new Score(mean(values), 0);

        double weightedMean = weightedMeanIgnoreNAs(values, confidences);
        log.trace("    wgtdMn:{}", weightedMean);

        double totalConf = calculateCombinedConfidenceM11(values, confidences);

        Score result = new Score(weightedMean, totalConf);
        log.trace("    lgttc: {}", totalConf);
        log.trace("    totco: {}", totalConf);
        log.trace("    rs-11: {}", result);
        return result;
    }

    private static double calculateCombinedConfidenceM11(double[] values, double[] confidences)
    {
        final double sigmoidRangeLow = -1.1;
        final double sigmoidRangeHigh = 1.1;

        double weightedMean = weightedMeanIgnoreNAs(values, confidences);

        // We take the inverse logit of confidences, then accumulate them, then take the sigmoid. This results in the
        // effect of two confidences of .9 adding up to something like .98 for instance (if the values are equal or similar).
        double[] logitConfidences = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            logitConfidences[i] = logit(confidences[i], sigmoidRangeLow, sigmoidRangeHigh);
        }

        double totalConf = 0;
        for (int i = 0; i < values.length; i++) {
            if ( ! isValue(values[i]) || ! isValue(confidences[i]) )
                continue;
            // Diff is how much this score is disagreeing with the average score
            double diff = abs(values[i] - weightedMean);
            // Addition is how much this score adds to total confidence
            //   diff=1 , agreement=0  -> 0 totalConfAddition to total conf
            //   diff=.5, agreement=.5 -> .25 totalConfAddition to total conf
            //   diff=0 , agreement=1  -> conf totalConfAddition to total conf
            double agreement = 1 - diff;
            double totalConfAddition = logitConfidences[i] * pow(agreement, 2); // powering agreement gives less addition to total confidence
            totalConf += totalConfAddition;
            int finalI = i;
            log.trace(  "    score: {}", () -> new Score(values[finalI], confidences[finalI]));
            log.trace("      diff : {}", () -> diff);
            log.trace("      agrmnt:{}", () -> agreement);
            log.trace("      logtco:{}", () -> logitConfidences[finalI]);
            log.trace("      addtn: {}", () -> totalConfAddition);
        }
        totalConf = sigmoid(totalConf, sigmoidRangeLow, sigmoidRangeHigh);
        totalConf = limit(totalConf, 0, 1);
        return totalConf;
    }

    /**
     * A variant of combineM11 with the following feature: Regardless of the weights, if all scores are 1/1, the end result
     * will be 1/1.
     *
     * <p>Also, no disagreement effect on confidence.</p>
     *
     * <p>One of the characteristics of this variant is that individual scores have a relatively strong effect on the
     * end score, because it takes some force to arrive at 1/1.</p>
     *
     * <p>If max weight &gt; 0, all weights will be scaled so that max weight == 0.
     * If you manually scale your weights such that max weight &lt; 0, the effect is that the lowest weighted scores will
     * have relatively more effect on the end result, and the highest weighted scores relatively less.</p>
     **/
    public static Score combineM11TightAndNoDisagreementEffect(Score[] scores, @Nullable double[] weights)
    {
        if (isEmpty(scores) || allElementsSatisfy(scores, Score::isNa))
            return NA_SCORE;

        // Taking relatively wide bounds here lessens the effect of individual scores on the end score confidence, or, in other
        // words, accumulation of confidences resembles lineair addition a bit more
        final double sigmoidRangeLow = -1.2;
        final double sigmoidRangeHigh = 1.2;

        log.trace("combineM11(scores = {}, weights = {})", () -> Arrays.toString(scores), () -> Arrays.toString(weights));

        double[] values = new double[scores.length];
        double[] adjustedConfidences = new double[scores.length];
        double[] logitConfidences = new double[scores.length];
        double[] adjustedLogitConfidences = new double[scores.length];
        double[] maxLogitConfidences = new double[scores.length];

        if (weights == null) {
            for (int i = 0; i < scores.length; i++) {
                values[i] = scores[i].value;
                adjustedConfidences[i] = scores[i].confidence;
                logitConfidences[i] = logit(adjustedConfidences[i], sigmoidRangeLow, sigmoidRangeHigh);
                maxLogitConfidences[i] = logit(1, sigmoidRangeLow, sigmoidRangeHigh);
            }
        } else {
            double maxWeight = max(weights);
            double weightAdjustment = maxWeight > 1 ? 1.0 / maxWeight : 1.0;
            for (int i = 0; i < scores.length; i++) {
                values[i] = scores[i].value;
                adjustedConfidences[i] = scores[i].confidence * weights[i] * weightAdjustment;
                logitConfidences[i] = logit(adjustedConfidences[i], sigmoidRangeLow, sigmoidRangeHigh);
                maxLogitConfidences[i] = logit(weights[i] * weightAdjustment, sigmoidRangeLow, sigmoidRangeHigh);
            }
        }

        // The max total logit we can get is now sum(maxLogits). We want that to be logit(1), so that if all scores are
        // 1/1, the end result is 1/1.
        double sumMaxLogits = sum(maxLogitConfidences);
        double logitAdjustment = logit(1, sigmoidRangeLow, sigmoidRangeHigh) / sumMaxLogits;
        for (int i = 0; i < logitConfidences.length; i++)
            adjustedLogitConfidences[i] = logitAdjustment * logitConfidences[i];

        if (sum(adjustedConfidences) == 0)
            return new Score(mean(values), 0);

        double weightedMean = weightedMeanIgnoreNAs(values, adjustedConfidences);
        log.trace("    wgtdMn:{}", weightedMean);

        double totalLogitConf = 0;
        for (int i = 0; i < scores.length; i++) {
            if ( ! isValue(values[i]) || ! isValue(adjustedConfidences[i]) || ! isValue(adjustedLogitConfidences[i]) )
                continue;
            totalLogitConf += adjustedLogitConfidences[i];
            log.trace("    score: {}", scores[i]);
            log.trace("      adjcon:{}", adjustedConfidences[i]);
            log.trace("      logtco:{}", logitConfidences[i]);
        }
        double totalConf = sigmoid(totalLogitConf, sigmoidRangeLow, sigmoidRangeHigh);
        totalConf = limit(totalConf, 0, 1);
        Score result = new Score(weightedMean, totalConf);
        log.trace("    lgttc: {}", totalLogitConf);
        log.trace("    totco: {}", totalConf);
        log.trace("    rs-11: {}", result);
        return result;
    }

    public static void printPerformanceStats()
    {
        System.out.println("Total time in s:" + totalTime * NANOS_TO_SECONDS);
    }

    /**
     * Converts a score with (-1,1) value into a score with (0,1) value, where
     * <ul>
     * <li>-1 -> 0</li>
     * <li>0 -> NEUTRAL_SCORE</li>
     * <li>1 -> 1</li>
     * </ul>
     * and the other values are interpolated.
     */
    public static Score minusOneOneScoreToZeroOneScore(Score score)
    {
        if (score.value < 0)
            return new Score((score.value + 1) * NEUTRAL_SCORE, score.confidence);
        else
            return new Score(NEUTRAL_SCORE + score.value * (1 - NEUTRAL_SCORE), score.confidence);
    }

}
