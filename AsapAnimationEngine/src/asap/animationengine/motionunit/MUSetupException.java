package asap.animationengine.motionunit;

/**
 * Thrown when a motion unit cannot be created/setup
 * @author Herwin
 *
 */
public class MUSetupException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final MotionUnit mu;
    
    public MUSetupException(String str, MotionUnit m)
    {
        super(str);
        mu = m;        
    }
    
    public final MotionUnit getMotionUnit()
    {
        return mu;
    }
}