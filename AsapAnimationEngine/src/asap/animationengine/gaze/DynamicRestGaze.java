/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.EyeSaturation;
import hmi.neurophysics.ListingsLaw;
import hmi.util.StringUtil;
import hmi.worldobjectenvironment.WorldObject;

import java.util.Set;

import saiba.bml.core.OffsetDirection;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Dynamically keeps the gaze on target. Creates transitions that are also dynamic.
 * @author harsens
 * 
 */
public class DynamicRestGaze implements RestGaze
{
    private AnimationPlayer aniPlayer;
    private String target;
    private GazeInfluence influence;
    private OffsetDirection offsetDirection = OffsetDirection.NONE;
    private double offsetAngle = 0;

    public DynamicRestGaze()
    {

    }

    @Override
    public DynamicRestGaze copy(AnimationPlayer player)
    {
        DynamicRestGaze copy = new DynamicRestGaze();
        copy.influence = influence;
        copy.target = target;
        copy.offsetDirection = offsetDirection;
        copy.offsetAngle = offsetAngle;
        copy.setAnimationPlayer(player);
        return copy;
    }

    @Override
    public void setAnimationPlayer(AnimationPlayer player)
    {
        aniPlayer = player;

    }

    private void VOREye(VJoint eye)
    {
        WorldObject woTarget = aniPlayer.getWoManager().getWorldObject(target);
        float gazeDir[] = Vec3f.getVec3f();
        woTarget.getTranslation2(gazeDir, eye);
        Quat4f.transformVec3f(GazeUtils.getOffsetRotation(offsetDirection, offsetAngle), gazeDir);
        Vec3f.normalize(gazeDir);
        float qp[] = Quat4f.getQuat4f();
        ListingsLaw.listingsEye(gazeDir, qp);
        float q[] = Quat4f.getQuat4f();
        EyeSaturation.sat(qp, Quat4f.getIdentity(), q);
        eye.setRotation(q);
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {
        if (!kinematicJoints.contains(Hanim.r_eyeball_joint) && !kinematicJoints.contains(Hanim.l_eyeball_joint))
        {
            if (aniPlayer.getVNextPartBySid(Hanim.r_eyeball_joint) != null && aniPlayer.getVNextPartBySid(Hanim.r_eyeball_joint) != null)
            {
                VOREye(aniPlayer.getVNextPartBySid(Hanim.l_eyeball_joint));
                VOREye(aniPlayer.getVNextPartBySid(Hanim.r_eyeball_joint));                
            }
        }
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
        try
        {
            DynamicGazeMU mu = createTransitionToRest();
            mu.setStartPose();
            return mu.getPreferedReadyDuration();
        }
        catch (MUSetupException e)
        {
            throw new RuntimeException(e);
        }
        catch (MUPlayException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DynamicGazeMU createTransitionToRest() throws MUSetupException
    {
        DynamicGazeMU mu = new DynamicGazeMU();
        mu.target = target;
        mu.offsetDirection = offsetDirection;
        mu.influence = influence;
        mu.offsetAngle = offsetAngle;
        mu = mu.copy(aniPlayer);
        return mu;
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("target"))
        {
            target = value;
        }
        else if (name.equals("offsetdirection"))
        {
            offsetDirection = OffsetDirection.valueOf(value);
        }
        else if (name.equals("influence"))
        {
            influence = GazeInfluence.valueOf(value);
        }
        else if (StringUtil.isNumeric(value))
        {
            setFloatParameterValue(name, Float.parseFloat(value));
        }
        else throw new InvalidParameterException(name, value);
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterNotFoundException
    {
        if (name.equals("offsetangle"))
        {
            offsetAngle = value;
        }
        else
        {
            throw new ParameterNotFoundException(name);
        }
    }

    @Override
    public GazeShiftTMU createGazeShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
            throws MUSetupException
    {
        DynamicGazeMU mu = new DynamicGazeMU();
        mu.target = target;
        mu.offsetDirection = offsetDirection;
        mu.influence = influence;
        mu.offsetAngle = offsetAngle;
        return new GazeShiftTMU(bbf, bmlBlockPeg, bmlId, id, mu.copy(aniPlayer), pb, this, aniPlayer);
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
    public Set<String> getKinematicJoints()
    {
        return GazeUtils.getJoints(aniPlayer.getVCurr(), influence);
    }
}
