/*******************************************************************************
 *******************************************************************************/
package asap.audioengine;

public class WavUnitPlanningException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final WavUnit wu;
    
    public WavUnitPlanningException(String str, WavUnit w,Exception ex)
    {
        this(str,w);
        initCause(ex);
    }
    
    public WavUnitPlanningException(String str, WavUnit w)
    {
        super(str);
        wu = w;        
    }
    
    public final WavUnit getWavUnit()
    {
        return wu;
    }
}
