package asap.animationengine.ace.lmp;

import java.util.Set;

import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;

public class MotorControlProgram extends TimedAbstractPlanUnit implements TimedAnimationUnit
{

    public MotorControlProgram(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId)
    {
        super(fbm, bmlPeg, bmlId, behId);
        // TODO Auto-generated constructor stub
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
    public Set<String> getKinematicJoints()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        // TODO Auto-generated method stub
        return null;
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
