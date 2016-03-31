/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

/**
 * Indicates that a trajectory of type type could not be constructed/found
 * @author welberge
 */
public class TrajectoryBindingException extends Exception
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String type;
    public TrajectoryBindingException(String type)
    {
        this.type = type;
    }
    public String getType()
    {
        return type;
    }
}
