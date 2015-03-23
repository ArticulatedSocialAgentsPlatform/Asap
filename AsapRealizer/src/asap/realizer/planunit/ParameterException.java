/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;

/**
 * Superclass for all Parameter related exceptions 
 * @author welberge
 */
public class ParameterException extends Exception
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
    
    public ParameterException(String message, Exception ex)
    {
        super(message,ex);
    } 
}
