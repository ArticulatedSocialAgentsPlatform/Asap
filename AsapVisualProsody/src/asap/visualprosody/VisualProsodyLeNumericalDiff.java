package asap.visualprosody;

import java.util.Arrays;

import weka.core.Optimization;

/**
 * Modified implementation of the head movement of<br>
 * Live Speech Driven Head-and-Eye Motion Generators(2012)<br>
 * IEEE Transactions on Visualization and Computer Graphics 18:11(1902-1914)<br>
 * 
 * @author hvanwelbergen
 *
 */
public class VisualProsodyLeNumericalDiff
{
    private final GaussianMixtureModel gmmPitchVoiced;
    private final GaussianMixtureModel gmmYawVoiced;
    private final GaussianMixtureModel gmmRollVoiced;
    private final GaussianMixtureModel gmmVelocityVoiced;
    private final GaussianMixtureModel gmmAccelerationVoiced;
    private final GaussianMixtureModel[] gmmRPY;
    private final double VELOCITY_SCALE = 1d / 8d;
    private final double ACCELERATION_SCALE = 1d / 16d;
    private double positionWeight = 1;
    private double velocityWeight = 1;
    private double accelerationWeight = 1;

    public VisualProsodyLeNumericalDiff(GaussianMixtureModel gmmRollVoiced, GaussianMixtureModel gmmPitchVoiced,
            GaussianMixtureModel gmmYawVoiced, GaussianMixtureModel gmmVelocityVoiced, GaussianMixtureModel gmmAccelerationVoiced)
    {
        this.gmmPitchVoiced = gmmPitchVoiced;
        this.gmmYawVoiced = gmmYawVoiced;
        this.gmmRollVoiced = gmmRollVoiced;
        this.gmmVelocityVoiced = gmmVelocityVoiced;
        this.gmmAccelerationVoiced = gmmAccelerationVoiced;
        this.gmmRPY = new GaussianMixtureModel[] { gmmRollVoiced, gmmPitchVoiced, gmmYawVoiced };
    }

    public void setVelocityWeight(double w)
    {
        this.velocityWeight = w;
    }

    public void setPositionWeight(double w)
    {
        this.positionWeight = w;
    }

    public void setAccelerationWeight(double w)
    {
        this.accelerationWeight = w;
    }

    public double minplog(double[] rpy, double[] rpyPrev, double[] rpyPrevPrev, double pitch, double rmsLoudness, double frameSynthDur,
            double frameDataDur)
    {
        double velocity = getVelocity(rpy, rpyPrev, frameSynthDur, frameDataDur);
        double acceleration = getAcceleration(rpy, rpyPrev, rpyPrevPrev, frameSynthDur, frameDataDur);
        if (pitch > 0)
        {
            return -positionWeight * Math.log(gmmRollVoiced.density(new double[] { rpy[0], pitch, rmsLoudness })) - positionWeight
                    * Math.log(gmmPitchVoiced.density(new double[] { rpy[1], pitch, rmsLoudness })) - positionWeight
                    * Math.log(gmmYawVoiced.density(new double[] { rpy[2], pitch, rmsLoudness }))
                    - Math.log(gmmVelocityVoiced.density(new double[] { velocity, pitch, rmsLoudness })) * VELOCITY_SCALE * velocityWeight
                    - Math.log(gmmAccelerationVoiced.density(new double[] { acceleration, pitch, rmsLoudness })) * ACCELERATION_SCALE
                    * accelerationWeight;
        }
        else
        {
            return 0;
        }
    }

    private double getVelocity(double[] rpy, double[] rpyPrev, double frameSynthDur, double frameDataDur)
    {
        double velocity = 0;
        for (int i = 0; i < 3; i++)
        {
            velocity += (rpy[i] - rpyPrev[i]) * (rpy[i] - rpyPrev[i]);
        }
        velocity = Math.sqrt(velocity) * (frameDataDur / frameSynthDur);
        return velocity;
    }

    private double getAcceleration(double[] rpy, double[] rpyPrev, double[] rpyPrevPrev, double frameSynthDur, double frameDataDur)
    {
        double acceleration = 0;
        for (int i = 0; i < 3; i++)
        {
            acceleration += (rpy[i] - 2 * rpyPrev[i] + rpyPrevPrev[i]) * (rpy[i] - 2 * rpyPrev[i] + rpyPrevPrev[i]);
        }
        acceleration = Math.sqrt(acceleration) * (frameDataDur / frameSynthDur) * (frameDataDur / frameSynthDur);
        return acceleration;
    }

