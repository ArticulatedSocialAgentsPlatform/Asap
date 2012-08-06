package asap.animationengine.gaze;

import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.EyeSaturation;
import hmi.neurophysics.ListingsLaw;
import hmi.worldobjectenvironment.WorldObject;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;

/**
 * Dynamically keeps the gaze on target. Creates transitions that are also dynamic.
 * @author harsens
 * 
 */
public class DynamicRestGaze implements RestGaze
{
    private AnimationPlayer aniPlayer;
    private final WorldObject target;
    private float[] offsetRotation = Quat4f.getIdentity();

    public DynamicRestGaze(WorldObject target)
    {
        this.target = target;
    }

    @Override
    public RestGaze copy(AnimationPlayer player)
    {
        RestGaze copy = new DynamicRestGaze(target);
        copy.setAnimationPlayer(aniPlayer);
        return copy;
    }

    @Override
    public void setAnimationPlayer(AnimationPlayer player)
    {
        aniPlayer = player;
    }

    private void setEyeRotation(VJoint eye)
    {
        float[] gazeDir = Vec3f.getVec3f();
        target.getTranslation2(gazeDir, eye);
        Quat4f.transformVec3f(offsetRotation, gazeDir);
        Vec3f.normalize(gazeDir);
        float q[] = Quat4f.getQuat4f();
        float qEye[] = Quat4f.getQuat4f();
        ListingsLaw.listingsEye(gazeDir, q);
        EyeSaturation.sat(q, Quat4f.getIdentity(), qEye);
        eye.setRotation(qEye);
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {

        // TODO Auto-generated method stub

    }

    @Override
    public TimedAnimationUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedAnimationUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, double duration,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getTransitionToRestDuration(VJoint vCurrent, Set<String> joints)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public AnimationUnit createTransitionToRest(Set<String> joints)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setRestPose()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public GazeShiftTMU createGazeShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
            throws MUSetupException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
