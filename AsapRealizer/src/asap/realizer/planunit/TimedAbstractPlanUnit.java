/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * Skeleton implementation of TimedPlanUnit Keeps track of the TimedPlanUnit id information, BMLBlockPeg, feedback listener registration, provides a
 * feedback sending method and keeps track of TimedPlanUnit state.
 * 
 * @author welberge
 */
@Slf4j
public abstract class TimedAbstractPlanUnit implements TimedPlanUnit
{
    protected final BMLBlockPeg bmlBlockPeg;
    private boolean subUnit;
    private final String id;
    private final String bmlBlockId;
    
    private final AtomicReference<TimedPlanUnitState> state;
    protected final FeedbackManager fbManager;
    private int priority = 0;
    
    public TimedAbstractPlanUnit(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId, boolean sub)
    {
        fbManager = fbm;
        bmlBlockPeg = bmlPeg;
        id = behId;
        bmlBlockId = bmlId;
        state = new AtomicReference<TimedPlanUnitState>(TimedPlanUnitState.IN_PREP);
        subUnit = sub;    
    }
    
    public void linkSynchs(List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            for (String syncId : getAvailableSyncs())
            {
                if (s.syncId.equals(syncId))
                {
                    if (s.offset == 0)
                    {
                        setTimePeg(syncId, s.peg);
                    }
                    else
                    {
                        setTimePeg(syncId, new OffsetPeg(s.peg, -s.offset));
                    }
                }
            }
        }
    }
    
    @Override
    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }
    
    public TimedAbstractPlanUnit(FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId)
    {
        this(fbm, bmlPeg, bmlId, behId, false);
    }

    public void setSubUnit(boolean sub)
    {
        subUnit = sub;
    }

    protected PlanUnitFloatParameterNotFoundException wrapIntoPlanUnitFloatParameterNotFoundException(ParameterNotFoundException ex)
    {
        PlanUnitFloatParameterNotFoundException pup = new PlanUnitFloatParameterNotFoundException(getBMLId(), getId(), ex.getParamId());
        pup.initCause(ex);
        return pup;
    }

    protected PlanUnitParameterNotFoundException wrapIntoPlanUnitParameterNotFoundException(ParameterNotFoundException ex)
    {
        PlanUnitParameterNotFoundException pup = new PlanUnitParameterNotFoundException(getBMLId(), getId(), ex.getParamId());
        pup.initCause(ex);
        return pup;
    }

    protected abstract void playUnit(double time) throws TimedPlanUnitPlayException;

    protected abstract void stopUnit(double time) throws TimedPlanUnitPlayException;

    

    /**
     * Starts the PlanUnit, is only called once at start
     * 
     * @param time global start time
     * @throws TimedPlanUnitPlayException
     */
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
    }

    /**
     * Relaxes the planunit
     * @param time global relax time
     * @throws TimedPlanUnitPlayException
     */
    protected void relaxUnit(double time) throws TimedPlanUnitPlayException
    {
        
    }
    
    @Override
    public boolean isSubUnit()
    {
        return subUnit;
    }

    @Override
    public void updateTiming(double time) throws TimedPlanUnitPlayException
    {
        
    }
    
    @Override
    public final void play(double time) throws TimedPlanUnitPlayException
    {
        if (!isPlaying())
            return;
        log.debug("playing planunit {}:{}, {}", new Object[]{bmlBlockId, id, getClass().getCanonicalName()});

        if (time < getStartTime() || getStartTime() == TimePeg.VALUE_UNKNOWN)
        {
            if (!isSubUnit())
            {
                throw new TimedPlanUnitPlayException("Calling play with time :" + time + "< startTime(" + getStartTime() + ").", this);
            }
            else
            {
            	log.warn("Calling sub unit of {}:{} play with time : {} < startTime({}).", new Object[]{getBMLId(),getId(),time, getStartTime()});
            }
        }
        
        if(getRelaxTime()!=TimePeg.VALUE_UNKNOWN)
        {
            if(time>=getRelaxTime() && getState() == TimedPlanUnitState.IN_EXEC)
            {
                setState(TimedPlanUnitState.SUBSIDING);
                relaxUnit(time);
            }
        }
        
        if (time < getEndTime() || getEndTime() == TimePeg.VALUE_UNKNOWN)
        {
        	log.debug("Entering playUnit {}:{}", bmlBlockId, id);            
            playUnit(time);
        }
        else
        {
            stop(time);
        }        
    }

    /**
     * Standard implementation, just stops the timedplanunit
     * @throws TimedPlanUnitPlayException 
     */
    public void interrupt(double time) throws TimedPlanUnitPlayException
    {
        stop(time);
    }
    
    @Override
    public final void stop(double time) throws TimedPlanUnitPlayException
    {
    	log.debug("Entering TimedAbstractPlanUnit {}:{} stop",this.getBMLId(),this.getBMLId());
        if (isPlaying())
        {
        	log.debug("TimedAbstractPlanUnit stop");            
            stopUnit(time);
            log.debug("TimedAbstractPlanUnit stopUnit done");
        }
        setState(TimedPlanUnitState.DONE);
    }

    @Override
    public final void start(double time) throws TimedPlanUnitPlayException
    {
    	log.debug("attempting to start planunit {}:{}", bmlBlockId, id);
        if (!isLurking())
            return;

        if (time > getEndTime() && getEndTime() != TimePeg.VALUE_UNKNOWN)
        {
            setState(TimedPlanUnitState.DONE);
            if (!isSubUnit())
            {
                throw new TimedPlanUnitPlayException("Starting behaviour " + getClass().getName() + " with id " + bmlBlockId + ":" + id + " at time "
                        + time + " past end time " + getEndTime() + ", behaviour was never executed.", this);
            }
            else
            {
            	log.warn("Starting sub plan unit behaviour {} with id {}:{} at time {} past end time {}, behaviour was never executed. Start time: {}.",
                        new Object[] { getClass().getName(), bmlBlockId, id, time, getEndTime(), getStartTime()});
            }
        }
        else
        {
            startUnit(time);
            setState(TimedPlanUnitState.IN_EXEC);            
        }
        log.debug("started planunit {}:{}", bmlBlockId, id);
    }

    @Override
    public BMLBlockPeg getBMLBlockPeg()
    {
        return bmlBlockPeg;
    }

    @Override
    public String getBMLId()
    {
        return bmlBlockId;
    }

    @Override
    public String getId()
    {
        return id;
    }

    /**
     * Send a list of feedback (in list-order) to the BMLFeedbackListeners. The listeners will receive all feedbacks in the list before any subsequent
     * feedback send using the feedback functions.
     * 
     * @param fbs
     */
    public void feedback(List<BMLSyncPointProgressFeedback> fbs)
    {
        fbManager.feedback(fbs);        
    }

    @Override
    public void feedback(BMLSyncPointProgressFeedback fb)
    {
        fbManager.feedback(fb);        
    }    
    
    protected void feedback(String sync, double time)
    {
        feedback(new BMLSyncPointProgressFeedback(getBMLId(), getId(), sync, time - bmlBlockPeg.getValue(), time));
    }
    
    @Override
    public void setState(TimedPlanUnitState newState)
    {
        state.set(newState);
    }

    @Override
    public TimedPlanUnitState getState()
    {
        return state.get();
    }

    @Override
    public boolean isPlaying()
    {
        return state.get().isPlaying();
    }
    
    @Override
    public boolean isPending()
    {
        return state.get().isPending();
    }
    
    @Override
    public boolean isLurking()
    {
        return state.get().isLurking();
    }
    
    public boolean isInExec()
    {
        return state.get().isInExec();
    }

    @Override
    public boolean isDone()
    {
        return state.get().isDone();
    }
    
    @Override
    public boolean isSubsiding()
    {
        return state.get().isSubsiding();
    }
    
    @Override
    public boolean isInPrep()
    {
        return state.get().isInPrep();
    }

    @Override
    public double getPreferedDuration()
    {
        return 0;
    }

    @Override
    public double getRelativeTime(String syncId) throws SyncPointNotFoundException
    {
        if (syncId.equals("start"))
            return 0;
        if (syncId.equals("end"))
            return 1;
        throw new SyncPointNotFoundException(getBMLId(), getId(), syncId);
    }

    @Override
    public double getTime(String syncId)
    {
        if(getTimePeg(syncId)!=null)
        {
            return getTimePeg(syncId).getGlobalValue();
        }
        return TimePeg.VALUE_UNKNOWN;
    }

   

    @Override
    public void setParameterValue(String paramId, String value) throws ParameterException
    {
        throw new PlanUnitParameterNotFoundException(getBMLId(), getId(), paramId);
    }

    @Override
    public void setFloatParameterValue(String paramId, float value) throws ParameterException
    {
        throw new PlanUnitFloatParameterNotFoundException(getBMLId(), getId(), paramId);
    }

    @Override
    public float getFloatParameterValue(String paramId) throws ParameterException
    {
        throw new PlanUnitFloatParameterNotFoundException(getBMLId(), getId(), paramId);
    }

    @Override
    public String getParameterValue(String paramId) throws ParameterException
    {
        throw new PlanUnitParameterNotFoundException(getBMLId(), getId(), paramId);
    }
}
