package gmjonker.math;

import java.util.Arrays;
import java.util.function.Function;

public class TextPlot
{
    /**
     * Plots a function.
     *
     * <p>If there is a point in the function that interests you particularly, consider choosing xmin/xmax and/or ymin/ymax
     * and width and height such that this point falls on an ascii grid point.
     */
    public static void plotf(Function<Double, Double> f, double xmin, double xmax, double ymin, double ymax,
            Integer width, Integer height)
    {
        if (width == null || width == 0) width = 40;
        if (height == null || height == 0) height = 20;

        char[][] points = new char[width][height + 1];
        for (int v = 0; v < width; v++)
            Arrays.fill(points[v], ' ');

        for (int v = 0; v < width; v++) {
            double x = xmin + 1.0 * v / width * (xmax - xmin);
            double y = f.apply(x);
            int yIndex = height - (int) Math.round((y - ymin) / (ymax - ymin) * height);
            if (yIndex >= 0 && yIndex <= height)
                points[v][yIndex] = '+';
        }
        for (int w = 0; w < height + 1; w++) {
            for (int v = 0; v < width; v++) {
                System.out.print(points[v][w]);
            }
            System.out.println();
        }
    }

    /**
     * Plots a number of y values next to each other.
     */
    public static void plotValues(double[] yValues, Integer height)
    {
        if (height == null)
            height = 30;
        int width = yValues.length;
        // in x/y form, or column/row
        char[][] points = new char[width][height + 1];
        double ymin = Double.MAX_VALUE;
        double ymax = Double.MIN_VALUE;
        for (int v = 0; v < yValues.length; v++) {
            double y = yValues[v];
            ymin = Math.min(ymin, y);
            ymax = Math.max(ymax, y);
            Arrays.fill(points[v], ' ');
        }
        for (int v = 0; v < yValues.length; v++) {
            double y = yValues[v];
            int yIndex = height - (int) Math.round((y - ymin) / (ymax - ymin) * height);
            points[v][yIndex] = '+';
        }
        for (int w = 0; w < height + 1; w++) {
            for (int v = 0; v < width; v++) {
                System.out.print(points[v][w]);
            }
            System.out.println();
        }
    }

    /**
     * Plots a number of y values next to each other.
     */
    public static void plotValues(int[] yValues, Integer height)
    {
        double[] yv = new double[yValues.length];
        for (int i = 0; i < yValues.length; i++)
            yv[i] = yValues[i];
        plotValues(yv, height);
    }

    /**
     * Plots a number of x/y points. Origin is top left corner.
     */
    public static void plotPoints(double[] xs, double[] ys, Integer width, Integer height)
    {
        plotPoints(xs, ys, width, height, false, false);
    }

    /**
     * Plots a number of x/y points. Origin is top left corner.
     */
    public static void plotPoints(double[] xs, double[] ys, Integer width, Integer height, boolean flipX, boolean flipY)
    {
        if (width == 0) width = 40;
        if (height == 0) height = 40;
        double xmin = Double.MAX_VALUE;
        double xmax = Double.MIN_VALUE;
        double ymin = Double.MAX_VALUE;
        double ymax = Double.MIN_VALUE;
        for (int i = 0; i < xs.length; i++) {
            xmin = Math.min(xmin, xs[i]);
            xmax = Math.max(xmax, xs[i]);
            ymin = Math.min(ymin, ys[i]);
            ymax = Math.max(ymax, ys[i]);
        }
        char[][] points = new char[width + 1][height + 1];
        for (int v = 0; v < width + 1; v++)
            Arrays.fill(points[v], ' ');
        for (int i = 0; i < xs.length; i++) {
            double x = xs[i];
            double y = ys[i];
            int v = flipX ? (int) Math.round(width - ((x - xmin) / (xmax - xmin) * width))
                          : (int) Math.round((x - xmin) / (xmax - xmin) * width);
            int w = flipY ? (int) Math.round(height - ((y - ymin) / (ymax - ymin) * height))
                          : (int) Math.round((y - ymin) / (ymax - ymin) * height);
            switch (points[v][w]) {
                case ' ': points[v][w] = '░'; break;
                case '░': points[v][w] = '▒'; break;
                case '▒': points[v][w] = '▓'; break;
                case '▓': points[v][w] = '▓'; break;
                default:  points[v][w] = '?'; break;
            }
        }
        for (int w = 0; w < height + 1; w++) {
            for (int v = 0; v < width + 1; v++) {
                System.out.print(points[v][w]);
            }
            System.out.println();
        }
    }

}
