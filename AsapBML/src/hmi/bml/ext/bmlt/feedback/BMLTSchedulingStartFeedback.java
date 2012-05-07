package hmi.bml.ext.bmlt.feedback;

import hmi.bml.feedback.BMLFeedback;
import net.jcip.annotations.Immutable;

/**
 * Feedback message to indicate that scheduling has started 
 * @author welberge
 */
@Immutable
public class BMLTSchedulingStartFeedback implements BMLFeedback
{
    public final double timeStamp;
    public final String id;
    public final String bmlId;    
    public final double predictedStart;
    
    public BMLTSchedulingStartFeedback(String id, String bmlId, double timeStamp, double predictedStart)
    {
        this.id = id;
        this.bmlId = bmlId;
        this.timeStamp = timeStamp;
        this.predictedStart = predictedStart;
    }
    
    @Override
    public final String toString()
    {
        return "Scheduling of "+bmlId+" started at "+timeStamp+", predicted block start "+predictedStart+"\n"; 
    }
}
