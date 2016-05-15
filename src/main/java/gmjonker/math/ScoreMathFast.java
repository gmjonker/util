package gmjonker.math;

import gmjonker.util.LambdaLogger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static gmjonker.math.GeneralMath.*;
import static gmjonker.math.NaType.NA;

/**
 * Alternative to ScoreMath using faster math. The results are a bit different, so take care when switching from
 * ScoreMath to this.
 *
 * <p>If, at some point, we fully switch to ScoreMathFast, it can replace ScoreMath.
 */
@Deprecated
public class ScoreMathFast
{
    private static final double SIGMOID_RANGE_LOW = -1.1;
    private static final double SIGMOID_RANGE_HIGH = 1.1;

    // Some performance stats
    private static final DescriptiveStatistics sizeStats = new DescriptiveStatistics();
    private static long totalTime = 0;

    protected static final LambdaLogger log = new LambdaLogger(ScoreMath.class);

    /**
     * Infers a new score based on given scores.
     *
     * <p>Score values in range [0..1]
     **/
    public static Score combine01(Score... scores)
    {
        return combine01(scores, null);
    }

    /**
     * Infers a new score based on given scores.
     *
     * <p>Score values in range [0..1]
     **/
    public static Score combine01(List<Score> scores)
    {
        sizeStats.addValue(scores.size());
        long now = System.nanoTime();
        Score[] scoreArray = new Score[scores.size()];
        Score score = combine01(scores.toArray(scoreArray), null);
        totalTime += System.nanoTime() - now;
        return score;
    }

    /**
     * Infers a new score based on given scores, where scores may be weighted to indicate that some scores should have
     * more weight in the outcome than others.
     *
     * <p>Score values in range [0..1]. Weights have no constraints (will be normalized on the fly).
     **/
    public static Score combine01(Score[] scores, double[] weights)
    {
        // Convert to [-1..1] range
        Score[] newScores = new Score[scores.length];
        for (int i = 0; i < scores.length; i++) {
            newScores[i] = new Score(ScoreMath.zeroOneRangeToMinusOneOneRange(scores[i].value), scores[i].confidence);
        }
        // Combine
        Score combinedScore = combine(newScores, weights);
        // Convert back
        return new Score(ScoreMath.minusOneOneRangeToZeroOneRange(combinedScore.value), combinedScore.confidence);
    }

    /**
     * Infers a new score based on given scores.
     *
     * <p>Score values in range [-1..1]
     **/
    static Score combine(Score... scores)
    {
        return combine(scores, null);
    }

    /**
     * Infers a new score based on given scores, where scores may be weighted to indicate that some scores should have
     * more weight in the outcome than others.
     *
     * <p>Score values in range [-1..1]. Weights have no constraints (will be normalized on the fly).
     **/
    static Score combine(Score[] scores, @Nullable double[] weights)
    {
        log.trace("scores: {}", () -> Arrays.toString(scores));
        log.trace("weights:{}", () -> Arrays.toString(weights));
        double[] values = new double[scores.length];
        double[] confidences = new double[scores.length];
        double[] adjustedWeights = new double[scores.length];
        // We take the inverse sigmoid of confidences, then accumulate them, then take the sigmoid. This results in the
        // effect of two confidences of .9 adding up to something like .98 for instance (if the values are equal or similar).
        double[] logitConfidences = new double[scores.length];
        double maxWeight = weights == null ? NA : max(weights);
        for (int i = 0; i < scores.length; i++) {
            values[i] = scores[i].value;
            // If max weight > 1, adjust all weights such that max weight == 1, otherwise just leave the weights as is
            adjustedWeights[i] = weights == null ? 1
                                                 : maxWeight > 1 ? weights[i] * 1 / maxWeight : weights[i];
            confidences[i] = scores[i].confidence * adjustedWeights[i];
            logitConfidences[i] = fastLogitAlternative(confidences[i], SIGMOID_RANGE_LOW, SIGMOID_RANGE_HIGH);
        }

        if (sum(confidences) == 0)
            return new Score(mean(values), 0);

        double weightedMean = weightedMean(values, confidences);
        log.trace("wgtdMn:{}", weightedMean);

        double totalConf = 0;
        for (int i = 0; i < scores.length; i++) {
            // Diff is how much this score is disagreeing with the average score
            double diff = abs(values[i] - weightedMean);
            // Addition is how much this score adds to total confidence
            //   diff=1 , agreement=0  -> 0 totalConfAddition to total conf
            //   diff=.5, agreement=.5 -> .25 totalConfAddition to total conf
            //   diff=0 , agreement=1  -> conf totalConfAddition to total conf
            double agreement = 1 - diff;
            double totalConfAddition = logitConfidences[i] * pow(agreement, 4); // powering agreement gives less addition to total confidence
            totalConf += totalConfAddition;
            log.trace("score: {}", scores[i]);
            log.trace("diff : {}", diff);
            log.trace("agrmnt:{}", agreement);
            log.trace("logtco:{}", logitConfidences[i]);
            log.trace("addtn: {}", totalConfAddition);
        }
        log.trace("lgttc: {}", totalConf);
        totalConf = fastSigmoidAlternative(totalConf, SIGMOID_RANGE_LOW, SIGMOID_RANGE_HIGH);
        log.trace("totco: {}", totalConf);
        totalConf = limit(totalConf, 0, 1);
        return new Score(weightedMean, totalConf);
    }

    public static void printPerformanceStats()
    {
        System.out.println("sizeStats = " + sizeStats);
        System.out.println("Total time in s:" + totalTime * .001 * .001 * .001);
    }
}
