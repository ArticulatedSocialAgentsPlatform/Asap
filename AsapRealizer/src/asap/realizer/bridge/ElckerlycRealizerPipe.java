package asap.realizer.bridge;

import asap.bml.bridge.RealizerPort;
import asap.bml.feedback.BMLFeedbackListener;
import asap.realizer.AsapRealizer;

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
        //realizer.parseXML(bmlString);
        realizer.scheduleBML(bmlString);
    }
    
    @Override
    public void removeAllListeners()
    {
        realizer.getScheduler().removeAllFeedbackListeners();        
    }
    
    @Override
    public void addListeners(BMLFeedbackListener ... listeners)
    {
        for (BMLFeedbackListener listener: listeners)
        {
            realizer.addFeedbackListener((BMLFeedbackListener)listener);
        }
    }
    
}