package asap.visualprosody;

import java.util.List;

import lombok.Data;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

import com.google.common.collect.ImmutableList;

/**
 * Gaussian mixture model implementation
 * @author herwinvw
 */
public class GaussianMixtureModel
{
    private final List<GaussianMixture> mixtures;

    @Data
    public static final class GaussianMixture
    {
        final double weight;
        final MultivariateNormalDistribution distribution;

        public double getDensity(double vals[])
        {
            return distribution.density(vals);
        }
    }

    public GaussianMixtureModel(List<GaussianMixture> mixtures)
    {
        this.mixtures = ImmutableList.copyOf(mixtures);
    }

    public double density(double vals[])
    {
        double density = 0;
        for (GaussianMixture gm : mixtures)
        {
            density += gm.getDensity(vals) * gm.weight;            
        }
        return density;
    }

    /**
     * Get the weighted average of the means of the mixtures in the GMM
     */
    public double[] getMu()
    {
        double mean[] = new double[mixtures.get(0).getDistribution().getMeans().length];
        for (GaussianMixture gm : mixtures)
        {
            for (int i = 0; i < gm.getDistribution().getDimension(); i++)
            {
                mean[i] += gm.getDistribution().getMeans()[i] * gm.getWeight();
            }
        }
        return mean;
    }

    /**
     * Get the weighted average of the variance of the mixtures in the GMM
     */
    public double[] getVar()
    {
        double var[] = new double[mixtures.get(0).getDistribution().getMeans().length];
        for (GaussianMixture gm : mixtures)
        {
            for (int i = 0; i < gm.getDistribution().getDimension(); i++)
            {
                var[i] += gm.getDistribution().getCovariances().getEntry(i, i) * gm.getWeight();
            }
        }
        return var;
    }
}
