package asap.visualprosody;

import hmi.animation.ConfigList;
import hmi.animation.Hanim;
import hmi.animation.SkeletonInterpolator;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

import java.util.Arrays;

public class VisualProsody
{
    private VisualProsodyLeNumericalDiff vpp;
    private float offset[] = Vec3f.getVec3f();
    private double[] rpyPrev;
    private double[] rpyPrevPrev;

    public VisualProsody(GaussianMixtureModel gmmRollVoiced, GaussianMixtureModel gmmPitchVoiced, GaussianMixtureModel gmmYawVoiced,
            GaussianMixtureModel gmmVelocityVoiced, GaussianMixtureModel gmmAccelerationVoiced, float[] offset)
    {
        Vec3f.set(this.offset, offset);
        vpp = new VisualProsodyLeNumericalDiff(gmmRollVoiced, gmmPitchVoiced, gmmYawVoiced, gmmVelocityVoiced, gmmAccelerationVoiced);
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
            if (audio.getF0()[i] > 10)
            {
                rpy = vpp.generateHeadPose(rpyPrev, rpyPrevPrev, audio.getF0()[i], audio.getRmsEnergy()[i]);
            }
            else
            {
                rpy = Arrays.copyOf(rpyPrev, 3);
            }
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
