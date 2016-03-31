/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import java.util.HashMap;
import java.util.Map;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Skeleton implementation for TimedSpeechUnits.
 * Keeps track of speech text.
 * Provides convenience methods for start and end feedback sending.
 * @author welberge
 */
public abstract class TimedAbstractSpeechUnit extends TimedAbstractPlanUnit
{
    private TimePeg startSync;
    private TimePeg endSync;

    protected String speechText;
    protected double bmlStartTime;
    
    private Map<String, Float> subUnitFloatParameterValues = new HashMap<>();
    private Map<String, String> subUnitStringParameterValues = new HashMap<>();
    
    TimedAbstractSpeechUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String text, String bmlId, String id)
    {
        super(bfm, bbPeg, bmlId, id);
        speechText = text;
        subUnitFloatParameterValues.put("visualprosodyAmplitude", 1f);
    }

    public boolean isSubUnitParameter(String paramId)
    {
        return subUnitFloatParameterValues.containsKey(paramId)||subUnitStringParameterValues.containsKey(paramId);
    }
    
    public boolean isSubUnitFloatParameter(String paramId)
    {
        return subUnitFloatParameterValues.containsKey(paramId);
    }
    
    public void storeSubUnitParameterValue(String paramId, float value)
    {
        subUnitFloatParameterValues.put(paramId, value);
    }
    
    public void storeSubUnitParameterValue(String paramId, String value)
    {
        subUnitStringParameterValues.put(paramId, value);
    }
    
    @Override
    public float getFloatParameterValue(String paramId) throws ParameterException
    {
        if(isSubUnitFloatParameter(paramId))
        {
            return subUnitFloatParameterValues.get(paramId);
        }
        return super.getFloatParameterValue(paramId);    
    }

    @Override
    public String getParameterValue(String paramId) throws ParameterException
    {
        if(isSubUnitFloatParameter(paramId))
        {
            return ""+subUnitFloatParameterValues.get(paramId);
        }
        else if(isSubUnitParameter(paramId))
        {
            return subUnitStringParameterValues.get(paramId);
        }
        return super.getParameterValue(paramId);    
    }
    
    @Override
    public double getStartTime()
    {
        if (startSync == null)
        {
            return TimePeg.VALUE_UNKNOWN;
        }
        return startSync.getGlobalValue();
    }

    @Override
    public double getEndTime()
    {
        double endTime;
        if (endSync == null)
        {
            endTime = TimePeg.VALUE_UNKNOWN;
        }
        else
        {
            endTime = endSync.getGlobalValue();
        }        
        return endTime;
    }

    @Override
    public double getRelaxTime()
    {
        return getEndTime();
    }

    public TimePeg getEndPeg()
    {
        return endSync;
    }

    public TimePeg getStartPeg()
    {
        return startSync;
    }

    public void setStart(TimePeg s)
    {
        startSync = s;
    }

    public void setEnd(TimePeg s)
    {
        endSync = s;
    }

    protected void sendStartProgress(double time)
    {
        String bmlId = getBMLId();
        String behaviorId = getId();        
        double bmlBlockTime = time - bmlBlockPeg.getValue();
        feedback(new BMLSyncPointProgressFeedback(bmlId, behaviorId, "start", bmlBlockTime, time));
    }

    /**
     * Checks wether the TimedPlanUnit has sync sync
     */
    public boolean hasSync(String sync)
    {
        for (String s : getAvailableSyncs())
        {
            if (s.equals(sync)) return true;
        }
        return false;
    }

    /**
     * Send the end progress feedback info, should be called only from the VerbalPlanPlayer.
     * @param time time since start of BML execution
     */
    public void sendEndProgress(double time)
    {
        String bmlId = getBMLId();
        String behaviorId = getId();
        double bmlBlockTime = time - bmlBlockPeg.getValue();
        feedback(new BMLSyncPointProgressFeedback(bmlId, behaviorId, "end", bmlBlockTime, time));
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        super.startUnit(time);
        if (getEndPeg() == null)
        {
            setTimePeg("end", new TimePeg(getBMLBlockPeg()));
        }
        if (getEndTime() == TimePeg.VALUE_UNKNOWN)
        {
            getEndPeg().setGlobalValue(time+getPreferedDuration());
        }
    }
}
