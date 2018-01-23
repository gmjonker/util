package gmjonker.math;

import lombok.val;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gmjonker.TestUtil.ind;
import static gmjonker.math.GeneralMath.abs;
import static gmjonker.math.GeneralMath.round;
import static gmjonker.math.Indication.toPrimitiveIndicationArray;
import static gmjonker.math.IndicationMath.combine;
import static gmjonker.math.IndicationMath.combineNoDisagreementEffect;
import static gmjonker.math.IndicationMath.combineStrict;
import static gmjonker.math.IndicationMath.combineTightAndNoDisagreementEffect;
import static gmjonker.math.NaType.NA;
import static gmjonker.util.CollectionsUtil.toPrimitiveDoubleArray;
import static gmjonker.util.FormattingUtil.asPercentage;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class IndicationMathTest
{
   
    @Test
    public void indicationsShouldBeCombinedCorrectly() throws Exception
    {
        // This test is mostly visual. The viewer must judge whether the combined indications are good enough compared to the
        // desired outcomes.

        double[][] data = new double[][] {
                // Each row is a sequence of indications, of value/confidence pairs, so v1, c1, v2, c2, etc. The last
                // value/confidence indication should be the result of combining the indications before it.
                { 0  , 0  },
                { 1  , 1  ,  1  , 1  },
                { 1  , 0  ,  1  , 0  },
                { 1  , .05,  1  , .05},
                { .3 , .4 ,  .3 , .4 },
                { 1  , 1  ,  -1 , 0.33, .33, .33 },
                { 1  , 1  ,  0  , 1  , 0.5 , 0.25 },
                { 1  , 1  ,  0  , 0.1, 0.9 , 0.9  },
                { 1  , 1  ,  0  , 0.5, 0.7 , 0.5  },
                { 1  , 1  ,  1  , 1  , 1   , 1    },
                { 1  , 1  , -1  , 1  , 0   , 0    },
                { 1  , 1  , -1  , 0.5, 0.25, 0.1  },
                { 1  , 0.5, -1  , 0.5, 0   , 0    },
                { 0.5, 0.5,  0.5, 0.5, 0.5 , 0.7  },
                { 0.5, 1  , -0.5, 1  , 0   , 0.25 },
                { 1  , 1  , -1  , 0  , 1   , 1    },
                { 0.5, 0.2,  0.5, 0.2, 0.5 , 0.3  },
                // pop         cbr         desired indication
                {  1.0, 1.0,   1.0, 1.0,   1.0, 1.0,   },
                {  1.0, 1.0,  -1.0, 1.0,   0.0, 0.0,   },
                {  1.0, 1.0,   0.0, 1.0,   0.5, 0.5,   },
                {  1.0, 0.5,   1.0, 0.5,   1.0, 0.9,   },
                {  1.0, 0.4,   1.0, 0.6,   1.0, 0.9,   },
                {  1.0, 0.4,   0.0, 0.6,   0.4, 0.1,   },
                // pop         cbr         ubr         desired indication
                {  1.0, 1.0,   1.0, 1.0,   1.0, 1.0,   1.0, 1.0,    },
                {  1.0, 0.9,   1.0, 0.8,   1.0, 0.7,   1.0, 1.0,    },
                {  0.0, 0.8,   0.5, 0.8,   1.0, 0.8,   0.5, 0.4,    },  // has worst error vs. desired outcome
                {  1.0, 1.0,   1.0, 1.0,  -1.0, 1.0,   0.3, 0.1,    },
                {  1.0, 1.0,  -1.0, 1.0,   1.0, 1.0,   0.3, 0.1,    },
                {  1.0, 1.0,   0.0, 1.0,   1.0, 1.0,   0.7, 0.5,    },
                {  1.0, 0.8,   1.0, 0.6,   1.0, 0.4,   1.0, 1.0,    },
                {  1.0, 0.1,   1.0, 0.1,   1.0, 0.1,   1.0, 0.3,    },
                {  1.0, 0.1,   0.0, 0.1,  -1.0, 0.1,   0.0, 0.0,    },
        };
        for (double[] row : data)
        {
            // Given:
            Indication[] indications = new Indication[row.length / 2 - 1];
            for (int i = 0; i < row.length / 2 - 1; i++)
                indications[i] = new Indication(row[i * 2], row[i * 2 + 1]);
            Indication desired = new Indication(row[row.length - 2], row[row.length - 1]);

            // When:
            Indication result = combine(indications);

            // Then:
            for (Indication indication : indications)
                System.out.printf("%s ", indication.toShortString());
            System.out.printf("= %s (des: %s, dif:%s/%s)%n", result.toShortString(), desired.toShortString(),
                    asPercentage(abs(result.value - desired.value)), asPercentage(abs(result.confidence - desired.confidence)));

            // Also check that combine is consistent with combine-weighted.
            double[] weights = new double[indications.length];
            Arrays.fill(weights, 1.0);
            Indication result2 = combine(indications, weights);
            assertThat(result2, equalTo(result));
        }
    }

    @Test
    public void combineWeighted()
    {
        // This test is mostly visual. The viewer must judge whether the combined indications are good enough compared to the
        // desired outcomes.

        double[] weights = {1, 2, 3};
        double[][] data = new double[][] {
                // Each row is a sequence of indications, of value/confidence pairs, so v1, c1, v2, c2, etc. The last
                // value/confidence indication should be the result of combining the indications before it.
                // pop         cbr         ubr         desired indication
                {  1.0, 1.0,    NA,  NA,    NA,  NA,   1.0 ,  .15,  },
                {  1.0, 1.0,   1.0, 1.0,   1.0, 1.0,   1.0 , 1.0,   },
                {  1.0, 0.9,   1.0, 0.9,   1.0, 0.9,   1.0 , 1.0,   },
                {  1.0, 0.8,   1.0, 0.8,   1.0, 0.8,   1.0 , 0.95,  },
                {  1.0, 0.6,   1.0, 0.6,   1.0, 0.6,   1.0 , 0.8,   },
                {  1.0, 0.4,   1.0, 0.4,   1.0, 0.4,   1.0 , 0.7,   },
                {  1.0, 0.2,   1.0, 0.2,   1.0, 0.2,   1.0 , 0.5,   },
                {  1.0, 0.1,   1.0, 0.1,   1.0, 0.1,   1.0 , 0.3,   },
                {  1.0, 0.0,   1.0, 0.0,   1.0, 0.0,   1.0 , 0.0,   },
                {  1.0, 1.0,   1.0, 1.0,  -1.0, 1.0,   0.0 , 0.0,   },
                {  1.0, 1.0,  -1.0, 1.0,   1.0, 1.0,   0.3 , 0.3,   },
                {  1.0, 1.0,   0.0, 1.0,   1.0, 1.0,   0.7 , 0.5,   },
                {  1.0, 0.8,   1.0, 0.6,   1.0, 0.4,   1.0 , 1.0,   },
                {  1.0, 0.1,   1.0, 0.1,   1.0, 0.1,   1.0 , 0.3,   },
                {  1.0, 0.1,   0.0, 0.1,  -1.0, 0.1,   -.3 , 0.0,   },
                {  1.0, 0.4,   1.0, 0.4,   1.0, 0.4,   1.0 , 0.7,   },
                {  0.8, 0.8,   0.6, 0.6,   0.4, 0.4,   0.5 , 0.5,   },
                {  1.0, 1.0,   1.0, 1.0,   0.5, 1.0,   0.75, 0.5,   },
                {  0.1, 0.2,   0.3, 0.4,   0.5, 0.6,   0.4 , 0.4,   },
        };
        for (double[] row : data)
        {
            // Given:
            Indication popularityIndication      = new Indication(row[0], row[1]);
            Indication contentBasedIndication    = new Indication(row[2], row[3]);
            Indication userBasedIndication       = new Indication(row[4], row[5]);
            Indication desiredCombinedIndication = new Indication(row[6], row[7]);

            // When:
            Indication indication = combine(new Indication[]{popularityIndication, contentBasedIndication, userBasedIndication}, weights);

            // Then:
            System.out.printf("%s/1 + %s/2 + %s/3 = %s (des: %s, dif:%d/%d)%n", popularityIndication.toShortString(),
                    contentBasedIndication.toShortString(), userBasedIndication.toShortString(), indication.toShortString(),
                    desiredCombinedIndication.toShortString(), round(abs(indication.value - desiredCombinedIndication.value) * 100),
                    round(abs(indication.confidence - desiredCombinedIndication.confidence) * 100));
        }
    }

    @Test
    public void combineWeightedTightAndNoDisagreementEffect()
    {
        // This test is mostly visual. The viewer must judge whether the combined indications are good enough compared to the
        // desired outcomes.

        double[][] data = new double[][] {
                // Each row is a sequence of indications, of value/confidence pairs, so v1, c1, v2, c2, etc. The last
                // value/confidence indication should be the result of combining the indications before it.
                {  0.3, 0.6, 1.0,  0.3, 0.6 },
                {  1.0, 1.0, 1.0,  1.0, 1.0, 1.0,  1.0, 1.0 },
                {  0.3, 0.6, 1.0,  0.3, 0.6, 1.0,  0.3, 0.6 },
                {  0.3, 0.6, 1.0,  0.3, 0.6, 0.0,  0.3, 0.3 },
                {  0.1, 0.1, 0.1,  0.5, 0.5, 0.5,  1.0, 1.0, 1.0,  0.9, 0.9 },
                {  1.0, 1.0, 1.0,  1.0, 1.0, 2.0,  1.0, 1.0, 2.0,  1.0, 1.0, 3.0,  1.0, 1.0 },
                {  1.0, 1.0, 1.0,  1.0, 0.0, 2.0,  1.0, 0.0, 2.0,  1.0, 0.0, 3.0,  1.0, 0.2 },
                {  1.0, 0.0, 1.0,  1.0, 0.0, 2.0,  1.0, 0.0, 2.0,  1.0, 1.0, 3.0,  1.0, 0.5 },
        };
        for (double[] row : data)
        {
            // Given:
            List<Indication> indications = new ArrayList<>();
            List<Double> weights = new ArrayList<>();
            Indication desiredIndication = null;
            int i = 0;
            while (i < row.length) {
                if (i + 3 < row.length) {
                    indications.add(new Indication(row[i], row[i + 1]));
                    weights.add(row[i + 2]);
                    i += 3;
                } else {
                    desiredIndication = new Indication(row[i], row[i + 1]);
                    i += 2;
                }
            }

            // When:
            Indication result = combineTightAndNoDisagreementEffect(toPrimitiveIndicationArray(indications), toPrimitiveDoubleArray(weights));

            // Then:
            for (int i1 = 0; i1 < indications.size(); i1++) {
                Indication indication = indications.get(i1);
                double weight = weights.get(i1);
                System.out.printf("%s/%s ", indication.toShortString(), weight);
            }
            System.out.printf("= %s (des: %s, dif:%s/%s)%n", result.toShortString(), desiredIndication.toShortString(),
                    asPercentage(abs(result.value - desiredIndication.value)),
                    asPercentage(abs(result.confidence - desiredIndication.confidence)));
        }
    }

    @Test
    public void threeCombineMethodsCompared()
    {
        System.out.println("Vanilla:     " + combine                            (asList(new Indication(-1, .5), new Indication(1, .5))));
        System.out.println("No dis:      " + combineNoDisagreementEffect        (asList(new Indication(-1, .5), new Indication(1, .5))));
        System.out.println("Tight nodis: " + combineTightAndNoDisagreementEffect(asList(new Indication(-1, .5), new Indication(1, .5))));

        System.out.println("Vanilla:     " + combine                            (asList(new Indication(.5, .5), new Indication(1, .2))));
        System.out.println("No dis:      " + combineNoDisagreementEffect        (asList(new Indication(.5, .5), new Indication(1, .2))));
        System.out.println("Tight nodis: " + combineTightAndNoDisagreementEffect(asList(new Indication(.5, .5), new Indication(1, .2))));

        System.out.println("Vanilla:     " + combine                            (asList(new Indication(0, .5), new Indication(1, .2))));
        System.out.println("No dis:      " + combineNoDisagreementEffect        (asList(new Indication(0, .5), new Indication(1, .2))));
        System.out.println("Tight nodis: " + combineTightAndNoDisagreementEffect(asList(new Indication(0, .5), new Indication(1, .2))));

        System.out.println("Vanilla:     " + combine                            (asList(new Indication(1, 1), new Indication(-1, .1))));
        System.out.println("No dis:      " + combineNoDisagreementEffect        (asList(new Indication(1, 1), new Indication(-1, .1))));
        System.out.println("Tight nodis: " + combineTightAndNoDisagreementEffect(asList(new Indication(1, 1), new Indication(-1, .1))));
    }

    @Test
    public void combineXYZz()
    {
        // This test is mostly visual. The viewer must judge whether the combined indications are good enough compared to the
        // desired outcomes.

        double[][] data = new double[][] {
                // Each row is a sequence of indications, of value/confidence pairs, so v1, c1, v2, c2, etc. The last
                // value/confidence indication should be the result of combining the indications before it.
//                {  0.3, 0.6, 1.0,  0.3, 0.6 },
//                {  1.0, 1.0, 1.0,  1.0, 1.0, 1.0,  1.0, 1.0 },
                {  1.0, 1.0, 1.0,  -1.0,  .9, 1.0,   1.0, 1.0 },
                {  1.0, 1.0, 1.0,   0.8, 1.0, 1.0,   0.9, 0.0 },
                {  1.0, 1.0, 1.0,   0.0, 1.0, 1.0,   0.5, 0.0 },
                {  1.0, 1.0, 1.0,  -0.9, 1.0, 1.0,   0.5, 0.0 },
                {  1.0, 1.0, 1.0,  -1.0, 1.0, 1.0,   0.0, 0.0 },
                {  1.0, 1.0, 1.0,   1.0, 1.0, 1.0,  -1.0, 0.9, 1.0,    1.0, 1.0 },
                {  1.0, 1.0, 1.0,   1.0, 1.0, 1.0,  -1.0, 1.0, 1.0,    1.0/3, 1.0 },
//                {  0.3, 0.6, 1.0,  0.3, 0.6, 1.0,  0.3, 0.6 },
//                {  0.3, 0.6, 1.0,  0.3, 0.6, 0.0,  0.3, 0.3 },
//                {  0.1, 0.1, 0.1,  0.5, 0.5, 0.5,  1.0, 1.0, 1.0,  0.9, 0.9 },
//                {  1.0, 1.0, 1.0,  1.0, 1.0, 2.0,  1.0, 1.0, 2.0,  1.0, 1.0, 3.0,  1.0, 1.0 },
//                {  1.0, 1.0, 1.0,  1.0, 0.0, 2.0,  1.0, 0.0, 2.0,  1.0, 0.0, 3.0,  1.0, 0.2 },
//                {  1.0, 0.0, 1.0,  1.0, 0.0, 2.0,  1.0, 0.0, 2.0,  1.0, 1.0, 3.0,  1.0, 0.5 },
        };
        for (double[] row : data)
        {
            // Given:
            List<Indication> indications = new ArrayList<>();
            List<Double> weights = new ArrayList<>();
            Indication desiredIndication = null;
            int i = 0;
            while (i < row.length) {
                if (i + 3 < row.length) {
                    indications.add(new Indication(row[i], row[i + 1]));
                    weights.add(row[i + 2]);
                    i += 3;
                } else {
                    desiredIndication = new Indication(row[i], row[i + 1]);
                    i += 2;
                }
            }

            // When:
            Indication result = combineStrict(toPrimitiveIndicationArray(indications), toPrimitiveDoubleArray(weights), false);

            // Then:
            for (int i1 = 0; i1 < indications.size(); i1++) {
                Indication indication = indications.get(i1);
                double weight = weights.get(i1);
                System.out.printf("%s/%s ", indication.toShortString(), weight);
            }
            System.out.printf("= %s (des: %s, dif:%s/%s)%n", result.toShortString(), desiredIndication.toShortString(),
                    asPercentage(abs(result.value - desiredIndication.value)),
                    asPercentage(abs(result.confidence - desiredIndication.confidence)));
        }
    }
    
    @Test
    public void combineTransitivity()
    {
        val ind1 = ind(  1, 1);
        val ind2 = ind( .8, .5);
        val ind3 = ind( .5, 1);
        val ind4 = ind( -1, .6);
        val ind5 = ind(-.7, 1);
        
        val comb1 = combine(ind1, ind2, ind3);
        val comb2 = combine(ind4, ind5);
        val final1 = combine(comb1, comb2);
        
        val final2 = combine(ind1, ind2, ind3, ind4, ind5);

        System.out.println("comb1 = " + comb1);
        System.out.println("comb2 = " + comb2);
        System.out.println("final1 = " + final1 + " -> " + final1.deriveDouble());
        System.out.println("final2 = " + final2 + " -> " + final2.deriveDouble());
        
    }
    
}
