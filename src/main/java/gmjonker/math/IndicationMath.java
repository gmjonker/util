package gmjonker.math;

import gmjonker.util.LambdaLogger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;

import static gmjonker.math.GeneralMath.*;
import static gmjonker.math.Indication.NA_INDICATION;
import static gmjonker.math.NaType.NA;
import static gmjonker.math.NaType.isValue;
import static gmjonker.math.SigmoidMath.*;
import static gmjonker.util.CollectionsUtil.allElementsSatisfy;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * Approximate statistical inference on point estimate/confidence measure pairs.
 *
 * <p>The math in this class is not an established model of statistical inference, but rather a common sense approximation
 * of it. It provides a rudimentary way to combine point estimates that are accompanied by a simple confidence measure.
 * Knowledge about underlying probability distributions is not needed. As such, it is not statistically correct,
 * but can provide a significant improvement over not using any measure of confidence at all.
 *
 * <p>All point estimates are assumed to be in range (-1,1), all confidences in range (0,1).
 */
@SuppressWarnings("WeakerAccess")
public class IndicationMath
{
    protected static final LambdaLogger log = new LambdaLogger(IndicationMath.class);

    /**
     * Infers a new indication based on given indications.
     *
     * <p>Indication values in range (-1,1)
     **/
    public static Indication combine(Indication... indications)
    {
        return combine(indications, null, false);
    }

    /**
     * Infers a new indication based on given indications.
     *
     * <p>Indication values in range (-1,1)
     **/
    public static Indication combine(Collection<Indication> indications)
    {
        Indication[] indicationArray = new Indication[indications.size()];
        return combine(indications.toArray(indicationArray), null, false);
    }

    /**
     * Infers a new indication based on given indications.
     *
     * <p>Indication values in range (-1,1)
     **/
    public static Indication combineWithComments(Collection<Indication> indications)
    {
        Indication[] indicationArray = new Indication[indications.size()];
        return combine(indications.toArray(indicationArray), null, true);
    }

    /**
     * Infers a new indication based on given indications.
     *
     * <p>Indication values in range (-1,1)
     **/
    public static Indication combineNoDisagreementEffect(Collection<Indication> indications)
    {
        Indication[] indicationArray = new Indication[indications.size()];
        return combineNoDisagreementEffect(indications.toArray(indicationArray), null);
    }

    /**
     * Infers a new indication based on given indications.
     *
     * <p>Indication values in range (-1,1)
     **/
    public static Indication combineTightAndNoDisagreementEffect(Collection<Indication> indications)
    {
        Indication[] indicationArray = new Indication[indications.size()];
        return combineTightAndNoDisagreementEffect(indications.toArray(indicationArray), null);
    }

    /**
     * Infers a new indication based on given indications, where indications may be weighted to indicate that some indications should have
     * more weight in the outcome than others.
     *
     * <p>Disagreement among the indications has a negative effect on confidence.</p>
     *
     * <p>Indication values in range (-1,1). Weights have no constraints (will be normalized on the fly).
     **/
    public static Indication combine(Indication[] indications, @Nullable double[] weights)
    {
        return combine(indications, weights, false);
    }

    /**
     * Infers a new indication based on given indications, where indications may be weighted to indicate that some indications should have
     * more weight in the outcome than others.
     *
     * <p>Disagreement among the indications has a negative effect on confidence.</p>
     *
     * <p>Indication values in range (-1,1). Weights have no constraints (will be normalized on the fly).
     **/
    public static Indication combine(Indication[] indications, @Nullable double[] weights, boolean combineComments)
    {
        if (isEmpty(indications) || allElementsSatisfy(indications, Indication::isNa))
            return NA_INDICATION;

        final double sigmoidRangeLow = -1.1;
        final double sigmoidRangeHigh = 1.1;

        log.trace("combine({}, {})", () -> Arrays.toString(indications), () -> Arrays.toString(weights));
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
            logitConfidences[i] = logit(confidences[i], sigmoidRangeLow, sigmoidRangeHigh);
        }

        String comment = "";
        if (combineComments) {
            StringJoiner stringJoiner = new StringJoiner(",");
            for (Indication indication : indications) {
                stringJoiner.add(indication.comment);
            }
            comment = stringJoiner.toString();
        }

        if (sum(confidences) == 0) {
            return new Indication(mean(values), 0, comment);
        }

        double weightedMean = weightedMeanIgnoreNAs(values, confidences);
        log.trace("    wgtdMn:{}", weightedMean);

