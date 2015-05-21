/*******************************************************************************
 *******************************************************************************/
package asap.audioengine;

import hmi.audioenvironment.Wav;
import hmi.audioenvironment.WavPlayException;
import hmi.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
/**
 * Interface for the playback of (wav) audio units
 * @author Herwin van Welbergen
 * @author Dennis Reidsma
 *
 */
public class WavUnitImpl implements WavUnit
{


    private static Logger logger = LoggerFactory.getLogger(WavUnitImpl.class.getName());
    
    Wav theWav = null;

    public WavUnitImpl(Wav w)
    {
        theWav = w;
    }
    
    @Override
    public void setParameterValue(String parameter, float value) throws ParameterNotFoundException
    {
        logger.debug("Setting WavClipUnitParameter {} to {}", parameter, value);
        if (parameter.equals("volume"))
        {
            theWav.setVolume(value);
        }
        else
        {
            throw new ParameterNotFoundException(parameter);
        }
    }

    public void setParameterValue(String parameter, String value) throws ParameterException
    {
        if (StringUtil.isNumeric(value))
        {
            setParameterValue(parameter, Float.parseFloat(value));
        }
        else
        {
            throw new InvalidParameterException(parameter,value);
        }
    }

    @Override
    public String getParameterValue(String parameter) throws ParameterException
    {
        return "" + getFloatParameterValue(parameter);
    }

    @Override
    public float getFloatParameterValue(String parameter) throws ParameterException
    {
        if (parameter.equals("volume"))
        {
            return theWav.getVolume();
        }
        else
        {
            throw new ParameterNotFoundException(parameter);
        }
    }    
    /**
     * @param relTime time relative to the start of the WavUnit
     */
    public void start(double relTime)
    {
        theWav.start(relTime);
    }
    
    /**
     * Stops and cleans up the WavUnit
     */
    public void stop()
    {
        theWav.stop();
    }
    
    /**
     * Play
     * 
     * @param relTime relative to start of WavUnit
     * @throws WavUnitPlayException
     */
    public void play(double relTime) throws WavUnitPlayException
    {
        try 
        {
            theWav.play(relTime);
        }
        catch (WavPlayException ex)    
        {
            throw new WavUnitPlayException("Error playing WavUnit "+ex.getLocalizedMessage(), this, ex);
        }
    }
    
    /**
     * Get the duration of the WavUnit in seconds
     * @return
     */
    public double getDuration()
    {
        return theWav.getDuration();
    }
}
