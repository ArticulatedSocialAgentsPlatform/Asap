/*******************************************************************************
 *******************************************************************************/
package asap.audioengine;

/**
 * An exception occurred when planning/setting up au
 * @author Herwin
 *
 */
public class AudioUnitPlanningException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final TimedAbstractAudioUnit au;
    
    public AudioUnitPlanningException(String str, TimedAbstractAudioUnit a, Exception ex)
    {
        this(str,a);
        initCause(ex);
    }
    
    public AudioUnitPlanningException(String str, TimedAbstractAudioUnit a)
    {
        super(str);
        au = a;        
    }
    
    public final TimedAbstractAudioUnit getAudioUnit()
    {
        return au;
    }
}
