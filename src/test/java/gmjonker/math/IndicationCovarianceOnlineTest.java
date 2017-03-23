package gmjonker.math;

import org.junit.*;

public class IndicationCovarianceOnlineTest
{
    @Test
    public void test()
    {
        // Basic cases
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            System.out.println("indicationCovariance.getCovariance0AsDouble() = " + indicationCovariance.getCovariance0AsDouble());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getCovariance0AsDouble() = " + indicationCovariance.getCovariance0AsDouble());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(-1, 1));
            System.out.println("indicationCovariance.getCovariance0AsDouble() = " + indicationCovariance.getCovariance0AsDouble());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(-1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getCovariance0AsDouble() = " + indicationCovariance.getCovariance0AsDouble());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, .1), new Indication( 1, .1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication(-1, .1));
            System.out.println("indicationCovariance.getCovariance0AsDouble() = " + indicationCovariance.getCovariance0AsDouble());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, .1), new Indication(-1, .1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication( 1, .1));
            System.out.println("indicationCovariance.getCovariance0AsDouble() = " + indicationCovariance.getCovariance0AsDouble());
        }
        
        // Point-pairs with low joint confidence should add little to the score
        System.out.println();
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication( 1, 1));
            System.out.println("indicationCovariance.getCovariance0AsDouble() = " + indicationCovariance.getCovariance0AsDouble());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication( 1, .1));
            System.out.println("indicationCovariance.getCovariance0AsDouble() = " + indicationCovariance.getCovariance0AsDouble());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication( 1, 0));
            System.out.println("indicationCovariance.getCovariance0AsDouble() = " + indicationCovariance.getCovariance0AsDouble());
        }
    }

    @Test
    public void covarianceWithConfidence()
    {
        // Same cases as above
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            System.out.println("indicationCovariance.getCovariance0AsDouble() = " + indicationCovariance.getCovariance0AsIndication());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getCovariance0AsIndication()() = " + indicationCovariance.getCovariance0AsIndication());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(-1, 1));
            System.out.println("indicationCovariance.getCovariance0AsIndication()() = " + indicationCovariance.getCovariance0AsIndication());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(-1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getCovariance0AsIndication()() = " + indicationCovariance.getCovariance0AsIndication());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, .1), new Indication( 1, .1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication(-1, .1));
            System.out.println("indicationCovariance.getCovariance0AsIndication()() = " + indicationCovariance.getCovariance0AsIndication());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, .1), new Indication(-1, .1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication( 1, .1));
            System.out.println("indicationCovariance.getCovariance0AsIndication()() = " + indicationCovariance.getCovariance0AsIndication());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication( 1, 1));
            System.out.println("indicationCovariance.getCovariance0AsIndication()() = " + indicationCovariance.getCovariance0AsIndication());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication( 1, .1));
            System.out.println("indicationCovariance.getCovariance0AsIndication()() = " + indicationCovariance.getCovariance0AsIndication());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication( 1, 0));
            System.out.println("indicationCovariance.getCovariance0AsIndication()() = " + indicationCovariance.getCovariance0AsIndication());
        }
    }

    @Test
    public void pearson()
    {
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(-1, 1));
            System.out.println("indicationCovariance.getPearsonSimilarity() = " + indicationCovariance.getPearsonSimilarity());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getPearsonSimilarity() = " + indicationCovariance.getPearsonSimilarity());
        }
    }
}