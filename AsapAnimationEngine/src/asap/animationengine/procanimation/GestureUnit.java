/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import hmi.util.Resources;
import asap.animationengine.motionunit.AnimationUnit;

/**
 * Motion unit with a flexible start and end, bound to another motion unit 
 * between strokeStart and strokeEnd.
 * @author Herwin
 *
 */
public interface GestureUnit extends AnimationUnit
{
    /**
     * Set the resource that's used to get the stroke motion unit
     */
    void setResource(Resources r);
}
