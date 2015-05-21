/*******************************************************************************
 *******************************************************************************/
package asap.motionunit;

import asap.realizer.PlayException;

/**
 * Thrown whenever a MotionUnit fails during play
 * @author Herwin van Welbergen
 */
public class MUPlayException extends PlayException
{
    private static final long serialVersionUID = 1L;
    private final MotionUnit mu;
    
    public MUPlayException(String str, MotionUnit m)
    {
        super(str);
        mu = m;        
    }
    
    public final MotionUnit getMotionUnit()
    {
        return mu;
    }
}
