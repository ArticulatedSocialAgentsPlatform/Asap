/*******************************************************************************
 *******************************************************************************/
package asap.audioengine;

import asap.realizer.planunit.ParameterException;

/**
 * Interface for the playback of (wav) audio units
 * @author hvanwelbergen
 *
 */
public interface WavUnit
{
    void setParameterValue(String parameter, float value)throws ParameterException;
    void setParameterValue(String parameter, String value)throws ParameterException;
    String getParameterValue(String parameter)throws ParameterException;
    float getFloatParameterValue(String parameter)throws ParameterException;
    
    /**
     * @param relTime time relative to the start of the WavUnit
     */
    void start(double relTime);
    
    /**
     * Stops and cleans up the WavUnit
     */
    void stop();
    
    /**
     * Play
     * @param relTime relative to start of WavUnit
     */
    void play(double relTime) throws WavUnitPlayException;
    
    /**
     * Get the duration of the WavUnit in seconds
     * @return
     */
    double getDuration();
}
