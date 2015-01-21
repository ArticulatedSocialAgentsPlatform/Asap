/*******************************************************************************
 *******************************************************************************/
package asap.realizer;

/**
 * Behavior bmlId:id not found
 * @author welberge
 */
public class BehaviorNotFoundException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String bmlId;
    
    public String getId()
    {
        return id;
    }

    public String getBmlId()
    {
        return bmlId;
    }

    
    public BehaviorNotFoundException(String bmlId, String id)
    {
        this.id = id;
        this.bmlId = bmlId;
    }
}
