package hmi.elckerlyc.planunit;

import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub implementation of a TimedAbstractPlanUnit
 * @author Herwin
 */
public class StubPlanUnit extends TimedAbstractPlanUnit
{
    private final double endTime,startTime;
    private TimePeg startPeg;
    private TimePeg endPeg;
    
    public StubPlanUnit(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId,double startTime, double endTime)
    {
        super(fbm, bmlPeg, bmlId, behId);
        this.startTime = startTime;
        this.endTime = endTime;
        startPeg = new TimePeg(bmlPeg);startPeg.setGlobalValue(startTime);
        endPeg = new TimePeg(bmlPeg);endPeg.setGlobalValue(endTime);
    }

    public StubPlanUnit(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId)
    {
        this(fbm,bmlPeg,bmlId,behId,0,1);
    }
    
    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException{}
    
    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException{}
    
    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException{}
    
    @Override
    public double getEndTime(){return endTime;}
    
    @Override 
    public double getRelaxTime(){return getEndTime();}
    
    @Override
    public double getStartTime(){return 0;}

    @Override
    public boolean hasValidTiming()
    {
        return true;
    }

    @Override
    public double getTime(String syncId)
    {
        return startTime;
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        if(syncId.equals("start"))return startPeg;
        if(syncId.equals("end"))return endPeg;
        return null;
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        return new ArrayList<String>();
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
                    
    }

    @Override
    public void setParameterValue(String paramId, String value){}
    
    
    @Override
    public void setFloatParameterValue(String paramId, float value)
            throws PlanUnitFloatParameterNotFoundException
    {
    }

    @Override
    public float getFloatParameterValue(String paramId) throws PlanUnitFloatParameterNotFoundException
    {
        return 0;
    }

    @Override
    public String getParameterValue(String paramId) throws PlanUnitParameterNotFoundException
    {
        return null;
    }        
}