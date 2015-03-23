/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

/**
 * Setup of TPU failed
 * @author hvanwelbergen
 * 
 */
public class TimedPlanUnitSetupException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final TimedPlanUnit planUnit;

    public TimedPlanUnitSetupException(String str, TimedPlanUnit pu, Exception ex)
    {
        this(str, pu);
        initCause(ex);
    }

    public TimedPlanUnitSetupException(String str, TimedPlanUnit tpu)
    {
        super(   (tpu==null?"Unknown TimedPlanUnit classname":tpu.getClass().getName()) + ":" + str);
        planUnit = tpu;
    }

    public final TimedPlanUnit getPlanUnit()
    {
        return planUnit;
    }
}
