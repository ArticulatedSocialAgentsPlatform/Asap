/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import asap.realizer.planunit.TimedPlanUnit;

/**
 * Exception in the planning of a timed speech unit 
 * @author welberge
 */
public class SpeechUnitPlanningException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final TimedPlanUnit su;
    
    public SpeechUnitPlanningException(String str, TimedPlanUnit s, Exception ex)
    {
        this(str,s);
        this.initCause(ex);     
    }
    
    public SpeechUnitPlanningException(String str, TimedPlanUnit s)
    {
        super(str);
        su = s;        
    }
    
    public final TimedPlanUnit getSpeechUnit()
    {
        return su;
    }
}
