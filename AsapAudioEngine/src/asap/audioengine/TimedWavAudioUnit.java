package asap.audioengine;


import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.elckerlyc.planunit.ParameterNotFoundException;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;

import hmi.audioenvironment.*;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plans audio by caching a .wav file, then playing it
 */
public class TimedWavAudioUnit extends TimedAbstractAudioUnit
{
    private static Logger logger = LoggerFactory
            .getLogger(TimedWavAudioUnit.class.getName());
    protected WavUnit wavUnit;
    private final SoundManager soundManager;
    
    public TimedWavAudioUnit(SoundManager soundManager,FeedbackManager bfm, BMLBlockPeg bbPeg, InputStream inputStream,
            String bmlId, String id)
    {
        super(bfm,bbPeg, inputStream, bmlId, id);
        this.soundManager = soundManager;
    }

    @Override
    public void sendProgress(double playTime, double time)
    {

    }

    @Override
    public void playUnit(double time) throws TimedPlanUnitPlayException
    {
        double playTime = time - getStartTime();
        try
        {
            wavUnit.play(playTime);
        }
        catch (WavUnitPlayException e)
        {
            throw new TimedAudioUnitPlayException(e.getLocalizedMessage(), this, e);            
        }

        // Send bookmark progress
        sendProgress(playTime, time);
    }

    @Override
    public void stopUnit(double time)
    {
        sendEndProgress(time);
        if (wavUnit != null)
        {
            wavUnit.stop();
        }
    }

    @Override
    public void cleanup()
    {
        if (wavUnit != null)
        {
            wavUnit.stop();
        }
    }
    
    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        bmlStartTime = time;
        systemStartTime = System.nanoTime() / 1E9;        
        if (wavUnit != null)
        {
            wavUnit.start(time - getStartTime());
        }
        else
        {
            throw new TimedAudioUnitPlayException("Null wavUnit unit", this);
        }
        sendStartProgress(time);
    }

    /**
     * load file, determine timing/duration, etc
     * 
     * @throws AudioUnitPlanningException
     */
    @Override
    protected void setupCache() throws AudioUnitPlanningException
    {
        try
        {
            wavUnit = new WavUnitImpl(soundManager.createWav(inputStream));
        }
        catch (WavCreationException e)
        {
            throw new AudioUnitPlanningException(e.getLocalizedMessage(), this, e);            
        }
        setPrefferedDuration(wavUnit.getDuration());
        logger.debug("WAVUNIT DURATION: {}", wavUnit.getDuration());
    }

    @Override
    public void setFloatParameterValue(String paramId, float value)
            throws ParameterException
    {
        logger.debug("Setting wav parameter {}, value: {}", paramId, value);
        try
        {
            wavUnit.setParameterValue(paramId, value);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitFloatParameterNotFoundException(e);
        }
    }

    @Override
    public void setParameterValue(String paramId, String value)
            throws ParameterException
    {
        try
        {
            wavUnit.setParameterValue(paramId, value);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }
    }

    @Override
    public float getFloatParameterValue(String paramId)
            throws ParameterException
    {
        try
        {
            return wavUnit.getFloatParameterValue(paramId);
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
            return wavUnit.getParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }
    }
}
