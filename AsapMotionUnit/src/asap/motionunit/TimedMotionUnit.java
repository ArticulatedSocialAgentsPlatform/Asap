/*******************************************************************************
 *******************************************************************************/
package asap.motionunit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Delegate;
import lombok.extern.slf4j.Slf4j;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.realizer.planunit.PlanUnitTimeManager;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;

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
    protected final PegBoard pegBoard;

    @Delegate
    protected final PlanUnitTimeManager puTimeManager;

    /**
     * Constructor
     * @param bmlBlockPeg
     * @param bmlId BML block id
     * @param id behaviour id
     * @param m motion unit
     */
    public TimedMotionUnit(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, MotionUnit m, PegBoard pb)
    {
        super(bbf, bmlBlockPeg, bmlId, id);
        mu = m;
        pegBoard = pb;
        puTimeManager = new PlanUnitTimeManager(mu);
    }

    public KeyPosition getKeyPosition(String kid)
    {
        return getMotionUnit().getKeyPosition(kid);
    }

    /**
     * Send progress feedback for all key positions passed at canonical time t.
     * 
     * @param t canonical time 0 &lt;= t &lt;=1
     * @param time time since start of BML execution
     */
    protected void sendProgress(double t, double time)
    {
        List<BMLSyncPointProgressFeedback> fbToSend = new ArrayList<BMLSyncPointProgressFeedback>();
        synchronized (progressHandled)
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
        feedback(fbToSend);        
    }

    protected void sendProgress(double time)
    {
        sendProgress(puTimeManager.getRelativeTime(time), time);
    }
    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        double t = puTimeManager.getRelativeTime(time);
        try
        {
            log.debug("Timed Motion Unit play {}", time);
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
        mu.cleanup();
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        if (getTimePeg("end") == null)
        {
            setTimePeg("end", new TimePeg(getBMLBlockPeg()));
        }
        if (getEndTime() == TimePeg.VALUE_UNKNOWN && getPreferedDuration() > 0)
        {
            getTimePeg("end").setGlobalValue(time + getPreferedDuration());
        }

        try
        {
            mu.startUnit(time);
        }
        catch (MUPlayException ex)
        {
            throw new TimedPlanUnitPlayException("MUPlayException on mu startUnit", this, ex);
        }
        super.startUnit(time);
    }

    /**
     * @return the encapsulated motion unit
     */
    public MotionUnit getMotionUnit()
    {
        return mu;
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
        return getBMLId() + ":" + getId();
    }

    protected void skipPegs(double time, String... pegs)
    {
        for (String peg : pegs)
        {
            if (getTime(peg) > time)
            {
                TimePeg tp = getTimePeg(peg);
                TimePeg tpNew = tp;
                if (pegBoard.getPegKeys(tp).size() > 1)
                {
                    tpNew = new TimePeg(tp.getBmlBlockPeg());
                    pegBoard.addTimePeg(getBMLId(), getId(), peg, tpNew);
                }
                tpNew.setGlobalValue(time - 0.01);
                setTimePeg(peg, tpNew);
            }
        }
    }

    protected void gracefullInterrupt(double time) throws TimedPlanUnitPlayException
    {
        stop(time);
    }

    @Override
    public void interrupt(double time) throws TimedPlanUnitPlayException
    {
        switch (getState())
        {
        case IN_PREP:
        case PENDING:
        case LURKING:
            stop(time);
            break; // just remove yourself
        case IN_EXEC:
            gracefullInterrupt(time);
            break; // gracefully interrupt yourself
        case SUBSIDING: // nothing to be done
        case DONE:
        default:
            break;
        }
    }
}
