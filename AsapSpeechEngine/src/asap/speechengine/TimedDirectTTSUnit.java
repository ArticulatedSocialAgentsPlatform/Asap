/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import hmi.tts.TTSCallback;
import hmi.tts.TTSException;
import hmi.tts.TTSTiming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.core.Behaviour;
import saiba.bml.core.SpeechBehaviour;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.speechengine.ttsbinding.TTSBinding;

/**
 * Used to speak directly through the TTS system
 * 
 * 
 * @author welberge
 */
public class TimedDirectTTSUnit extends TimedTTSUnit
{
    private double systemStartTime;
    
    
    private static Logger logger = LoggerFactory.getLogger(TimedDirectTTSUnit.class.getName());

    public TimedDirectTTSUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String text, String bmlId, String id, TTSBinding ttsBin,
            Class<? extends Behaviour> behClass)
    {
        super(bfm, bbPeg, text, bmlId, id, ttsBin, behClass);
    }

    public TimedDirectTTSUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String text, String bmlId, String id, TTSBinding ttsBin)
    {
        super(bfm, bbPeg, text, bmlId, id, ttsBin, SpeechBehaviour.class);
    }

    public TimedDirectTTSUnit(BMLBlockPeg bbPeg, String text, String bmlId, String id, TTSBinding ttsBin)
    {
        this(NullFeedbackManager.getInstance(), bbPeg, text, bmlId, id, ttsBin);
    }

    @Override
    public void startUnit(double time) throws TimedPlanUnitPlayException
    {
        super.startUnit(time);
        logger.debug("startUnit {}", speechText);
        sendStartProgress(time);
        bmlStartTime = time;
        systemStartTime = System.nanoTime() / 1E9;
        ttsBinding.setCallback(new MyTTSCallback());
        try
        {
            ttsBinding.speak(getBehaviourClass(), speechText);
        }
        catch (TTSException e)
        {
            throw new TimedPlanUnitPlayException("TTSException", this, e);
        }
    }

    @Override
    protected TTSTiming getTiming() throws SpeechUnitPlanningException
    {
        synchronized (ttsBinding)
        {
            try
            {
                return ttsBinding.getTiming(getBehaviourClass(), speechText);
            }
            catch (TTSException e)
            {
                throw new SpeechUnitPlanningException("",this,e);
            }
        }
    }

    protected class MyTTSCallback implements TTSCallback
    {

        @Override
        public void bookmarkCallback(String bookmark)
        {
            double bmTime = System.nanoTime() / 1E9;
            String behaviorId = getId();
            String syncId = bookmark;
            double timeStamp = bmlStartTime + (bmTime - systemStartTime);
            double bmlBlockTime = timeStamp - bmlBlockPeg.getValue();
            feedback(new BMLSyncPointProgressFeedback(getBMLId(), behaviorId, syncId, bmlBlockTime, timeStamp));
        }

        @Override
        public void phonemeCallback(int phoneme, int dur, int nextPhoneme, boolean stress)
        {

        }

        @Override
        public void sentenceBoundryCallback(int offset, int length)
        {

        }

        @Override
        public boolean stopCallback()
        {
            return isDone() || isLurking();
        }

        @Override
        public void visimeCallback(int visime, int duration, int nextVis, boolean stress)
        {
            
        }

        @Override
        public void wordBoundryCallback(int offset, int length)
        {

        }
    }

    @Override
    protected void sendProgress(double playTime, double time)
    {
        // empty, bookmark feedback is handled by callbacks from the engine.
    }

    @Override
    public void setFloatParameterValue(String parameter, float value) throws ParameterException
    {
        try
        {
            ttsBinding.setFloatParameterValue(parameter, value);
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
            return ttsBinding.getFloatParameterValue(paramId);
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
            return ttsBinding.getParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }
    }

    @Override
    public void setParameterValue(String paramId, String value) throws ParameterException
    {
        try
        {
            ttsBinding.setParameterValue(paramId, value);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        if (time >= getEndTime())
        {
            sendEndProgress(time);
            synchronized (ttsBinding)
            {
                logger.debug("StopUnit {}", speechText);
            }
        }
    }

    @Override
    protected void playUnit(double arg0) throws TimedPlanUnitPlayException
    {

    }
}
