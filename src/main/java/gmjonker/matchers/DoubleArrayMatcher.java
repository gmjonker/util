package gmjonker.matchers;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;

public class DoubleArrayMatcher {

    public static Matcher<Double[]> arrayCloseTo(double[] array, double error) {
        List<Matcher<? super Double>> matchers = new ArrayList<>();
        for (double d : array)
            matchers.add(closeTo(d, error));
        return arrayContaining(matchers);
    }

}
