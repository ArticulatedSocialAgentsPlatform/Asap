package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.math.Quat4f;
import hmi.util.StringUtil;

import java.util.Set;

import saiba.bml.core.OffsetDirection;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;

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

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {
        //XXX: hack hack: if the eyeball is free, restore it to its previous rotation
        if (!kinematicJoints.contains(Hanim.r_eyeball_joint) && !kinematicJoints.contains(Hanim.l_eyeball_joint))
        {
            if (aniPlayer.getVNextPartBySid(Hanim.r_eyeball_joint) != null && aniPlayer.getVNextPartBySid(Hanim.r_eyeball_joint) != null)
            {
                float q[] = Quat4f.getQuat4f();
                aniPlayer.getVCurrPartBySid(Hanim.r_eyeball_joint).getRotation(q);
                System.out.println("Left eyeball current rotation: "+Quat4f.explainQuat4f(q));
                aniPlayer.getVNextPartBySid(Hanim.r_eyeball_joint).setRotation(q);
                aniPlayer.getVCurrPartBySid(Hanim.l_eyeball_joint).getRotation(q);
                aniPlayer.getVNextPartBySid(Hanim.l_eyeball_joint).setRotation(q);
            }
        }
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, double startTime, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, double startTime, double duration, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getTransitionToRestDuration()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public AnimationUnit createTransitionToRest()
    {
        // TODO Auto-generated method stub
        return null;
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
        return new GazeShiftTMU(bmlBlockPeg, bmlId, id, mu.copy(aniPlayer), pb, this, aniPlayer);
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, TimePeg startPeg, TimePeg endPeg, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return GazeUtils.getJoints(aniPlayer.getVCurr(), influence);
    }
}
