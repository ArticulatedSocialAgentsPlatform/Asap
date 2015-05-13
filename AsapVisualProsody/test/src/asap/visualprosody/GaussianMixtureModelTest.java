package asap.visualprosody;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.junit.Test;

import asap.visualprosody.GaussianMixtureModel.GaussianMixture;

import com.google.common.collect.ImmutableList;



/**
 * Unit tests for the GaussianMixture
 * @author herwinvw
 *
 */
public class GaussianMixtureModelTest
{
    private static final double PRECISION = 0.00001d;

    @Test
    public void testOneGaussianOneDimension()
    {
        GaussianMixture gm = new GaussianMixture(1, new MultivariateNormalDistribution(new double[] { 0 }, new double[][] { { 1 } }));
        GaussianMixtureModel gmm = new GaussianMixtureModel(ImmutableList.of(gm));
        assertEquals(1 / Math.sqrt(2 * Math.PI), gmm.density(new double[] { 0 }), PRECISION);
    }

    @Test
    public void testOneGaussianTwoDimensions()
    {
        GaussianMixture gm = new GaussianMixture(1, new MultivariateNormalDistribution(new double[] { 0, 1 }, new double[][] { { 1, 0 },
                { 0, 1 } }));
        GaussianMixtureModel gmm = new GaussianMixtureModel(ImmutableList.of(gm));
        assertEquals(1 / (2 * Math.PI), gmm.density(new double[] { 0, 1 }), PRECISION);
    }
    
    @Test
    public void testTwoGaussiansOneDimension()
    {
        GaussianMixture gm1 = new GaussianMixture(0.7,new MultivariateNormalDistribution(new double[] { 10 }, new double[][] { { 1 }}));
        GaussianMixture gm2 = new GaussianMixture(0.3,new MultivariateNormalDistribution(new double[] { -10 }, new double[][] { { 1 }}));
        GaussianMixtureModel gmm = new GaussianMixtureModel(ImmutableList.of(gm1,gm2));
        assertEquals(0.7 / Math.sqrt(2 * Math.PI),gmm.density(new double[]{10}),PRECISION);
        assertEquals(0.3 / Math.sqrt(2 * Math.PI),gmm.density(new double[]{-10}),PRECISION);
    }
}
