/*******************************************************************************
 *******************************************************************************/
package asap.picture.planunit;


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
public class TimedPictureUnit extends TimedAbstractPlanUnit
{
    public final PictureUnit pu;
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
     * @param pu
     *            picture unit
     */
    public TimedPictureUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, PictureUnit pu)
    {
        super(bfm, bbPeg, bmlId, id);
        this.pu = pu;
        puTimeManager = new PlanUnitTimeManager(pu);
    }

    /**
     * Starts the PlanUnit, is only called once at start
     * 
     * @param time global start time
     * @throws TimedPlanUnitPlayException
     */
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        try {
            pu.startUnit(time);
        } catch (PUPlayException ex) {
            TimedPlanUnitPlayException e = new TimedPlanUnitPlayException(ex.getMessage(), this);
            e.initCause(ex);
            throw e;
        }
    };


    /**
     * Gets the keyposition with id id
     */
    public KeyPosition getKeyPosition(String kid)
    {
        return pu.getKeyPosition(kid);
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
        for (KeyPosition k : pu.getKeyPositions())
        {
            if (k.time <= t)
            {
                if (!progressHandled.contains(k))
                {
                    String bmlId = getBMLId();
                    String behaviorId = getId();
                    String syncId = k.id;
                    //String id = "fb-" + bmlId + ":" + behaviorId + ":" + syncId;
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
            pu.play(t);
        }
        catch (PUPlayException ex)
        {
            throw new TPUPlayException(ex.getLocalizedMessage(), this);
        }
        sendProgress(t, time);
    }

    public void cleanup()
    {
        pu.cleanup();
    }

    /**
     * @return the encapsulated Picture unit
     */
    public PictureUnit getPictureUnit()
    {
        return pu;
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
        return pu.getPreferedDuration();
    }

    @Override
    public void setParameterValue(String paramId, String value) throws ParameterException
    {
        try
        {
            pu.setParameterValue(paramId, value);
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
            pu.setFloatParameterValue(paramId, value);
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
            return pu.getFloatParameterValue(paramId);
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
            return pu.getParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }
    }
}
