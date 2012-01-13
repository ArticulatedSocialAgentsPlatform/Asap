package asap.animationengine.procanimation;

import asap.animationengine.motionunit.MotionUnit;
import hmi.util.Resources;

/**
 * Motion unit with a flexible start and end, bound to another motion unit 
 * between strokeStart and strokeEnd.
 * @author Herwin
 *
 */
public interface GestureUnit extends MotionUnit
{
    /**
     * Set the resource that's used to get the stroke motion unit
     */
    void setResource(Resources r);
}
