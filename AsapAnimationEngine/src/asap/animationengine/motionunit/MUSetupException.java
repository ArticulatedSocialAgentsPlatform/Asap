/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.motionunit;

/**
 * Thrown when a motion unit cannot be created/setup
 * @author Herwin
 *
 */
public class MUSetupException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final AnimationUnit mu;
    
    public MUSetupException(String str, AnimationUnit m)
    {
        super(str);
        mu = m;        
    }
    
    public final AnimationUnit getMotionUnit()
    {
        return mu;
    }
}