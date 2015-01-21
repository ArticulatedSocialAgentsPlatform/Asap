/*******************************************************************************
 *******************************************************************************/
package asap.realizer;

/**
 * bmlId:id:syncId is not valid/found on behavior bmlId:id.
 * @author welberge
 */
public class SyncPointNotFoundException extends Exception
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
    
    public SyncPointNotFoundException(String bmlId, String id, String syncId)
    {
        this.id = id;
        this.bmlId = bmlId;
        this.syncId = syncId;
    }
}
