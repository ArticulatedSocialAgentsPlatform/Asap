/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

/**
 * Generic parameter exception (e.g. in the combination of paramId and value or in the value of value) 
 * @author welberge
 */
public class InvalidParameterException extends ParameterException
{
    private static final long serialVersionUID = -8642635217080796975L;
    private final String paramId;
    private final String value;
    public String getParamId()
    {
        return paramId;
    }
    
    public String getValue()
    {
        return value;
    }
    
    public InvalidParameterException(String param, String value)
    {
        super("InvalidParameter "+param+"="+value);
        this.paramId = param;
        this.value = value;
    }
    
    public InvalidParameterException(String param, String value, Exception e)
    {
        this(param,value);
        initCause(e);
    }
    
    public InvalidParameterException(String message, String param, String value)
    {
        super(message);
        this.paramId = param;
        this.value = value;
    }
}
