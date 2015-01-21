/*******************************************************************************
 *******************************************************************************/
package asap.realizer.anticipator;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;

/**
 * Anticipates the (may be changing through calls of updateTempo) tempo of a metronome. 
 * @author welberge
 */
@ThreadSafe
public class MetronomeAnticipator extends Anticipator
{
    public MetronomeAnticipator(String id, PegBoard pb)
    {
        super(id, pb);        
    }

    private List<TimePeg> orderedSynchs = Collections.synchronizedList(new ArrayList<TimePeg>());
    
    @Override
    public void addSynchronisationPoint(String syncRef, TimePeg sp)
    {
        super.addSynchronisationPoint(syncRef,sp);
        orderedSynchs.add(sp);
    }
    
    public void updateTempo(double tempo, double startTime)
    {
        double time = 0;
        boolean update = false;
        synchronized(orderedSynchs)
        {
            for(TimePeg p:orderedSynchs)
            {
                if(update)
                {
                    //TODO: in extreme tempo changes, this might move p.value to an earlier value as the previous value in the list. fix.
                    time+=tempo;
                    p.setGlobalValue(time);
                }
                else if(p.getGlobalValue()==TimePeg.VALUE_UNKNOWN || p.getGlobalValue()>startTime)                
                {
                    time = p.getGlobalValue();
                    update = true;
                }
            }
        }
    }
}
