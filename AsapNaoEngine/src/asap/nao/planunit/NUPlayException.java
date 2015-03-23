/*******************************************************************************
 *******************************************************************************/
package asap.nao.planunit;

import asap.realizer.PlayException;

/**
 * Thrown whenever a NaoUnit fails during play
 * @author Robin ten Buuren
 */
@SuppressWarnings("serial")
public class NUPlayException extends PlayException
{
    private final NaoUnit nu;
    
    public NUPlayException(String str, NaoUnit n, Exception ex)
    {
        this(str,n);
        initCause(ex);
    }
    
    public NUPlayException(String str, NaoUnit n)
    {
        super(str);
        nu = n;
    }
    
    public final NaoUnit getNaoUnit()
    {
        return nu;
    }
}