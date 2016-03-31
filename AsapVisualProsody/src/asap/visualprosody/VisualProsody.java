package asap.visualprosody;

import hmi.animation.ConfigList;
import hmi.animation.Hanim;
import hmi.animation.SkeletonInterpolator;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

import java.util.Arrays;

import lombok.Getter;

/**
 * Automatic, incrementally constructed head motion on the basis of speech features (pitch, energy)
 * @author hvanwelbergen
 *
 */
public class VisualProsody
{
    private VisualProsodyLeNumericalDiff vpp;
    
    @Getter
    private float offset[] = Vec3f.getVec3f();
    private double[] rpyPrev;
    private double[] rpyPrevPrev;

    public VisualProsody(GaussianMixtureModel gmmRollVoiced, GaussianMixtureModel gmmPitchVoiced, GaussianMixtureModel gmmYawVoiced,
            GaussianMixtureModel gmmVelocityVoiced, GaussianMixtureModel gmmAccelerationVoiced, float[] offset)
    {
        Vec3f.set(this.offset, offset);
        vpp = new VisualProsodyLeNumericalDiff(gmmRollVoiced, gmmPitchVoiced, gmmYawVoiced, gmmVelocityVoiced, gmmAccelerationVoiced);
    }

    public double[] firstHeadMotion(double rpyStart[], double f0, double rmsEnergy, double frameSynthDur, double frameDataDur)
    {
        double startOffsetted[] = new double[3];
        for (int i = 0; i < 3; i++)
        {
            startOffsetted[i] = rpyStart[i] + offset[i];
        }
        return nextHeadMotion(startOffsetted, startOffsetted, f0, rmsEnergy, frameSynthDur, frameDataDur);
    }

    public double[] nextHeadMotion(double rpyPrev[], double rpyPrevPrev[], double f0, double rmsEnergy, double frameSynthDur,
            double frameDataDur)
    {
        double[] rpy;
        if (f0 > 10)
        {
            rpy = vpp.generateHeadPose(rpyPrev, rpyPrevPrev, f0, rmsEnergy, frameSynthDur, frameDataDur);
        }
        else
        {
            rpy = Arrays.copyOf(rpyPrev, 3);
        }
        return new double[] { rpy[0], rpy[1], rpy[2] };
    }

    public SkeletonInterpolator nextHeadMotion(AudioFeatures audio)
    {
        return headMotion(rpyPrev, rpyPrevPrev, audio);
    }

    public SkeletonInterpolator headMotion(double[] rpyPrev, double[] rpyPrevPrev, AudioFeatures audio)
    {
        ConfigList cl = new ConfigList(4);
        double rpy[];

        for (int i = 0; i < audio.getF0().length; i++)
        {
            rpy = nextHeadMotion(rpyPrev, rpyPrevPrev, audio.getF0()[i], audio.getRmsEnergy()[i], audio.getFrameDuration(),
                    audio.getFrameDuration());            
            cl.addConfig(
                    i * audio.getFrameDuration(),
                    Quat4f.getQuat4fFromRollPitchYawDegrees((float) rpy[0] - offset[0], (float) rpy[1] - offset[1], (float) rpy[2]
                            - offset[2]));
            rpyPrevPrev = rpyPrev;
            rpyPrev = rpy;
        }
        return new SkeletonInterpolator(new String[] { Hanim.skullbase }, cl, "R");
    }

    public SkeletonInterpolator headMotion(double[] rpyStart, AudioFeatures audio)
    {
        double startOffsetted[] = new double[3];
        for (int i = 0; i < 3; i++)
        {
            startOffsetted[i] = rpyStart[i] + offset[i];
        }
        rpyPrev = Arrays.copyOf(startOffsetted, 3);
        rpyPrevPrev = Arrays.copyOf(startOffsetted, 3);
        return headMotion(rpyPrev, rpyPrevPrev, audio);
    }
}
