/*******************************************************************************
 *******************************************************************************/
package asap.realizer.bridge;

import asap.realizer.AsapRealizer;
import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

/** Access to an Elckerlyc VH through the RealizerBridge interface. */
public class ElckerlycRealizerPipe implements RealizerPort
{
    private AsapRealizer realizer;

    public ElckerlycRealizerPipe(AsapRealizer realizer)
    {
        this.realizer = realizer;
    }

    @Override
    public void performBML(String bmlString)
    {
        realizer.scheduleBML(bmlString);
    }

    @Override
    public void removeAllListeners()
    {
        realizer.getScheduler().removeAllFeedbackListeners();
    }

    @Override
    public void removeListener(BMLFeedbackListener l)
    {
        realizer.getScheduler().removeFeedbackListener(l);
        
    }
    
    @Override
    public void addListeners(BMLFeedbackListener... listeners)
    {
        for (BMLFeedbackListener listener : listeners)
        {
            realizer.addFeedbackListener((BMLFeedbackListener) listener);
        }
    }
}
