package asap.visualprosody;

import hmi.animation.ConfigList;
import hmi.animation.Hanim;
import hmi.animation.SkeletonInterpolator;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

import java.util.Arrays;

public class VisualProsodyProvider
{
    private VisualProsodyLeNumericalDiff vpp;
    private float offset[] = Vec3f.getVec3f();
    
    public VisualProsodyProvider(GaussianMixtureModel gmmRollVoiced, GaussianMixtureModel gmmPitchVoiced,
            GaussianMixtureModel gmmYawVoiced, GaussianMixtureModel gmmVelocityVoiced, GaussianMixtureModel gmmAccelerationVoiced, float[]offset)
    {
        Vec3f.set(this.offset,offset);
        vpp = new VisualProsodyLeNumericalDiff(gmmRollVoiced, gmmPitchVoiced, gmmYawVoiced, gmmVelocityVoiced, gmmAccelerationVoiced);        
    }

    public SkeletonInterpolator headMotion(double[] rpyStart, AudioFeatures audio)
    {
        ConfigList cl = new ConfigList(4);
        double[] rpy = Arrays.copyOf(rpyStart, 3);
        double[] rpyPrev = Arrays.copyOf(rpyStart, 3);
        double[] rpyPrevPrev = Arrays.copyOf(rpyStart, 3);

        for (int i = 0; i < audio.getF0().length; i++)
        {
            rpyPrevPrev = rpyPrev;
            rpyPrev = rpy;
            if (audio.getF0()[i] > 10)
            {
                rpy = vpp.generateHeadPose(rpyPrev, rpyPrevPrev, audio.getF0()[i], 0.2*audio.getRmsEnergy()[i]);
            }
            else
            {
                rpy = Arrays.copyOf(rpyPrev, 3);
            }
            cl.addConfig(
                    i * audio.getFrameDuration(),
                    Quat4f.getQuat4fFromRollPitchYawDegrees((float) rpy[0] - offset[0], (float) rpy[1] - offset[1], (float) rpy[2]
                            - offset[2]));
        }
        return new SkeletonInterpolator(new String[] { Hanim.skullbase }, cl, "R");
    }
}
