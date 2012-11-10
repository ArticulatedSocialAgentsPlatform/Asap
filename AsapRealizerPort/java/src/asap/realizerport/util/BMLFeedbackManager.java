package asap.realizerport.util;

import java.util.ArrayList;
import java.util.List;

import asap.realizerport.BMLFeedbackListener;

/**
 * Manages multiple feedback listeners
 * @author Herwin
 */
public class BMLFeedbackManager
{
    private List<BMLFeedbackListener> listeners = new ArrayList<BMLFeedbackListener>();
    
    public void sendFeedback(String feedback)
    {
        for (BMLFeedbackListener l:listeners)
        {
            l.feedback(feedback);
        }
    }
    
    public void removeAllListeners()
    {
        listeners.clear();
    }
    
    public void addListeners(BMLFeedbackListener... bmlListeners)
    {
        for(BMLFeedbackListener l:bmlListeners)
        {
            listeners.add(l);
        }
    }
}
