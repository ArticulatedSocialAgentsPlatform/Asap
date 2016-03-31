/*******************************************************************************
 *******************************************************************************/
package asap.audioengine;

import asap.realizer.planunit.TimedPlanUnitPlayException;
/**
 * TimedPlanUnitPlayException implementation for a TimedAudioUnit 
 * @author welberge
 */
public class TimedAudioUnitPlayException extends TimedPlanUnitPlayException
{
    private static final long serialVersionUID = 1L;
    private final TimedAbstractAudioUnit au;
    
    public TimedAudioUnitPlayException(String str, TimedAbstractAudioUnit a,Exception ex)
    {
        this(str,a);
        initCause(ex);
    }
    
    public TimedAudioUnitPlayException(String str, TimedAbstractAudioUnit a)
    {
        super(str, a);
        au = a;        
    }
    
    public final TimedAbstractAudioUnit getAudioUnit()
    {
        return au;
    }
}
