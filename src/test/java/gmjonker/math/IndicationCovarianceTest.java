package gmjonker.math;

import org.junit.*;

public class IndicationCovarianceTest
{
    @Test
    public void test()
    {
        // Basic cases
        {
            IndicationCovariance indicationCovariance = new IndicationCovariance();
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovariance indicationCovariance = new IndicationCovariance();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovariance indicationCovariance = new IndicationCovariance();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(-1, 1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovariance indicationCovariance = new IndicationCovariance();
            indicationCovariance.addDataPoint(new Indication(1, 1), new Indication(-1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovariance indicationCovariance = new IndicationCovariance();
            indicationCovariance.addDataPoint(new Indication( 1, .1), new Indication( 1, .1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication(-1, .1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovariance indicationCovariance = new IndicationCovariance();
            indicationCovariance.addDataPoint(new Indication( 1, .1), new Indication(-1, .1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication( 1, .1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        
        // Point-pairs with low joint confidence should add little to the score
        System.out.println();
        {
            IndicationCovariance indicationCovariance = new IndicationCovariance();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication( 1, 1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovariance indicationCovariance = new IndicationCovariance();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, .1), new Indication( 1, .1));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
        {
            IndicationCovariance indicationCovariance = new IndicationCovariance();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication( 1, 0));
            System.out.println("indicationCovariance.getCovariance() = " + indicationCovariance.getCovariance());
        }
    }

    @Test
    public void pearson()
    {
        {
            IndicationCovariance indicationCovariance = new IndicationCovariance();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(-1, 1));
            System.out.println("indicationCovariance.getPearsonSimilarity() = " + indicationCovariance.getPearsonSimilarity());
        }
        {
            IndicationCovariance indicationCovariance = new IndicationCovariance();
            indicationCovariance.addDataPoint(new Indication( 1, 1), new Indication(1, 1));
            indicationCovariance.addDataPoint(new Indication(-1, 1), new Indication(1, 1));
            System.out.println("indicationCovariance.getPearsonSimilarity() = " + indicationCovariance.getPearsonSimilarity());
        }
    }
}