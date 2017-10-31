package gmjonker.matchers;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;

public class IterableCloseToMatcher {

    public static Matcher<Iterable<? extends Double>> isIterableCloseTo(Iterable<Double> iterable, double error) {
        List<Matcher<? super Double>> matchers = new ArrayList<>();
        for (double d : iterable)
            matchers.add(closeTo(d, error));
        return contains(matchers);
    }
}
