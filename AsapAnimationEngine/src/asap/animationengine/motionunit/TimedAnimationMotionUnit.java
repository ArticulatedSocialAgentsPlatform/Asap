/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.motionunit;

import java.util.List;
import java.util.Set;

import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.motionunit.MUPlayException;
import asap.motionunit.TMUPlayException;
import asap.motionunit.TimedMotionUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.Priority;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.LinearStretchResolver;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizer.scheduler.UniModalResolver;

import com.google.common.collect.Sets;

/**
 * A TimedAnimationUnit implementation that delegates the motion execution etc to an AnimationUnit
 * 
 * When you do not set an end time peg, 'UNKNOWN' is assumed. This leads to the TimedMotionUnit being timed as
 * starttime..starttime+mu.getpreferredduration() When you do not set a start time peg, the animation cannot be played
 * 
 * @author welberge
 */
public class TimedAnimationMotionUnit extends TimedMotionUnit implements TimedAnimationUnit
{
    private final AnimationUnit mu;
    private final UniModalResolver resolver = new LinearStretchResolver();
    private final AnimationPlayer aniPlayer;
    private AnimationUnit retractUnit;

    public Set<String> getKinematicJoints()
    {
        return mu.getKinematicJoints();
    }

    public Set<String> getPhysicalJoints()
    {
        return mu.getPhysicalJoints();
    };

    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        resolver.resolveSynchs(bbPeg, b, sac, this);
    }

    /**
     * Constructor
     * @param bmlBlockPeg
     * @param bmlId BML block id
     * @param id behaviour id
     * @param m motion unit
     */
    public TimedAnimationMotionUnit(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationUnit m, PegBoard pb,
            AnimationPlayer aniPlayer)
    {
        super(bbf, bmlBlockPeg, bmlId, id, m, pb);
        this.aniPlayer = aniPlayer;
        setPriority(Priority.GESTURE);
        mu = m;
    }

    public TimedAnimationMotionUnit(BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationUnit m, PegBoard pb,
            AnimationPlayer aniPlayer)
    {
        this(NullFeedbackManager.getInstance(), bmlBlockPeg, bmlId, id, m, pb, aniPlayer);
    }

    public void updateTiming(double time) throws TMUPlayException
    {

    }

    @Override
    public double getPreparationDuration()
    {
        return 0;
    }

    @Override
    public double getRetractionDuration()
    {
        return 0;
    }

    @Override
    public double getStrokeDuration()
    {
        return getPreferedDuration() - getPreparationDuration() - getRetractionDuration();
    }

    @Override
    protected void relaxUnit(double time) throws TimedPlanUnitPlayException
    {
        sendProgress(puTimeManager.getRelativeTime(time), time);
        if (!progressHandled.contains(getKeyPosition("relax")))
        {
            progressHandled.add(getKeyPosition("relax"));
            feedback("relax", time);
        }
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        
        if (retractUnit != null && time >= getRelaxTime())
        {
            try
            {
                double t = (time - getRelaxTime()) / (getEndTime() - getRelaxTime());                
                retractUnit.play(t);
            }
            catch (MUPlayException e)
            {
                throw new TimedPlanUnitPlayException(e.getMessage(), this, e);
            }
        }
        else
        {
            super.playUnit(time);
        }
    }

    @Override
    protected void gracefullInterrupt(double time) throws TimedPlanUnitPlayException
    {
        if (getTimePeg("relax") == null)
        {
            super.gracefullInterrupt(time);
            return;
        }
        Set<String> joints = Sets.union(Sets.union(mu.getKinematicJoints(), mu.getPhysicalJoints()), mu.getAdditiveJoints());
        double retractionDuration = aniPlayer.getRestPose().getTransitionToRestDuration(aniPlayer.getVCurr(), joints);
        skipPegs(time, "ready", "strokeStart", "stroke", "strokeEnd");
        getTimePeg("relax").setGlobalValue(time);
        getTimePeg("end").setGlobalValue(time + retractionDuration);
        retractUnit = aniPlayer.getRestPose().createTransitionToRest(joints);
    }
}
