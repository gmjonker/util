package gmjonker.math;

import org.junit.*;

import static gmjonker.TestUtil.ind;
import static java.util.Arrays.asList;

public class IndicationCorrelationOnlineTest
{
    @Test
    public void test()
    {
        // Basic cases
        print(new IndicationCorrelationOnline(
        ));
        print(new IndicationCorrelationOnline(
                asList(ind( 1,  1)),
                asList(ind( 1,  1))
        ));
        print(new IndicationCorrelationOnline(
                asList(ind( 1,  1), ind(-1,  1)),
                asList(ind( 1,  1), ind(-1,  1))
        ));
        print(new IndicationCorrelationOnline(
                asList(ind( 1,  1), ind(-1,  1)),
                asList(ind(-1,  1), ind( 1,  1))
        ));
        print(new IndicationCorrelationOnline(
                asList(ind( 1, .1), ind(-1, .1)),
                asList(ind( 1, .1), ind(-1, .1))
        ));
        print(new IndicationCorrelationOnline(
                asList(ind( 1, .1), ind(-1, .1)),
                asList(ind(-1, .1), ind( 1, .1))
        ));
        System.out.println();
        print(new IndicationCorrelationOnline(
                asList(ind( 1,  1), ind(-1,  1)),
                asList(ind( 1,  1), ind( 1,  1))
        ));
        print(new IndicationCorrelationOnline(
                asList(ind( 1,  1), ind(-1, .1)),
                asList(ind( 1,  1), ind( 1, .1))
        ));
        print(new IndicationCorrelationOnline(
                asList(ind( 1,  1), ind( 1,  1)),
                asList(ind(-1,  1), ind( 1,  0))
        ));
    }

    private void print(IndicationCorrelationOnline cov)
    {
        System.out.println("s1 = " + cov.getSeries1());
        System.out.println("s2 = " + cov.getSeries2());
        System.out.println("cov0 = " + cov.getCovariance0());
        if (cov.getN() > 1)
            System.out.println("pear = " + cov.getPearsonSimilarity());
        System.out.println();
    }

    @Test
    public void covarianceWithConfidence()
    {
        // Same cases as above
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            printCovInd(cov);
        }
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            cov.addDataPoint(ind(1, 1), ind(1, 1));
            printCovInd(cov);
        }
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            cov.addDataPoint(ind(1, 1), ind(1, 1));
            cov.addDataPoint(ind(-1, 1), ind(-1, 1));
            printCovInd(cov);
        }
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            cov.addDataPoint(ind(1, 1), ind(-1, 1));
            cov.addDataPoint(ind(-1, 1), ind(1, 1));
            printCovInd(cov);
        }
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            cov.addDataPoint(ind( 1, .1), ind( 1, .1));
            cov.addDataPoint(ind(-1, .1), ind(-1, .1));
            printCovInd(cov);
        }
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            cov.addDataPoint(ind( 1, .1), ind(-1, .1));
            cov.addDataPoint(ind(-1, .1), ind( 1, .1));
            printCovInd(cov);
        }
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            cov.addDataPoint(ind( 1, 1), ind(1, 1));
            cov.addDataPoint(ind(-1, 1), ind( 1, 1));
            printCovInd(cov);
        }
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            cov.addDataPoint(ind( 1, 1), ind(1, 1));
            cov.addDataPoint(ind(-1, .1), ind( 1, .1));
            printCovInd(cov);
        }
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            cov.addDataPoint(ind(1, 1), ind( 1, .1));
            cov.addDataPoint(ind(1, 1), ind( 1, .1));
            cov.addDataPoint(ind(1, 1), ind( 1, .1));
            printCovInd(cov);
        }
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            cov.addDataPoint(ind( 1, 1), ind(1, 1));
            cov.addDataPoint(ind(-1, 1), ind( 1, 0));
            printCovInd(cov);
        }
    }

    private void printCovInd(IndicationCorrelationOnline cov)
    {
        System.out.println("s1 = " + cov.getSeries1());
        System.out.println("s2 = " + cov.getSeries2());
        System.out.println("cov = " + cov.getCovariance0AsIndication());
        System.out.println();
    }

    @Test
    public void pearson()
    {
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            cov.addDataPoint(ind( 1, 1), ind(1, 1));
            cov.addDataPoint(ind(-1, 1), ind(-1, 1));
            System.out.println("cov.getPearsonSimilarity() = " + cov.getPearsonSimilarity());
        }
        {
            IndicationCorrelationOnline cov = new IndicationCorrelationOnline();
            cov.addDataPoint(ind( 1, 1), ind(1, 1));
            cov.addDataPoint(ind(-1, 1), ind(1, 1));
            System.out.println("cov.getPearsonSimilarity() = " + cov.getPearsonSimilarity());
        }
    }
}