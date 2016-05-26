package gmjonker.math;

import gmjonker.util.LambdaLogger;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Prints a histogram in ASCII characters.
 */
public class TextHistogram
{
    private List<Double> values = new ArrayList<>();
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;

    protected static final LambdaLogger log = new LambdaLogger(TextHistogram.class);

    public TextHistogram() {}

    public TextHistogram(List<Double> data)
    {
        this.values = data;
        if ( ! this.values.isEmpty()) {
            this.min = Collections.min(this.values);
            this.max = Collections.max(this.values);
        }
    }

    public TextHistogram(double[] values)
    {
        this(asList(ArrayUtils.toObject(values)));
    }

    public void addValue(double value)
    {
        values.add(value);
        if (value < min)
            min = value;
        if (value > max)
            max = value;
    }

    public void plot(Integer numBins, Integer height)
    {
        if (values.size() == 0)
            return;

        if (numBins == 0) numBins = 40;
        if (height == 0) height = 20;

        int[] counts = new int[numBins];
        // if we would simply divide by numBins, the max value would end up in bin numBins, which is one too high.
        double binSize = (max - min) / (numBins - 1);
        for (double value : values)
            counts[((int) ((value - min) / binSize))]++;

        int maxBinCount = 0;
        int maxBinNr = -1;
        for (int i = 0; i < counts.length; i++) {
            int count = counts[i];
            if (count > maxBinCount) {
                maxBinNr = i;
                maxBinCount = count;
            }
        }

        // points[0][0] is bottom left corner
        char[][] points = new char[numBins][height + 1];
        for (int v = 0; v < numBins; v++)
            Arrays.fill(points[v], ' ');

        for (int v = 0; v < numBins; v++) {
            log.trace("bin {}: {}", v, counts[v]);
            int w = (int) Math.round((double)counts[v] / maxBinCount * height);
            if (w < 0)
                points[v][0] = '?';
            else if (w > height)
                points[v][height] = '?';
            else
                for (int i = 0; i < w; i++)
                    points[v][i] = '+';
        }
        for (int w = height; w >= 0; w--) {
            for (int v = 0; v < numBins; v++) {
                System.out.print(points[v][w]);
            }
            System.out.println();
        }
        for (int v = 0; v < numBins; v++)
            System.out.print("-");
        System.out.println();
        System.out.printf("Max count is %d at bin [%.2f,%.2f]\n", maxBinCount, min + maxBinNr * binSize,
                min + (maxBinNr + 1) * binSize);
        System.out.printf("A bar is %.2f wide\n", binSize);
    }
}
