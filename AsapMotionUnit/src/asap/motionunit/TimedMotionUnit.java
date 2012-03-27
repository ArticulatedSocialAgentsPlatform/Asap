package asap.motionunit;

import java.util.List;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Delegate;
import lombok.extern.slf4j.Slf4j;

import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.PlanUnitTimeManager;
import hmi.elckerlyc.planunit.TimedAbstractPlanUnit;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.elckerlyc.planunit.ParameterNotFoundException;

import hmi.bml.feedback.BMLSyncPointProgressFeedback;

/**
 * A timedmotionunit is an abstract plan unit that delegates playback of motion to a motion unit.
 * It takes care of translating the 'real' time into time values between 0 and 1 used in this motion unit.
 * @author hvanwelbergen
 */
@Slf4j
public class TimedMotionUnit extends TimedAbstractPlanUnit
{
    public final MotionUnit mu;
    protected List<KeyPosition> progressHandled = new CopyOnWriteArrayList<KeyPosition>();
    
    @Delegate protected final PlanUnitTimeManager puTimeManager;    
    
    
    /**
     * Constructor
     * @param bmlBlockPeg
     * @param bmlId BML block id
     * @param id behaviour id
     * @param m motion unit
     */
    public TimedMotionUnit(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, MotionUnit m)
    {
        super(bbf, bmlBlockPeg, bmlId, id);
        mu = m;
        puTimeManager = new PlanUnitTimeManager(mu);    
    }
    
    public KeyPosition getKeyPosition(String kid)
    {
        return getMotionUnit().getKeyPosition(kid);
    }
    
    /**
     * Send progress feedback for all key positions passed at canonical time t.
     * 
     * @param t canonical time 0 &lt= t &lt=1
     * @param time time since start of BML execution
     */
    private void sendProgress(double t, double time)
    {
        List<BMLSyncPointProgressFeedback> fbToSend = new ArrayList<BMLSyncPointProgressFeedback>();        
        synchronized(progressHandled)
        {
            for (KeyPosition k : mu.getKeyPositions())
            {
                if (k.time <= t)
                {
                    if (!progressHandled.contains(k))
                    {
                        String bmlId = getBMLId();
                        String behaviorId = getId();
                        String syncId = k.id;
                        double bmlBlockTime = time - bmlBlockPeg.getValue();             
                        progressHandled.add(k);
                        fbToSend.add(new BMLSyncPointProgressFeedback(bmlId, behaviorId, syncId, bmlBlockTime, time));
                    }
                }
            }
        }
        for(BMLSyncPointProgressFeedback fb:fbToSend)
        {
            feedback(fb);        
        }
    }
    
    @Override
    protected void playUnit(double time) throws TMUPlayException
    {
        double t = puTimeManager.getRelativeTime(time);        
        try
        {
            log.debug("Timed Motion Unit play {}",time);
            mu.play(t);
        }
        catch (MUPlayException ex)
        {
            throw new TMUPlayException(ex.getLocalizedMessage(), this, ex);
        }
        sendProgress(t, time);
    }

    @Override
    protected void stopUnit(double time)
    {
        if (time < getEndTime())
        {
            sendProgress(puTimeManager.getRelativeTime(time), time);
        }
        else
        {
            sendProgress(1, time);
        }
    }

    /**
     * @return the encapsulated motion unit
     */
    public MotionUnit getMotionUnit()
    {
        return mu;
    }
    
    @Override
    @Deprecated
    public String getReplacementGroup()
    {
        return mu.getReplacementGroup();
    }

    @Override
    public double getPreferedDuration()
    {
        return mu.getPreferedDuration();
    }

    @Override
    public void setParameterValue(String paramId, String value) throws ParameterException
    {
        try
        {
            mu.setParameterValue(paramId, value);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }

    }

    @Override
    public void setFloatParameterValue(String paramId, float value) throws ParameterException
    {
        try
        {
            mu.setFloatParameterValue(paramId, value);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitFloatParameterNotFoundException(e);
        }
    }

    @Override
    public float getFloatParameterValue(String paramId) throws ParameterException
    {
        try
        {
            return mu.getFloatParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitFloatParameterNotFoundException(e);
        }
    }

    @Override
    public String getParameterValue(String paramId) throws ParameterException
    {
        try
        {
            return mu.getParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }
    }
    
    @Override
    public String toString()
    {
        return getBMLId()+":"+getId();
    }
}
