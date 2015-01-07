/*******************************************************************************
 *******************************************************************************/
package asap.realizer.pegboard;

import lombok.Getter;

import com.google.common.base.Objects;

/**
 * Stores sync point information, e.g. for quick lookups in the pegboard. 
 * @author hvanwelbergen
 */
public final class PegKey
{
    public PegKey(String bmlId, String id, String syncId)
    {
        this.syncId = syncId;
        this.id = id;
        this.bmlId = bmlId;
    }
    @Override 
    public boolean equals(Object o)
    {
        if(!(o instanceof PegKey))return false;
        PegKey pk = (PegKey)o;
        return pk.bmlId.equals(bmlId)&&pk.id.equals(id)&&pk.syncId.equals(syncId);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hashCode(syncId,id,bmlId);            
    }
    
    @Override
    public String toString()
    {
        return "PegKey("+bmlId+":"+id+":"+syncId+")";
    }
    
    @Getter
    final String syncId;
    @Getter
    final String id;
    @Getter
    final String bmlId;
}
