/*******************************************************************************
 *******************************************************************************/
package asap.picture.planunit;


/**
 * Thrown whenever a PictureUnit fails during play
 * @author Dennis Reidsma
 */
public class PUPlayException extends Exception
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