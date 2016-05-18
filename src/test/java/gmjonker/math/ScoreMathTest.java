package gmjonker.math;

import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gmjonker.math.GeneralMath.abs;
import static gmjonker.math.GeneralMath.round;
import static gmjonker.math.NaType.NA;
import static gmjonker.math.Score.NEUTRAL_SCORE;
import static gmjonker.math.Score.toPrimitiveScoreArray;
import static gmjonker.util.CollectionsUtil.toPrimitiveDoubleArray;
import static gmjonker.util.FormattingUtil.asPercentage;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

@Deprecated
public class ScoreMathTest
{
    @Test
    public void combine01ConvertsRangeCorrectly()
    {
        assertThat(ScoreMath.combine01(new Score(0, 1), new Score(1, 1)), equalTo(new Score(NEUTRAL_SCORE, 0)));
    }

    @Test
    public void scoresShouldBeCombinedCorrectly() throws Exception
    {
        // This test is mostly visual. The viewer must judge whether the combined scores are good enough compared to the
        // desired outcomes.

        double[][] data = new double[][] {
                // Each row is a sequence of scores, of value/confidence pairs, so v1, c1, v2, c2, etc. The last
                // value/confidence score should be the result of combining the scores before it.
                { 0  , 0  },
                { 1  , 1  ,  1  , 1  },
                { 1  , 0  ,  1  , 0  },
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
                // pop         cbr         desired score
                {  1.0, 1.0,   1.0, 1.0,   1.0, 1.0,   },
                {  1.0, 1.0,  -1.0, 1.0,   0.0, 0.0,   },
                {  1.0, 1.0,   0.0, 1.0,   0.5, 0.5,   },
                {  1.0, 0.5,   1.0, 0.5,   1.0, 0.9,   },
                {  1.0, 0.4,   1.0, 0.6,   1.0, 0.9,   },
                {  1.0, 0.4,   0.0, 0.6,   0.4, 0.1,   },
                // pop         cbr         ubr         desired score
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
            Score[] scores = new Score[row.length / 2 - 1];
            for (int i = 0; i < row.length / 2 - 1; i++)
                scores[i] = new Score(row[i * 2], row[i * 2 + 1]);
            Score desired = new Score(row[row.length - 2], row[row.length - 1]);

            // When:
            Score result = ScoreMath.combineM11(scores);

            // Then:
            for (Score score : scores)
                System.out.printf("%s ", score.toShortString());
            System.out.printf("= %s (des: %s, dif:%s/%s)%n", result.toShortString(), desired.toShortString(),
                    asPercentage(abs(result.value - desired.value)), asPercentage(abs(result.confidence - desired.confidence)));

            // Also check that combine is consistent with combine-weighted.
            double[] weights = new double[scores.length];
            Arrays.fill(weights, 1.0);
            Score result2 = ScoreMath.combineM11(scores, weights);
            assertThat(result2, equalTo(result));
        }
    }

    @Test
    public void combineWeighted()
    {
        // This test is mostly visual. The viewer must judge whether the combined scores are good enough compared to the
        // desired outcomes.

        double[] weights = {1, 2, 3};
        double[][] data = new double[][] {
                // Each row is a sequence of scores, of value/confidence pairs, so v1, c1, v2, c2, etc. The last
                // value/confidence score should be the result of combining the scores before it.
                // pop         cbr         ubr         desired score
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
            Score popularityScore      = new Score(row[0], row[1]);
            Score contentBasedScore    = new Score(row[2], row[3]);
            Score userBasedScore       = new Score(row[4], row[5]);
            Score desiredCombinedScore = new Score(row[6], row[7]);

            // When:
            Score score = ScoreMath.combineM11(new Score[]{popularityScore, contentBasedScore, userBasedScore}, weights);

            // Then:
            System.out.printf("%s/1 + %s/2 + %s/3 = %s (des: %s, dif:%d/%d)%n", popularityScore.toShortString(),
                    contentBasedScore.toShortString(), userBasedScore.toShortString(), score.toShortString(),
                    desiredCombinedScore.toShortString(), round(abs(score.value - desiredCombinedScore.value) * 100),
                    round(abs(score.confidence - desiredCombinedScore.confidence) * 100));
        }
    }

    @Test
    public void combineWeightedTightAndNoDisagreementEffect()
    {
        // This test is mostly visual. The viewer must judge whether the combined scores are good enough compared to the
        // desired outcomes.

        double[][] data = new double[][] {
                // Each row is a sequence of scores, of value/confidence pairs, so v1, c1, v2, c2, etc. The last
                // value/confidence score should be the result of combining the scores before it.
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
            List<Score> scores = new ArrayList<>();
            List<Double> weights = new ArrayList<>();
            Score desiredScore = null;
            int i = 0;
            while (i < row.length) {
                if (i + 3 < row.length) {
                    scores.add(new Score(row[i], row[i + 1]));
                    weights.add(row[i + 2]);
                    i += 3;
                } else {
                    desiredScore = new Score(row[i], row[i + 1]);
                    i += 2;
                }
            }

            // When:
            Score result = ScoreMath.combine01TightAndNoDisagreementEffect(toPrimitiveScoreArray(scores), toPrimitiveDoubleArray(weights));

            // Then:
            for (int i1 = 0; i1 < scores.size(); i1++) {
                Score score = scores.get(i1);
                double weight = weights.get(i1);
                System.out.printf("%s/%s ", score.toShortString(), weight);
            }
            System.out.printf("= %s (des: %s, dif:%s/%s)%n", result.toShortString(), desiredScore.toShortString(),
                    asPercentage(abs(result.value - desiredScore.value)),
                    asPercentage(abs(result.confidence - desiredScore.confidence)));
        }
    }

    @Test
    public void zeroOneRangeToMinusOneOneRange()
    {
        double eps = .000001;
        assertThat(Range.from01toM11(0, NEUTRAL_SCORE), closeTo(-1, eps));
        assertThat(Range.from01toM11(NEUTRAL_SCORE / 2, NEUTRAL_SCORE), closeTo(-0.5, eps));
        assertThat(Range.from01toM11(NEUTRAL_SCORE, NEUTRAL_SCORE), closeTo(0, eps));
        assertThat(Range.from01toM11(NEUTRAL_SCORE + (1 - NEUTRAL_SCORE) / 2, NEUTRAL_SCORE), closeTo(0.5, eps));
        assertThat(Range.from01toM11(1, NEUTRAL_SCORE), closeTo(1, eps));
    }

    @Test
    public void minusOneOneRangeToZeroOneRange()
    {
        double eps = .000001;
        assertThat(Range.fromM11to01(-1, NEUTRAL_SCORE), closeTo(0, eps));
        assertThat(Range.fromM11to01(-0.5, NEUTRAL_SCORE), closeTo(NEUTRAL_SCORE / 2, eps));
        assertThat(Range.fromM11to01(0, NEUTRAL_SCORE), closeTo(NEUTRAL_SCORE, eps));
        assertThat(Range.fromM11to01(.5, NEUTRAL_SCORE), closeTo(NEUTRAL_SCORE + (1 - NEUTRAL_SCORE) / 2, eps));
        assertThat(Range.fromM11to01(1, NEUTRAL_SCORE), closeTo(1, eps));
    }
}
