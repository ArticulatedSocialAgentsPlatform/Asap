/*******************************************************************************
 *******************************************************************************/
package asap.emitterengine.planunit;



/**
 * Thrown whenever an EmitterUnit fails during play
 * @author Dennis Reidsma
 */
public class EUPlayException extends Exception
{
    private static final long serialVersionUID = 1423L;
    private final EmitterUnit eu;
    
    public EUPlayException(String str, EmitterUnit e, Exception ex)
    {
        this(str,e);
        initCause(ex);
    }
    
    public EUPlayException(String str, EmitterUnit e)
    {
        super(str);
        eu = e;
    }
    
    public final EmitterUnit getEmitterUnit()
    {
        return eu;
    }
}