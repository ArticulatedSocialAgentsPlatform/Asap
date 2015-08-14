/*******************************************************************************
 *******************************************************************************/
package asap.srnao.planunit;


/**
 * Thrown whenever a NaoUnit fails during play
 * @author Daniel
 */
public class NUPlayException extends Exception
{
    private static final long serialVersionUID = 1423L;
    private final NaoUnit nu;
    
    public NUPlayException(String str, NaoUnit nu, Exception ex)
    {
        this(str,nu);
        initCause(ex);
    }
    
    public NUPlayException(String str, NaoUnit nu)
    {
        super(str);
        this.nu = nu;
    }
    
    public final NaoUnit getNaoUnit()
    {
        return nu;
    }
}