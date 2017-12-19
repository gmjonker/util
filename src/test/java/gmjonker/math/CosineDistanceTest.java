package gmjonker.math;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class CosineDistanceTest {

    @Test
    public void test() throws Exception {
        double eps = .000001;
        assertThat(CosineDistance.distance(new double[]{1, 2}, new double[]{1, 2}), closeTo(0, eps));
        assertThat(CosineDistance.distance(new double[]{1, 1}, new double[]{1, -1}), closeTo(1, eps));
        assertThat(CosineDistance.distance(new double[]{1, 1}, new double[]{-1, -1}), closeTo(2, eps));
    }

}
