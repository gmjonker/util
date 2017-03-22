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
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(-1, 1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(-1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, .1), new Indication( 1, .1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication(-1, .1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, .1), new Indication(-1, .1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication( 1, .1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        
        // Point-pairs with low joint confidence should add little to the score
        System.out.println();
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication( 1, 1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication( 1, .1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication( 1, 0));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
    }

    @Test
    public void covarianceWithConfidence()
    {
        // Same cases as above
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovarianceWithConfidence());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getCovarianceWithConfidence()() = " + indicationCovariance.getCovarianceWithConfidence());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(-1, 1));
            System.out.println("indicationCovariance.getCovarianceWithConfidence()() = " + indicationCovariance.getCovarianceWithConfidence());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(-1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getCovarianceWithConfidence()() = " + indicationCovariance.getCovarianceWithConfidence());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, .1), new Indication( 1, .1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication(-1, .1));
            System.out.println("indicationCovariance.getCovarianceWithConfidence()() = " + indicationCovariance.getCovarianceWithConfidence());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, .1), new Indication(-1, .1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication( 1, .1));
            System.out.println("indicationCovariance.getCovarianceWithConfidence()() = " + indicationCovariance.getCovarianceWithConfidence());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication( 1, 1));
            System.out.println("indicationCovariance.getCovarianceWithConfidence()() = " + indicationCovariance.getCovarianceWithConfidence());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication( 1, .1));
            System.out.println("indicationCovariance.getCovarianceWithConfidence()() = " + indicationCovariance.getCovarianceWithConfidence());
        }
        {
            IndicationCovarianceOnline indicationCovariance = new IndicationCovarianceOnline();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication( 1, 0));
            System.out.println("indicationCovariance.getCovarianceWithConfidence()() = " + indicationCovariance.getCovarianceWithConfidence());
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