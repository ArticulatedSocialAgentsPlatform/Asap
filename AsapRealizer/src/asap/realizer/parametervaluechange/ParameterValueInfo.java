/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

import net.jcip.annotations.Immutable;

/**
 * Provides information on the parameter id and desired values of a parameter value change
 */
@Immutable
public final class ParameterValueInfo
{
    private final String targetId;    
    private final String targetBmlId;
    private final String paramId;
    private final float initialValue;
    private final float targetValue;
    private final boolean hasInitialValue;
    
    private ParameterValueInfo(String targetBmlId, String targetId, String paramId, float initialValue, float targetValue,boolean hasInitialValue)
    {
        this.targetId = targetId;
        this.paramId = paramId;
        this.targetValue = targetValue;
        this.targetBmlId = targetBmlId;
        this.initialValue = initialValue;
        this.hasInitialValue = hasInitialValue;
    }
    
    public ParameterValueInfo(String targetBmlId, String targetId, String paramId, float targetValue)
    {
        this(targetBmlId, targetId, paramId, 0, targetValue,false);        
    }
    
    public ParameterValueInfo(String targetBmlId, String targetId, String paramId, float initialValue, float targetValue)
    {
        this(targetBmlId, targetId, paramId, initialValue, targetValue,true);        
    }

    public String getTargetId()
    {
        return targetId;
    }

    public String getTargetBmlId()
    {
        return targetBmlId;
    }

    public String getParamId()
    {
        return paramId;
    }

    public float getInitialValue()
    {
        return initialValue;
    }

    public float getTargetValue()
    {
        return targetValue;
    }

    public boolean hasInitialValue()
    {
        return hasInitialValue;
    }
}
