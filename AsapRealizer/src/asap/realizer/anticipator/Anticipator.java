/*******************************************************************************
 *******************************************************************************/
package asap.realizer.anticipator;



import java.util.Collection;

import lombok.Getter;
import net.jcip.annotations.ThreadSafe;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;

/**
 * An Anticipator manages a set of TimePegs that can be used to synchronize behavior to.
 * Anticipators typically store predictions of time events on the basis of observed interlocutor
 * behavior in their TimePegs. This predictions are often incremental, and increase in precision over
 * time. 
 * @author welberge
 */
@ThreadSafe
public class Anticipator
{
    
    private final PegBoard pegBoard;
    
    @Getter
    private final String id;
    
    public Anticipator(String id, PegBoard pb)
    {
        this.pegBoard = pb;
        this.id = id;
    }
    
    /**
     * Get an ImmutableList copy of the time pegs handled by this anticipator  
     */
    public synchronized Collection<TimePeg> getTimePegs()
    {
        return pegBoard.getTimePegs(BMLBlockPeg.ANTICIPATOR_PEG_ID, id);        
    }
    
    public synchronized TimePeg getSynchronisationPoint(String syncId)
    {
        return pegBoard.getTimePeg(BMLBlockPeg.ANTICIPATOR_PEG_ID,id, syncId);        
    }
    
    public synchronized void addSynchronisationPoint(String syncId, TimePeg sp)
    {
        pegBoard.addTimePeg(BMLBlockPeg.ANTICIPATOR_PEG_ID,id,syncId, sp);        
    }
    
    public synchronized void setSynchronisationPoint(String syncId, double time)
    {
        pegBoard.setPegTime(BMLBlockPeg.ANTICIPATOR_PEG_ID,id,syncId, time);
    }
}
