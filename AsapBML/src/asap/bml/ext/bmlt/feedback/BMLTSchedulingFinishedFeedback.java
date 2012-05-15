package asap.bml.ext.bmlt.feedback;

import saiba.bml.feedback.BMLFeedback;

/**
 * Feedback message indicating that scheduling of a BML block has finished.
 * @author welberge
 *
 */
public class BMLTSchedulingFinishedFeedback implements BMLFeedback
{
    public final double timeStamp;
    public final String id;
    public final String bmlId;    
    public final double predictedStart;
    public final double predictedEnd;
    
    public BMLTSchedulingFinishedFeedback(String id, String bmlId, double timeStamp,double predictedStart, double predictedEnd)
    {
        this.id = id;
        this.bmlId = bmlId;
        this.timeStamp = timeStamp;
        this.predictedStart = predictedStart;
        this.predictedEnd = predictedEnd;
    }
    
    @Override
    public final String toString()
    {
        return "Scheduling of "+bmlId+" finished at "+timeStamp+" predicted block start "+predictedStart+" predicted block end "+predictedEnd+"\n"; 
    }
}
