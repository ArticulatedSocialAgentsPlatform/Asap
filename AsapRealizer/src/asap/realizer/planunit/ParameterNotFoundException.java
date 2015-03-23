/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

/**
 * Parameter was not found
 * @author welberge
 */
public class ParameterNotFoundException extends ParameterException
{
    private static final long serialVersionUID = 1L;
    private final String paramId;
    public String getParamId()
    {
        return paramId;
    }
    
    public ParameterNotFoundException(String param)
    {
        super("ParameterNotFound "+param);
        this.paramId = param;
    }
    
    public ParameterNotFoundException(String param,Exception ex)
    {
        this(param);
        initCause(ex);        
    }
}
