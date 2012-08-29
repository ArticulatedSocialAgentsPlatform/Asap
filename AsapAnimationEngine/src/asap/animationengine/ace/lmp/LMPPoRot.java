package asap.animationengine.ace.lmp;

import hmi.animation.Hanim;

import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.ImmutableSet;

import saiba.bml.core.Behaviour;
import asap.motionunit.TMUPlayException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.TimePegAndConstraint;

@Slf4j
public class LMPPoRot extends LMP
{
    private ImmutableSet<String> kinematicJoints;
    private final String joint;

    public LMPPoRot(String scope, FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, PegBoard pegBoard)
    {
        super(fbm, bmlPeg, bmlId, behId, pegBoard);

        // TODO: implement proper scope selection when no scope is provided.
        if (scope == null)
        {
            scope = "left_arm";
        }

        if (scope.equals("left_arm"))
        {
            kinematicJoints = ImmutableSet.of(Hanim.l_wrist);
            joint = Hanim.l_wrist;
        }
        else if (scope.equals("right_arm"))
        {
            kinematicJoints = ImmutableSet.of(Hanim.r_wrist);
            joint = Hanim.r_wrist;
        }
        else
        {
            joint = null;
            log.warn("Invalid scope {}" + scope);
        }
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return kinematicJoints;
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        return ImmutableSet.of();
    }

    private double getPODurationFromAmplitude(double amp)
    {
        return (Math.abs(amp) / 140.0);
    }

    @Override
    public void updateTiming(double time) throws TMUPlayException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public double getStartTime()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getEndTime()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getRelaxTime()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasValidTiming()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        // TODO Auto-generated method stub

    }

}
