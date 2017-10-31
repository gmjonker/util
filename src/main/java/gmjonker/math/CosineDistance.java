package gmjonker.math;

/**
 * Copied from Mahout.
 * 
 * This class implements a cosine distance metric by dividing the dot product of two vectors by the product of their
 * lengths.  That gives the cosine of the angle between the two vectors.  To convert this to a usable distance,
 * 1-cos(angle) is what is actually returned.
 */
public class CosineDistance
{
    /**
     * @param p1
     * @param p2
     * @return Result in (0,2)
     */
    public static double distance(double[] p1, double[] p2) {
        assert p1.length == p2.length;
        double dotProduct = 0.0;
        double lengthSquaredp1 = 0.0;
        double lengthSquaredp2 = 0.0;
        for (int i = 0; i < p1.length; i++) {
            lengthSquaredp1 += p1[i] * p1[i];
            lengthSquaredp2 += p2[i] * p2[i];
            dotProduct += p1[i] * p2[i];
        }
        double denominator = Math.sqrt(lengthSquaredp1) * Math.sqrt(lengthSquaredp2);

        // correct for floating-point rounding errors
        if (denominator < dotProduct) {
            denominator = dotProduct;
        }

        // correct for zero-vector corner case
        if (denominator == 0 && dotProduct == 0) {
            return 0;
        }

        return 1.0 - dotProduct / denominator;
    }
}

