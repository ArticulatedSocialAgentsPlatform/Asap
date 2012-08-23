package asap.animationengine.ace.lmp;

import hmi.animation.Hanim;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import asap.animationengine.ace.OrientConstraint;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import lombok.extern.slf4j.Slf4j;

/**
 * Local motor program for absolute wrist rotations
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 */
public class LMPWristRot extends LMPPos
{
    private ImmutableSet<String> kinematicJoints;
    
    public LMPWristRot(String scope, List<OrientConstraint> ocVec, FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pegBoard)
    {
        super(bbf, bmlBlockPeg, bmlId, id);
        if (scope.equals("left_arm"))
        {
            kinematicJoints = ImmutableSet.of(Hanim.l_wrist);
        }
        else if (scope.equals("right_arm"))
        {
            kinematicJoints = ImmutableSet.of(Hanim.r_wrist);
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

    @Override
    public void updateTiming(double time) throws TMUPlayException
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
