package asap.motionunit;

import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.ParameterException;

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
    
    void setFloatParameterValue(String name, float value)throws ParameterException;;
    void setParameterValue(String name, String value)throws ParameterException;;
    String getParameterValue(String name)throws ParameterException;
    float getFloatParameterValue(String name)throws ParameterException;
   
}
