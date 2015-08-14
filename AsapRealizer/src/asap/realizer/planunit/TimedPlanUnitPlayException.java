/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

/**
 * Thrown whenever a TimedPlanUnit fails during playback
 * @author hvanwelbergen
 *
 */
public class TimedPlanUnitPlayException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final TimedPlanUnit planUnit;
    
    public TimedPlanUnitPlayException(String str, TimedPlanUnit pu,Exception ex)
    {
        this(str,pu);
        initCause(ex);                
    }
    
    public TimedPlanUnitPlayException(String str, TimedPlanUnit pu)
    {
        super(pu.getClass().getName() +":"+ str);
        planUnit = pu;        
    }
    
    public final TimedPlanUnit getPlanUnit()
    {
        return planUnit;
    }    
}
