package hmi.elckerlyc.planunit;

/**
 * Superclass for all Parameter related exceptions 
 * @author welberge
 */
public abstract class ParameterException extends Exception
{
    private static final long serialVersionUID = 1L;

    public ParameterException()
    {
        super();
    }
    
    public ParameterException(String message)
    {
        super(message);
    }    
}
