/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

/**
 * Parameter with parameterId was not found
 * @author Herwin
 *
 */
public class PlanUnitParameterNotFoundException extends ParameterException
{
    private static final long serialVersionUID = 1L;
    private final String behaviorId,bmlId,parameterId;
    
    public PlanUnitParameterNotFoundException(String bmlId, String behId, String paramId)
    {
        super("Parameter "+paramId+" not found on "+bmlId+":"+behId);
        behaviorId = behId;
        this.bmlId = bmlId;
        parameterId = paramId;        
    }
    
    public PlanUnitParameterNotFoundException(String bmlId, String behId, String paramId,Exception ex)
    {
        this(bmlId,behId,paramId);
        initCause(ex);
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
