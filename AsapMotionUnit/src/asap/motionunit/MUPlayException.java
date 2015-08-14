/*******************************************************************************
 *******************************************************************************/
package asap.motionunit;


/**
 * Thrown whenever a MotionUnit fails during play
 * @author Herwin van Welbergen
 */
public class MUPlayException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final MotionUnit mu;

    public MUPlayException(String str, MotionUnit m)
    {
        super(str);
        mu = m;
    }

    public MUPlayException(String str, MotionUnit m, Throwable ex)
    {
        super(str);
        this.initCause(ex);
        mu = m;
    }

    public final MotionUnit getMotionUnit()
    {
        return mu;
    }
}
