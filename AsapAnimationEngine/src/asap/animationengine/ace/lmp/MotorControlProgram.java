package asap.animationengine.ace.lmp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;

public class MotorControlProgram extends TimedAbstractPlanUnit implements TimedAnimationUnit
{
    private List<TimedAnimationUnit> lmpQueue = new ArrayList<>();
    private TimePeg startPeg, endPeg, relaxPeg;
    private final PegBoard globalPegBoard;
    private final PegBoard localPegBoard;
    
    public MotorControlProgram(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, PegBoard pegBoard)
    {
        super(fbm, bmlPeg, bmlId, behId);
        startPeg = new TimePeg(bmlPeg);
        endPeg = new TimePeg(bmlPeg);
        relaxPeg = new TimePeg(bmlPeg);
        globalPegBoard = pegBoard;
        localPegBoard = new PegBoard();
    }

    public void addLMP(TimedAnimationUnit tau)
    {
        lmpQueue.add(tau);
    }
    
    @Override
    public double getStartTime()
    {
        return startPeg.getGlobalValue();
    }

    @Override
    public double getEndTime()
    {
        return endPeg.getGlobalValue();
    }

    @Override
    public double getRelaxTime()
    {
        if(relaxPeg.getGlobalValue()!=TimePeg.VALUE_UNKNOWN)
        {
            return relaxPeg.getGlobalValue();
        }
        return getEndTime();
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        return localPegBoard.getTimePeg(getBMLId(), getId(), syncId);
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
    public Set<String> getKinematicJoints()
    {
        Set<String> kinJoints = new HashSet<>();
        for(TimedAnimationUnit tmu: lmpQueue)
        {
            kinJoints.addAll(tmu.getKinematicJoints());
        }
        return ImmutableSet.copyOf(kinJoints);
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        Set<String> phJoints = new HashSet<>();
        for(TimedAnimationUnit tmu: lmpQueue)
        {
            phJoints.addAll(tmu.getPhysicalJoints());
        }
        return ImmutableSet.copyOf(phJoints);
    }

    @Override
    public void updateTiming(double time) throws TMUPlayException
    {
        // TODO Auto-generated method stub
        
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
