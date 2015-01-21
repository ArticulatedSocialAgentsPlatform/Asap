/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

/**
 * Float parameter with was not found
 * @author Herwin
 *
 */
public class PlanUnitFloatParameterNotFoundException extends ParameterException
{
    private static final long serialVersionUID = 1L;
    private final String behaviorId,bmlId,parameterId;
    public PlanUnitFloatParameterNotFoundException(String bmlId, String behId, String paramId)
    {
        super("Float parameter "+paramId+" not found on "+bmlId+":"+behId);
        behaviorId = behId;
        this.bmlId = bmlId;
        parameterId = paramId;        
    }
    
    public PlanUnitFloatParameterNotFoundException(String bmlId, String behId, String paramId, Exception cause)
    {
        this(bmlId,behId,paramId);
        this.initCause(cause);
    }
    public String getBehaviorId()
    {
        return behaviorId;
    }
    public String getBmlId()
    {
        return bmlId;
    }
    public String getParameterId()
    {
        return parameterId;
    }
}
