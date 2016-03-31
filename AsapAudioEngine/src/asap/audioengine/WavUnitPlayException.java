/*******************************************************************************
 *******************************************************************************/
package asap.audioengine;

public class WavUnitPlayException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final WavUnit wu;
    
    public WavUnitPlayException(String str, WavUnit w, Exception ex)
    {
        this(str,w);
        initCause(ex);
    }
    
    public WavUnitPlayException(String str, WavUnit w)
    {
        super(str);
        wu = w;        
    }
    
    public final WavUnit getWavUnit()
    {
        return wu;
    }
}
