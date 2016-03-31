/*******************************************************************************
 *******************************************************************************/
package asap.motionunit;

import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Thrown when playback of a TMU fails
 * @author Herwin
 *
 */
public class TMUPlayException extends TimedPlanUnitPlayException
{
    public final TimedMotionUnit timedMU;
    
    public TMUPlayException(String str, TimedMotionUnit tmu, Exception ex)
    {
        this(str, tmu);
        initCause(ex);        
    }
    
    public TMUPlayException(String str, TimedMotionUnit tmu)
    {
        super(str, tmu);
        timedMU = tmu;        
    }
    
    private static final long serialVersionUID = -6983568422653209455L;
}
