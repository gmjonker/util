package gmjonker.math;

import gmjonker.util.LambdaLogger;

import javax.annotation.Nullable;
import java.util.Arrays;

import static gmjonker.math.GeneralMath.*;
import static gmjonker.math.NaType.NA;

/**
 * Alternative to IndicationMath using faster math. The results are a bit different, so take care when switching from
 * IndicationMath to this.
 *
 * <p>If, at some point, we fully switch to IndicationMathFast, it can replace IndicationMath.
 */
@SuppressWarnings("WeakerAccess")
public class IndicationMathFast
{
    private static final double SIGMOID_RANGE_LOW = -1.1;
    private static final double SIGMOID_RANGE_HIGH = 1.1;

    protected static final LambdaLogger log = new LambdaLogger(IndicationMath.class);

    /**
     * Infers a new indication based on given indications.
     *
     * <p>Indication values in range (-1,1)
     **/
    public static Indication combine(Indication... indications)
    {
        return combine(indications, null);
    }

    /**
     * Infers a new indication based on given indications, where indications may be weighted to indicate that some indications should have
     * more weight in the outcome than others.
     *
     * <p>Indication values in range (-1,1). Weights have no constraints (will be normalized on the fly).
     **/
    public static Indication combine(Indication[] indications, @Nullable double[] weights)
    {
        log.trace("indications: {}", () -> Arrays.toString(indications));
        log.trace("weights:{}", () -> Arrays.toString(weights));
        double[] values = new double[indications.length];
        double[] confidences = new double[indications.length];
        double[] adjustedWeights = new double[indications.length];
        // We take the inverse sigmoid of confidences, then accumulate them, then take the sigmoid. This results in the
        // effect of two confidences of .9 adding up to something like .98 for instance (if the values are equal or similar).
        double[] logitConfidences = new double[indications.length];
        double maxWeight = weights == null ? NA : max(weights);
        for (int i = 0; i < indications.length; i++) {
            values[i] = indications[i].value;
            // If max weight > 1, adjust all weights such that max weight == 1, otherwise just leave the weights as is
            adjustedWeights[i] = weights == null ? 1
                                                 : maxWeight > 1 ? weights[i] * 1 / maxWeight : weights[i];
            confidences[i] = indications[i].confidence * adjustedWeights[i];
            logitConfidences[i] = fastLogitAlternative(confidences[i], SIGMOID_RANGE_LOW, SIGMOID_RANGE_HIGH);
        }

        if (sum(confidences) == 0)
            return new Indication(mean(values), 0);

        double weightedMean = weightedMean(values, confidences);
        log.trace("wgtdMn:{}", weightedMean);

        double totalConf = 0;
        for (int i = 0; i < indications.length; i++) {
            // Diff is how much this indication is disagreeing with the average indication
            double diff = abs(values[i] - weightedMean);
            // Addition is how much this indication adds to total confidence
            //   diff=1 , agreement=0  -> 0 totalConfAddition to total conf
            //   diff=.5, agreement=.5 -> .25 totalConfAddition to total conf
            //   diff=0 , agreement=1  -> conf totalConfAddition to total conf
            double agreement = 1 - diff;
            double totalConfAddition = logitConfidences[i] * pow(agreement, 4); // powering agreement gives less addition to total confidence
            totalConf += totalConfAddition;
            log.trace("indication: {}", indications[i]);
            log.trace("diff : {}", diff);
            log.trace("agrmnt:{}", agreement);
            log.trace("logtco:{}", logitConfidences[i]);
            log.trace("addtn: {}", totalConfAddition);
        }
        log.trace("lgttc: {}", totalConf);
        totalConf = fastSigmoidAlternative(totalConf, SIGMOID_RANGE_LOW, SIGMOID_RANGE_HIGH);
        log.trace("totco: {}", totalConf);
        totalConf = limit(totalConf, 0, 1);
        return new Indication(weightedMean, totalConf);
    }

//    /**
//     * Infers a new indication based on given indications.
//     *
//     * <p>Indication values in range (0,1)
//     **/
//    public static Indication combine01(Indication... indications)
//    {
//        return combine01(indications, null);
//    }
//
//    /**
//     * Infers a new indication based on given indications.
//     *
//     * <p>Indication values in range (0,1)
//     **/
//    public static Indication combine01(List<Indication> indications)
//    {
//        sizeStats.addValue(indications.size());
//        long now = System.nanoTime();
//        Indication[] indicationArray = new Indication[indications.size()];
//        Indication indication = combine01(indications.toArray(indicationArray), null);
//        totalTime += System.nanoTime() - now;
//        return indication;
//    }
//
//    /**
//     * Infers a new indication based on given indications, where indications may be weighted to indicate that some indications should have
//     * more weight in the outcome than others.
//     *
//     * <p>Indication values in range (0,1). Weights have no constraints (will be normalized on the fly).
//     **/
//    public static Indication combine01(Indication[] indications, double[] weights)
//    {
//        // Convert to (-1,1) range
//        Indication[] newIndications = new Indication[indications.length];
//        for (int i = 0; i < indications.length; i++) {
//            newIndications[i] = new Indication(IndicationMath.from01toM11(indications[i].value), indications[i].confidence);
//        }
//        // Combine
//        Indication combinedIndication = combine(newIndications, weights);
//        // Convert back
//        return new Indication(IndicationMath.fromM11to01(combinedIndication.value), combinedIndication.confidence);
//    }
}