        double totalConf = 0;
        for (int i = 0; i < indications.length; i++) {
            if ( ! isValue(values[i]) || ! isValue(confidences[i]) )
                continue;
            // Diff is how much this indication is disagreeing with the average indication
            double diff = abs(values[i] - weightedMean);
            // Addition is how much this indication adds to total confidence
            //   diff=1 , agreement=0  -> 0 totalConfAddition to total conf
            //   diff=.5, agreement=.5 -> .25 totalConfAddition to total conf
            //   diff=0 , agreement=1  -> conf totalConfAddition to total conf
            // TODO: check if agreement shouldn't be trimmed to be at least 0
            double agreement = 1 - diff;
            double totalConfAddition = logitConfidences[i] * pow(agreement, 2); // powering agreement gives less addition to total confidence
            totalConf += totalConfAddition;
            log.trace("    indication: {}", indications[i]);
            log.trace("      diff : {}", diff);
            log.trace("      agrmnt:{}", agreement);
            log.trace("      logtco:{}", logitConfidences[i]);
            log.trace("      addtn: {}", totalConfAddition);
        }
        log.trace("    lgttc: {}", totalConf);
        totalConf = sigmoid(totalConf, sigmoidRangeLow, sigmoidRangeHigh);
        totalConf = limit(totalConf, 0, 1);

        Indication result = new Indication(weightedMean, totalConf, comment);
        log.trace("    totco: {}", totalConf);
        log.trace("    rs-11: {}", result);

