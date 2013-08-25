package asap.speechengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;

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

    private static Logger logger = LoggerFactory.getLogger(TimedAbstractSpeechUnit.class.getName());

    TimedAbstractSpeechUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String text, String bmlId, String id)
    {
        super(bfm, bbPeg, bmlId, id);
        speechText = text;
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
        if (endTime == TimePeg.VALUE_UNKNOWN)
        {
            double startTime = getStartTime();
            if (startTime != TimePeg.VALUE_UNKNOWN)
            {
                return startTime + getPreferedDuration();
            }
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
        logger.debug("Sending start progress feedback.");
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
