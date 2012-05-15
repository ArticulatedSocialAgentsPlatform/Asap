package asap.motionunit;

import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.ParameterException;

/**
 * A unit of playable motion 
 * @author hvanwelbergen
 *
 */
public interface MotionUnit extends KeyPositionManager
{
    /**
     * Executes the motion unit, typically by rotating some VJoints
     * @param t execution time, 0 &lt t &lt 1
     * @throws MUPlayException if the play fails for some reason
     */
    void play(double t)throws MUPlayException;    
    
    /**
     * Prepares the motion unit for start     
     */
    void startUnit(double t)throws MUPlayException;
    
    /**
     * Get the faceunit replacement group (=typically the BML behavior)
     * Used to determine the currently active persistent TFU for this group in the player
     * Only one group is active at a time
     * @deprecated will no longer be relevant in BML 1.0
     */
    @Deprecated
    String getReplacementGroup();   
    
    /**
     * @return Prefered duration (in seconds) of this face unit, 0 means not determined/infinite 
     */
    double getPreferedDuration();
        
    void setFloatParameterValue(String name, float value)throws ParameterException;;
    void setParameterValue(String name, String value)throws ParameterException;;
    String getParameterValue(String name)throws ParameterException;
    float getFloatParameterValue(String name)throws ParameterException;
   
}
