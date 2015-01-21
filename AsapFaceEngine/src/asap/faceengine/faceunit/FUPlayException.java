/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.faceunit;

import asap.motionunit.MUPlayException;

/**
 * Thrown whenever a FaceUnit fails during play
 * @author Dennis Reidsma
 */
public class FUPlayException extends MUPlayException
{
    private static final long serialVersionUID = 1L;
    private final FaceUnit fu;
    
    public FUPlayException(String str, FaceUnit f, Exception ex)
    {
        this(str,f);
        initCause(ex);
    }
    
    public FUPlayException(String str, FaceUnit f)
    {
        super(str,f);
        fu = f;
    }
    
    public final FaceUnit getFaceUnit()
    {
        return fu;
    }
}
