/*******************************************************************************
 *******************************************************************************/
package asap.picture.planunit;

import asap.realizer.PlayException;

/**
 * Thrown whenever a PictureUnit fails during play
 * @author Dennis Reidsma
 */
public class PUPlayException extends PlayException
{
    private static final long serialVersionUID = 1423L;
    private final PictureUnit pu;
    
    public PUPlayException(String str, PictureUnit p, Exception ex)
    {
        this(str,p);
        initCause(ex);
    }
    
    public PUPlayException(String str, PictureUnit p)
    {
        super(str);
        pu = p;
    }
    
    public final PictureUnit getPictureUnit()
    {
        return pu;
    }
}