        return result;
    }

    /**
     * Infers a new indication based on given indications, where indications may be weighted to indicate that some indications should have
     * more weight in the outcome than others.
     *
     * <p>Disagreement among the indications has a no effect on confidence. The more indications are combines, the higher the
     * resulting confidence.</p>
     *
     * <p>Indication values in range (-1,1). Weights have no constraints (will be normalized on the fly).
     **/
    public static Indication combineNoDisagreementEffect(Indication... indications)
    {
        return combineNoDisagreementEffect(indications, null);
    }

    /**
     * Infers a new indication based on given indications, where indications may be weighted to indicate that some indications should have
     * more weight in the outcome than others.
     *
     * <p>Disagreement among the indications has a no effect on confidence. The more indications are combines, the higher the
     * resulting confidence.</p>
     *
     * <p>Indication values in range (-1,1). Weights have no constraints (will be normalized on the fly).
     **/
    public static Indication combineNoDisagreementEffect(Indication[] indications, @Nullable double[] weights)
    {
        if (isEmpty(indications) || allElementsSatisfy(indications, Indication::isNa))
            return NA_INDICATION;

        // Taking relatively wide bounds here lessens the effect of individual indications on the end indication confidence, or, in other
        // words, accumulation of confidences resembles lineair addition a bit more
        final double sigmoidRangeLow = -1.2;
        final double sigmoidRangeHigh = 1.2;

        log.trace("combineNoDisagreementEffect({}, {})", () -> Arrays.toString(indications), () -> Arrays.toString(weights));
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
            logitConfidences[i] = logit(confidences[i], sigmoidRangeLow, sigmoidRangeHigh);
        }

        if (sum(confidences) == 0)
            return new Indication(mean(values), 0);

        double weightedMean = weightedMeanIgnoreNAs(values, confidences);
        log.trace("    wgtdMn:{}", weightedMean);

        double totalConf = 0;
        for (int i = 0; i < indications.length; i++) {
            if ( ! isValue(values[i]) || ! isValue(confidences[i]) )
                continue;
            totalConf += logitConfidences[i];
            log.trace("    indication: {}", indications[i]);
            log.trace("      logtco:{}", logitConfidences[i]);
        }
        log.trace("    lgttc: {}", totalConf);
        totalConf = sigmoid(totalConf, sigmoidRangeLow, sigmoidRangeHigh);
        totalConf = limit(totalConf, 0, 1);
        Indication result = new Indication(weightedMean, totalConf);
        log.trace("    totco: {}", totalConf);
        log.trace("    rs-11: {}", result);
        return result;
    }

    /**
     * A variant of combineM11 with the following feature: Regardless of the weights, if all indications are 1/1, the end result
     * will be 1/1.
     *
     * <p>Also, no disagreement effect on confidence.</p>
     *
     * <p>One of the characteristics of this variant is that individual indications have a relatively strong effect on the
     * end indication, because it takes some force to arrive at 1/1.</p>
     *
     * <p>If max weight &gt; 0, all weights will be scaled so that max weight == 0.
     * If you manually scale your weights such that max weight &lt; 0, the effect is that the lowest weighted indications will
     * have relatively more effect on the end result, and the highest weighted indications relatively less.</p>
     **/
    public static Indication combineTightAndNoDisagreementEffect(Indication[] indications, @Nullable double[] weights)
    {
        if (isEmpty(indications) || allElementsSatisfy(indications, Indication::isNa))
            return NA_INDICATION;

        // Taking relatively wide bounds here lessens the effect of individual indications on the end indication confidence, or, in other
        // words, accumulation of confidences resembles lineair addition a bit more
        final double sigmoidRangeLow = -1.2;
        final double sigmoidRangeHigh = 1.2;

        log.trace("combineTightAndNoDisagreementEffect({}, {})", () -> Arrays.toString(indications), () -> Arrays.toString(weights));
        double[] values = new double[indications.length];
        double[] adjustedConfidences = new double[indications.length];
        double[] logitConfidences = new double[indications.length];
        double[] adjustedLogitConfidences = new double[indications.length];
        double[] maxLogitConfidences = new double[indications.length];

        if (weights == null) {
            for (int i = 0; i < indications.length; i++) {
                values[i] = indications[i].value;
                adjustedConfidences[i] = indications[i].confidence;
                logitConfidences[i] = logit(adjustedConfidences[i], sigmoidRangeLow, sigmoidRangeHigh);
                maxLogitConfidences[i] = logit(1, sigmoidRangeLow, sigmoidRangeHigh);
                log.trace("i = {}", i);
                log.trace("adjustedConfidences[i] = {}", adjustedConfidences[i]);
                log.trace("logitConfidences[i] = {}", logitConfidences[i]);
                log.trace("maxLogitConfidences[i] = {}", maxLogitConfidences[i]);
            }
        } else {
            double maxWeight = max(weights);
            double weightAdjustment = maxWeight > 1 ? 1.0 / maxWeight : 1.0;
            log.trace("maxWeight = {}", maxWeight);
            for (int i = 0; i < indications.length; i++) {
                values[i] = indications[i].value;
                adjustedConfidences[i] = indications[i].confidence * weights[i] * weightAdjustment;
                logitConfidences[i] = logit(adjustedConfidences[i], sigmoidRangeLow, sigmoidRangeHigh);
                maxLogitConfidences[i] = logit(weights[i] * weightAdjustment, sigmoidRangeLow, sigmoidRangeHigh);
                log.trace("i = {}", i);
                log.trace("adjustedConfidences[i] = {}", adjustedConfidences[i]);
                log.trace("logitConfidences[i] = {}", logitConfidences[i]);
                log.trace("maxLogitConfidences[i] = {}", maxLogitConfidences[i]);
            }
        }

        // The max total logit we can get is now sum(maxLogits). We want that to be logit(1), so that if all indications are
        // 1/1, the end result is 1/1.
        double sumMaxLogits = sum(maxLogitConfidences);
        double logitAdjustment = logit(1, sigmoidRangeLow, sigmoidRangeHigh) / sumMaxLogits;
        log.trace("sumMaxLogits = {}", sumMaxLogits);
        for (int i = 0; i < logitConfidences.length; i++) {
            adjustedLogitConfidences[i] = logitAdjustment * logitConfidences[i];
            log.trace("adjustedLogitConfidences[i] = {}", adjustedLogitConfidences[i]);
        }

        if (sum(adjustedConfidences) == 0)
            return new Indication(mean(values), 0);

        double weightedMean = weightedMeanIgnoreNAs(values, adjustedConfidences);
        log.trace("    wgtdMn:{}", weightedMean);

        double totalLogitConf = 0;
        for (int i = 0; i < indications.length; i++) {
            if ( ! isValue(values[i]) || ! isValue(adjustedConfidences[i]) || ! isValue(adjustedLogitConfidences[i]) )
                continue;
            totalLogitConf += adjustedLogitConfidences[i];
            log.trace("    indication: {}", indications[i]);
            log.trace("      adjcon:{}", adjustedConfidences[i]);
            log.trace("      logtco:{}", logitConfidences[i]);
        }
        double totalConf = sigmoid(totalLogitConf, sigmoidRangeLow, sigmoidRangeHigh);
        totalConf = limit(totalConf, 0, 1);
        Indication result = new Indication(weightedMean, totalConf);
        log.trace("    lgttc: {}", totalLogitConf);
        log.trace("    totco: {}", totalConf);
        log.trace("    rs-11: {}", result);
        return result;
    }

    /**
     * Converts a indication with (-1,1) value into a indication with (0,1) value, where
     * <ul>
     * <li>-1 -> 0</li>
     * <li>0 -> neutralValue</li>
     * <li>1 -> 1</li>
     * </ul>
     * and the other values are linearly interpolated.
     */
    public static Indication minusOneOneRangeToZeroOneRange(Indication indication, double neutralValue)
    {
        if (indication.value < 0)
            return new Indication((indication.value + 1) * neutralValue, indication.confidence);
        else
            return new Indication(neutralValue + indication.value * (1 - neutralValue), indication.confidence);
    }





    /**
     * See {@link #combineStrict(Indication[], double[], boolean)}
     **/
    public static Indication combineStrict(Indication[] indications, boolean combineComments)
    {
        return combineStrict(indications, null, combineComments);
    }

    /**
     * See {@link #combineStrict(Indication[], double[], boolean)}
     **/
    public static Indication combineStrict(Collection<Indication> indications, boolean combineComments)
    {
        Indication[] indicationArray = new Indication[indications.size()];
        return combineStrict(indications.toArray(indicationArray), null, combineComments);
    }

    /**
     * Infers a new indication based on given indications, where indications may be weighted to indicate that some indications should have
     * more weight in the outcome than others.
     *
     * <p>Strict: if any of the indications has confidence 1.0, that indication will be the end result.
     * If there are more than one indications with confidence 1.0, some averaging will happen for the value, the confidence
     * will almost always be 1.0.
     * </p>
     *
     * <p>Indication values in range (-1,1). Weights have no constraints (will be normalized on the fly).
     **/
    public static Indication combineStrict(Indication[] indications, @Nullable double[] weights, boolean combineComments)
    {
        if (isEmpty(indications) || allElementsSatisfy(indications, Indication::isNa))
            return NA_INDICATION;

        final double sigmoidRangeLow = -1;
        final double sigmoidRangeHigh = 1;

        log.trace("combine({}, {})", () -> Arrays.toString(indications), () -> Arrays.toString(weights));
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
            logitConfidences[i] = fastLogitAlternative(confidences[i], sigmoidRangeLow, sigmoidRangeHigh);
        }
        log.trace("    logitConfidences = {}", logitConfidences);

        String comment = "";
        if (combineComments) {
            StringJoiner stringJoiner = new StringJoiner(",");
            for (Indication indication : indications) {
                stringJoiner.add(indication.comment);
            }
            comment = stringJoiner.toString();
        }

        if (sum(confidences) == 0) {
            return new Indication(mean(values), 0, comment);
        }

        double weightedMean = weightedMeanIgnoreNAs(values, logitConfidences);
        log.trace("    wgtdMn:{}", weightedMean);

        double totalLogitConf = 0;
        for (int i = 0; i < indications.length; i++) {
            if ( ! isValue(values[i]) || ! isValue(confidences[i]) )
                continue;
            // Diff is how much this indication is disagreeing with the average indication
            double diff = abs(values[i] - weightedMean);
            // Addition is how much this indication adds to total confidence
            //   diff=1 , agreement=0  -> 0 totalConfAddition to total conf
            //   diff=.5, agreement=.5 -> .25 totalConfAddition to total conf
            //   diff=0 , agreement=1  -> conf totalConfAddition to total conf
            double agreement = max(0, 1 - diff);
            double totalConfAddition =
                    agreement == 0
                            ? 0 // needed to deal with the case that logitConfidences[i] is infinite
                            : logitConfidences[i] * pow(agreement, 2); // powering agreement gives less addition to total confidence
            totalLogitConf += totalConfAddition;
            log.trace("    indication: {}", indications[i]);
            log.trace("      diff : {}", diff);
            log.trace("      agrmnt:{}", agreement);
            log.trace("      logtco:{}", logitConfidences[i]);
            log.trace("      addtn: {}", totalConfAddition);
        }
        double totalConf = fastSigmoidAlternative(totalLogitConf, sigmoidRangeLow, sigmoidRangeHigh);
        totalConf = limit(totalConf, 0, 1);

        Indication result = new Indication(weightedMean, totalConf, comment);
        log.trace("    lgttc: {}", totalLogitConf);
        log.trace("    totco: {}", totalConf);
        log.trace("    rs-11: {}", result);

        return result;
    }
}