    public double[] numGradMinplog(double[] rpy, double[] rpyPrev, double[] rpyPrevPrev, double pitch, double rmsLoudness, double delta,
            double frameSynthDur, double frameDataDur)
    {
        if (pitch <= 0)
        {
            return new double[] { 0, 0, 0 };
        }

        double diff[] = new double[3];

        double posDensities[] = new double[] {
                -positionWeight * Math.log(gmmRollVoiced.density(new double[] { rpy[0], pitch, rmsLoudness })),
                -positionWeight * Math.log(gmmPitchVoiced.density(new double[] { rpy[1], pitch, rmsLoudness })),
                -positionWeight * Math.log(gmmYawVoiced.density(new double[] { rpy[2], pitch, rmsLoudness })) };
        double velocity = getVelocity(rpy, rpyPrev, frameSynthDur, frameDataDur);
        double acceleration = getAcceleration(rpy, rpyPrev, rpyPrevPrev, frameSynthDur, frameDataDur);
        double p = posDensities[0] + posDensities[1] + posDensities[2]
                - Math.log(gmmVelocityVoiced.density(new double[] { velocity, pitch, rmsLoudness })) * VELOCITY_SCALE * velocityWeight
                - Math.log(gmmAccelerationVoiced.density(new double[] { acceleration, pitch, rmsLoudness })) * ACCELERATION_SCALE
                * accelerationWeight;

        for (int i = 0; i < 3; i++)
        {
            double rpyDelta[] = Arrays.copyOf(rpy, 3);
            rpyDelta[i] += delta;
            double vel = getVelocity(rpyDelta, rpyPrev, frameSynthDur, frameDataDur);
            double acc = getAcceleration(rpyDelta, rpyPrev, rpyPrevPrev, frameSynthDur, frameDataDur);
            for (int j = 0; j < 3; j++)
            {
                if (j != i)
                {
                    diff[i] += posDensities[j];
                }
                else
                {
                    diff[i] += -positionWeight * Math.log(gmmRPY[i].density(new double[] { rpyDelta[i], pitch, rmsLoudness }));
                }
            }
            diff[i] -= Math.log(gmmVelocityVoiced.density(new double[] { vel, pitch, rmsLoudness })) * VELOCITY_SCALE * velocityWeight;
            diff[i] -= Math.log(gmmAccelerationVoiced.density(new double[] { acc, pitch, rmsLoudness })) * ACCELERATION_SCALE
                    * accelerationWeight;
            diff[i] -= p;
            diff[i] /= delta;
        }
        return diff;
    }

    public double[] generateHeadPose(final double rpyPrev[], final double rpyPrevPrev[], final double pitch, final double rmsLoudness,
            double frameSynthDur, double frameDataDur)
    {
        class MyOpt extends Optimization
        {
            // Provide the objective function
            protected double objectiveFunction(double[] rpy)
            {
                return minplog(rpy, rpyPrev, rpyPrevPrev, pitch, rmsLoudness, frameSynthDur, frameDataDur);
            }

            // Provide the first derivatives
            protected double[] evaluateGradient(double[] rpy)
            {

                double delta = 0.001d;
                /*
                 * double p = minplog(rpy, rpyPrev, rpyPrevPrev, pitch, rmsLoudness, frameSynthDur, frameDataDur);
                 * double proll = minplog(new double[] { rpy[0] + delta, rpy[1], rpy[2] }, rpyPrev, rpyPrevPrev, pitch, rmsLoudness,
                 * frameSynthDur, frameDataDur);
                 * double ppitch = minplog(new double[] { rpy[0], rpy[1] + delta, rpy[2] }, rpyPrev, rpyPrevPrev, pitch, rmsLoudness,
                 * frameSynthDur, frameDataDur);
                 * double pyaw = minplog(new double[] { rpy[0], rpy[1], rpy[2] + delta }, rpyPrev, rpyPrevPrev, pitch, rmsLoudness,
                 * frameSynthDur, frameDataDur);
                 * return new double[] { (proll - p) / delta, (ppitch - p) / delta, (pyaw - p) / delta };
                 */
                return numGradMinplog(rpy, rpyPrev, rpyPrevPrev, pitch, rmsLoudness, delta, frameSynthDur, frameDataDur);
            }

            @Override
            public String getRevision()
            {
                return "";
            }
        }
        MyOpt opt = new MyOpt();
        double[][] constraints = new double[][] { { -90, -90, -90 }, { 90, 90, 90 } };
        try
        {
            return opt.findArgmin(rpyPrev, constraints);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
