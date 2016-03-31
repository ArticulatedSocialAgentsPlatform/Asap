/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.EyeSaturation;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Simply looks forward (all influenced joints are 0)
 * @author hvanwelbergen
 *
 */
public class ForwardRestGaze implements RestGaze
{
    private AnimationPlayer aPlayer;
    private final GazeInfluence influence;

    public ForwardRestGaze(GazeInfluence influence)
    {
        this.influence = influence;
    }

    @Override
    public RestGaze copy(AnimationPlayer player)
    {
        ForwardRestGaze restGaze = new ForwardRestGaze(influence);
        restGaze.setAnimationPlayer(player);
        return restGaze;
    }

    @Override
    public void setAnimationPlayer(AnimationPlayer player)
    {
        aPlayer = player;
    }

    private void VOREye(VJoint eye)
    {
        VJoint par = eye.getParent();
        float qp[] = Quat4f.getQuat4f();
        par.getPathRotation(null, qp);
        Quat4f.inverse(qp);
        float q[]=Quat4f.getQuat4f();
        EyeSaturation.sat(qp, Quat4f.getIdentity(), q);
        eye.setRotation(q);
    }
    
    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {
        if (!kinematicJoints.contains(Hanim.r_eyeball_joint) && !kinematicJoints.contains(Hanim.l_eyeball_joint))
        {
            if (aPlayer.getVNextPartBySid(Hanim.r_eyeball_joint) != null && aPlayer.getVNextPartBySid(Hanim.r_eyeball_joint) != null)
            {
                VOREye(aPlayer.getVNextPartBySid(Hanim.l_eyeball_joint));
                VOREye(aPlayer.getVNextPartBySid(Hanim.r_eyeball_joint));
            }
        }
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, TimePeg startPeg, TimePeg endPeg, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb) throws TMUSetupException
    {
        AnimationUnit mu;
        try
        {
            mu = createTransitionToRest();
        }
        catch (MUSetupException e)
        {
            throw new TMUSetupException("Cannot setup TMU for transition to rest ", null, e);
        }

        TimedAnimationMotionUnit tmu = mu.createTMU(fbm, bmlBlockPeg, bmlId, id, pb);
        tmu.setTimePeg("start", startPeg);
        tmu.setTimePeg("ready", endPeg);
        tmu.setState(TimedPlanUnitState.LURKING);
        return tmu;
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, double startTime, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb) throws TMUSetupException
    {
        TimePeg startPeg = new TimePeg(bmlBlockPeg);
        startPeg.setGlobalValue(startTime);
        TimePeg endPeg = new OffsetPeg(startPeg, getTransitionToRestDuration());
        return createTransitionToRest(fbm, startPeg, endPeg, bmlId, id, bmlBlockPeg, pb);
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, double startTime, double duration, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb) throws TMUSetupException
    {
        TimePeg startPeg = new TimePeg(bmlBlockPeg);
        startPeg.setGlobalValue(startTime);
        TimePeg endPeg = new OffsetPeg(startPeg, duration);
        return createTransitionToRest(fbm, startPeg, endPeg, bmlId, id, bmlBlockPeg, pb);
    }

    @Override
    public double getTransitionToRestDuration()
    {
        // FIXME: real duration calcuration here
        return 2;
    }

    @Override
    public AnimationUnit createTransitionToRest() throws MUSetupException
    {
        DynamicGazeMU mu = new DynamicGazeMU();
        mu = mu.copy(aPlayer);
        mu.setInfluence(influence);
        mu.setGazeDirection(Vec3f.getVec3f(0, 0, 1));
        return mu;
    }

    @Override
    public GazeShiftTMU createGazeShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
            throws MUSetupException
    {
        DynamicGazeMU mu = new DynamicGazeMU();
        mu.setGazeDirection(Vec3f.getVec3f(0, 0, 1));
        mu.influence = influence;
        return new GazeShiftTMU(bbf, bmlBlockPeg, bmlId, id, mu.copy(aPlayer), pb, this, aPlayer);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        throw new ParameterException("ForwardRestGaze doesn't support any parameters");
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        throw new ParameterException("ForwardRestGaze doesn't support any parameters");
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return GazeUtils.getJoints(aPlayer.getVCurr(), influence);
    }
}
