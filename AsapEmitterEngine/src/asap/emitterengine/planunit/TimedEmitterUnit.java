/*******************************************************************************
 *******************************************************************************/
package asap.emitterengine.planunit;


import java.util.ArrayList;

import lombok.Delegate;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.realizer.planunit.PlanUnitTimeManager;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * 
 * @author Dennis Reidsma
 */
public class TimedEmitterUnit extends TimedAbstractPlanUnit
{
    public final EmitterUnit eu;
    protected ArrayList<KeyPosition> progressHandled = new ArrayList<KeyPosition>();
    
    @Delegate
    private final PlanUnitTimeManager puTimeManager;

    /**
     * Constructor
     * 
     * @param bmlId
     *            BML block id
     * @param id
     *            behaviour id
     * @param e
     *            emitter unit
     */
    public TimedEmitterUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, EmitterUnit e)
    {
        super(bfm, bbPeg, bmlId, id);
        eu = e;
        puTimeManager = new PlanUnitTimeManager(e);
    }

    /**
     * Starts the PlanUnit, is only called once at start
     * 
     * @param time global start time
     * @throws TimedPlanUnitPlayException
     */
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        eu.startUnit(time);
    };

    /**
     * Gets the keyposition with id id
     */
    public KeyPosition getKeyPosition(String kid)
    {
        return eu.getKeyPosition(kid);
    }

    /**
     * Send progress feedback for all key positions passed at canonical time t.
     * 
     * @param t
     *            canonical time 0 &lt= t &lt=1
     * @param time
     *            time since start of BML execution
     */
    private void sendProgress(double t, double time)
    {
        for (KeyPosition k : eu.getKeyPositions())
        {
            if (k.time <= t)
            {
                if (!progressHandled.contains(k))
                {
                    String bmlId = getBMLId();
                    String behaviorId = getId();
                    String syncId = k.id;
                    double bmlBlockTime = time - bmlBlockPeg.getValue();
                    feedback(new BMLSyncPointProgressFeedback(bmlId, behaviorId, syncId, bmlBlockTime, time));
                    progressHandled.add(k);
                }
            }
            else
            {
                if (progressHandled.contains(k))
                {
                    progressHandled.remove(k);
                }
            }
        }
    }

    @Override
    public void playUnit(double time) throws TimedPlanUnitPlayException
    {
        double t = puTimeManager.getRelativeTime(time);
        try
        {
            eu.play(t);
        }
        catch (EUPlayException ex)
        {
            throw new TEUPlayException(ex.getLocalizedMessage(), this);
        }
        sendProgress(t, time);
    }

    public void cleanup()
    {
        eu.cleanup();
    }

    /**
     * @return the encapsulated Emitter unit
     */
    public EmitterUnit getEmitterUnit()
    {
        return eu;
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        sendProgress(1, time);
        cleanup();
    }

    @Override
    public double getPreferedDuration()
    {
        return eu.getPreferedDuration();
    }

    @Override
    public void setParameterValue(String paramId, String value) throws ParameterException
    {
        try
        {
            eu.setParameterValue(paramId, value);
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
            eu.setFloatParameterValue(paramId, value);
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
            return eu.getFloatParameterValue(paramId);
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
            return eu.getParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }
    }
}
