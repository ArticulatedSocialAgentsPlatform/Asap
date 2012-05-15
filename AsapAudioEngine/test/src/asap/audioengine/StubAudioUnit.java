package asap.audioengine;


import java.io.InputStream;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.PlanUnitFloatParameterNotFoundException;
import asap.realizer.planunit.PlanUnitParameterNotFoundException;
import asap.realizer.planunit.TimedPlanUnitPlayException;

class StubAudioUnit extends TimedAbstractAudioUnit
{
    public StubAudioUnit(FeedbackManager bfm,BMLBlockPeg bbPeg, InputStream is, String id,
            String bmlId)
    {
        super(bfm,bbPeg, is, bmlId, id);
    }

    @Override
    public void sendProgress(double playTime, double time){}

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException{}
    
    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException{}
    
    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException{}
    
    @Override
    public double getPreferedDuration(){return 5;}

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
        return "";
    }

    @Override
    public void cleanup()
    {
                
    }        
}