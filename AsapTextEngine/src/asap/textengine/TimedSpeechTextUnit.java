/*******************************************************************************
 *******************************************************************************/
package asap.textengine;

import hmi.tts.util.BMLTextUtil;
import hmi.tts.util.SyncAndOffset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Implements a SpeechBehaviour by sending the speech text at a certain SPEECH_RATE
 * through a text output channel. The TimedTextSpeechUnit allows time warping on all its segments.
 * @author Herwin
 */
@Slf4j
public class TimedSpeechTextUnit extends TimedAbstractTextUnit
{
    private final String[] words;
    private TextOutput output;
    private final Map<String, Integer> syncMap = new HashMap<String, Integer>(); // syncId
                                                                                 // =>
                                                                                 // nr
                                                                                 // of
                                                                                 // the
                                                                                 // word
                                                                                 // after
                                                                                 // the
                                                                                 // sync
    private Map<String, TimePeg> pegs = new HashMap<String, TimePeg>(); // syncName
                                                                        // =>
                                                                        // TimePeg

    private static final double SPEECH_RATE = 2;
    private List<String> progressHandled = new ArrayList<>();
    private List<String> syncs = new ArrayList<>();

    /**
     * @return the syncs
     */
    public List<String> getAvailableSyncs()
    {
        return syncs;
    }

    @Override
    public TimePeg getTimePeg(String s)
    {
        return pegs.get(s);
    }

    @Override
    public double getTime(String s)
    {
        TimePeg p = pegs.get(s);
        if (p == null) return TimePeg.VALUE_UNKNOWN;
        return p.getGlobalValue();
    }

    @Override
    public void setTimePeg(String sync, TimePeg sp)
    {
        if (syncs.contains(sync))
        {
            pegs.put(sync, sp);
            log.debug("Adding sync {}", sync);
        }
        else
        {
            log.warn("Can't set TimePeg for sync {}, only setting " + syncs + "is allowed", sync);
        }
        
        if (sync.equals("start"))
        {
            setStart(sp);
        }
        else if (sync.equals("end"))
        {
            setEnd(sp);
        }        
    }

    public TimedSpeechTextUnit(BMLBlockPeg bbPeg, String text, String bmlId, String id, TextOutput output)
    {
        this(NullFeedbackManager.getInstance(), bbPeg, text, bmlId, id, output);
    }

    public TimedSpeechTextUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String text, String bmlId, String id, TextOutput output)
    {
        super(bfm, bbPeg, text, bmlId, id);

        syncs.add("start");
        syncMap.put("start", 0);

        log.debug("text: {}", text);

        String textNoSync = BMLTextUtil.stripSyncs(text);
        words = textNoSync.split(" ");
        List<SyncAndOffset> syncAndOffsets = BMLTextUtil.getSyncAndOffsetList(text, words.length);

        for (SyncAndOffset s : syncAndOffsets)
        {
            syncs.add(s.getSync());
            syncMap.put(s.getSync(), s.getOffset());
        }

        this.output = output;

        speechText = textNoSync;

        syncs.add("end");
        syncMap.put("end", words.length);

        log.debug("speechText: {}", speechText);
    }

    @Override
    public double getPreferedDuration()
    {
        return words.length * (1.0 / SPEECH_RATE);
    }

    @Override
    public boolean hasValidTiming()
    {
        double prevTime = 0;
        for (String sync : syncs)
        {
            TimePeg p = pegs.get(sync);
            if (p != null && p.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                log.debug("Checking time of {}: {}", sync, p.getGlobalValue());

                if (p.getGlobalValue() < prevTime)
                {
                    return false;
                }
                else
                {
                    prevTime = p.getGlobalValue();
                }
            }
        }
        return true;
    }

    @Override
    public void setFloatParameterValue(String parameter, float value) throws ParameterException
    {
        try
        {
            output.setFloatParameterValue(parameter, value);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitFloatParameterNotFoundException(e);
        }
    }

    @Override
    public void setParameterValue(String paramId, String value) throws ParameterException
    {
        try
        {
            output.setParameterValue(paramId, value);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }
    }

    @Override
    public void stopUnit(double time)
    {
        sendProgress(getRelativeTime(time), time);
        if (time >= getEndTime())
        {
            sendEndProgress(time);
        }
        progressHandled.clear();
        output.setText(speechText);
    }

    public double getRelativeTime(double time)
    {
        double startSegmentTime = getStartTime();
        double startKeyTime = 0;
        double endSegmentTime = getEndTime();
        double endKeyTime = 1;

        for (String sync : syncs)
        {
            TimePeg s = pegs.get(sync);
            if (s != null && s.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
            {
                // find first keypos
                if (s.getGlobalValue() <= time)
                {
                    startSegmentTime = s.getGlobalValue();
                    startKeyTime = getRelativeTime(sync);
                }
                // find end keypos
                else
                {
                    endSegmentTime = s.getGlobalValue();
                    endKeyTime = getRelativeTime(sync);
                    break;
                }
            }
        }

        if (endSegmentTime == TimePeg.VALUE_UNKNOWN)
        {
            endSegmentTime = startSegmentTime;
            endKeyTime = startKeyTime;
        }

        // local timewarp
        double realDuration = endSegmentTime - startSegmentTime;
        double canDuration = endKeyTime - startKeyTime;
        double t = ((time - startSegmentTime) / realDuration) * canDuration + startKeyTime;
        if (t > 1) t = 1;
        return t;
    }

    /**
     * Assumes that sync is a valid synchronization point (could also be
     * start/end)
     */
    @Override
    public double getRelativeTime(String sync)
    {
        return (double) syncMap.get(sync) / (double) words.length;
    }

    @Override
    public void playUnit(double time)
    {
        double t = getRelativeTime(time);
        String str = speechText.substring(0, (int) Math.round((t * (speechText.length()))));
        output.setText(str);

        sendProgress(t, time);
    }

    // @Override
    public void sendProgress(double playTime, double time)
    {
        for (String sync : syncMap.keySet())
        {
            double t = getRelativeTime(sync);
            if (playTime > t)
            {
                if (!progressHandled.contains(sync))
                {
                    String bmlId = getBMLId();
                    String behaviorId = getId();
                    double bmlBlockTime = time - bmlBlockPeg.getValue();
                    feedback(new BMLSyncPointProgressFeedback(bmlId, behaviorId, sync, bmlBlockTime, time));
                    progressHandled.add(sync);
                }
            }
        }
    }

    @Override
    protected void sendStartProgress(double time)
    {
        super.sendStartProgress(time);
        progressHandled.add("start");        
    }

    @Override
    public void sendEndProgress(double time)
    {
        super.sendEndProgress(time);
        progressHandled.add("end");
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        sendStartProgress(time);
    }

    @Override
    public float getFloatParameterValue(String paramId) throws ParameterException
    {
        try
        {
            return output.getFloatParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw this.wrapIntoPlanUnitFloatParameterNotFoundException(e);
        }
    }

    @Override
    public String getParameterValue(String paramId) throws ParameterException
    {
        try
        {
            return output.getParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw this.wrapIntoPlanUnitParameterNotFoundException(e);
        }
    }
}
