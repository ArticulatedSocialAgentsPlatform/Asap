/*******************************************************************************
 *******************************************************************************/
package asap.audioengine;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.BMLGestureSync;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;

/**
 * Skeleton class for the implementation of TimedAudioUnits
 * @author hvanwelbergen
 * 
 */
public abstract class TimedAbstractAudioUnit extends TimedAbstractPlanUnit
{
    private static Logger logger = LoggerFactory.getLogger(TimedAbstractAudioUnit.class.getName());

    protected InputStream inputStream;
    private double duration;

    protected double systemStartTime;
    protected double bmlStartTime;

    private TimePeg startPeg;
    private TimePeg endPeg;

    public TimedAbstractAudioUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, InputStream inputStream, String bmlId, String id)
    {
        super(bfm, bbPeg, bmlId, id);
        this.inputStream = inputStream;
    }

    /**
     * Clean up any resources associated with the TimedAbstractAudioUnit
     */
    public abstract void cleanup();
    
    /**
     * @return the startSync
     */
    public TimePeg getStartPeg()
    {
        return startPeg;
    }

    /**
     * @param startSync
     *            the startSync to set
     */
    public void setStartPeg(TimePeg startSync)
    {
        this.startPeg = startSync;
    }

    /**
     * @return the endSync
     */
    public TimePeg getEndPeg()
    {
        return endPeg;
    }

    /**
     * @param endSync
     *            the endSync to set
     */
    public void setEndPeg(TimePeg endSync)
    {
        this.endPeg = endSync;
    }

    public void setStart(TimePeg start)
    {
        startPeg = start;
    }

    public void setEnd(TimePeg end)
    {
        endPeg = end;
    }

    @Override
    public double getStartTime()
    {
        if (startPeg == null)
        {
            return TimePeg.VALUE_UNKNOWN;
        }
        return startPeg.getGlobalValue();
    }

    @Override
    public double getEndTime()
    {
        if (endPeg == null || endPeg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            double startTime = getStartTime();
            if (startTime != TimePeg.VALUE_UNKNOWN)
            {
                return startTime + getPreferedDuration();
            }
        }
        else
        {
            return endPeg.getGlobalValue();
        }
        return TimePeg.VALUE_UNKNOWN;
    }

    @Override
    public double getRelaxTime()
    {
        return getEndTime();
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        if (BMLGestureSync.isBMLSync(syncId))
        {
            if (BMLGestureSync.get(syncId).isAfter(BMLGestureSync.STROKE))
            {
                setEndPeg(peg);
            }
            else
            {
                setStartPeg(peg);
            }
        }
        else
        {
            logger.warn("Can't set TimePeg on non-BML sync {}", syncId);
        }
    }

    @Override
    public boolean hasValidTiming()
    {
        if (getStartTime() > getEndTime())
        {
            return false;
        }
        else if (Math.abs((getEndTime() - getStartTime()) - getPreferedDuration()) > 0.0001)
        {
            logger.debug("End time: {}", getEndTime());
            logger.debug("Start time: {}", getStartTime());
            logger.debug("End-start: {}", (getEndTime() - getStartTime()));
            logger.debug("Duration: {}", getPreferedDuration());
            return false;
        }

        return true;
    }

    /**
     * @return Preferred duration (in seconds) of this audio unit (call setup before calling this)
     */
    @Override
    public double getPreferedDuration()
    {
        return duration;
    }

    /**
     * load file, determine timing/duration, etc
     * 
     * @throws AudioUnitPlanningException
     */
    protected void setupCache() throws AudioUnitPlanningException
    {
    };

    /**
     * Setup the Audiounit. Calls setupCache
     * 
     * @throws AudioUnitPlanningException
     */
    public void setup() throws AudioUnitPlanningException
    {
        setupCache();
    }

    protected void sendStartProgress(double time)
    {
        logger.debug("sendStartProgress");
        String bmlId = getBMLId();
        String behaviorId = getId();

        double bmlBlockTime = time - bmlBlockPeg.getValue();
        feedback(new BMLSyncPointProgressFeedback(bmlId, behaviorId, "start", bmlBlockTime, time));
    }

    /**
     * Send progress feedback for all bookmarks passed at playTime.
     * 
     * @param playTime
     *            time since start of the audio unit
     * @param time
     *            time since start of BML execution
     */
    public abstract void sendProgress(double playTime, double time);

    /**
     * Send the end progress feedback info, should be called only from the AudioPlanPlayer.
     * 
     * @param time
     *            time since start of BML execution
     */
    public void sendEndProgress(double time)
    {
        logger.debug("sendEndProgress");
        String bmlId = getBMLId();
        String behaviorId = getId();

        double bmlBlockTime = time - bmlBlockPeg.getValue();
        feedback(new BMLSyncPointProgressFeedback(bmlId, behaviorId, "end", bmlBlockTime, time));
    }

    public void setPrefferedDuration(double duration)
    {
        this.duration = duration;
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        if (syncId.equals("start")) return startPeg;
        if (syncId.equals("end")) return endPeg;
        return null;
    }
}
