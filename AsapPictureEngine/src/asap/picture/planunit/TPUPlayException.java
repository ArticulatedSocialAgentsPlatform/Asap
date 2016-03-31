/*******************************************************************************
 *******************************************************************************/
package asap.picture.planunit;

import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Thrown when playback of a TPU fails
 * @author Dennis
 *
 */
public class TPUPlayException extends TimedPlanUnitPlayException
{
    public TimedPictureUnit timedPU;
    public TPUPlayException(String str, TimedPictureUnit tpu)
    {
        super(str,tpu);
        timedPU = tpu;        
    }
    
    private static final long serialVersionUID = -6983568422653209435L;
}
