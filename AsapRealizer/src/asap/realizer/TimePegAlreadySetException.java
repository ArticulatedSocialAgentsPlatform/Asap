/*******************************************************************************
 *******************************************************************************/
package asap.realizer;

/**
 * TimePeg is already linked to a value other than TimePeg.VALUEUNKNOWN 
 * @author welberge
 */
public class TimePegAlreadySetException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final String syncId;
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

    public String getSyncId()
    {
        return syncId;
    }
    
    public TimePegAlreadySetException(String bmlId, String id, String syncId)
    {
        this.id = id;
        this.bmlId = bmlId;
        this.syncId = syncId;
    }
}
