package asap.realizer.bridge;

import asap.bml.bridge.RealizerPort;
import asap.bml.ext.bmlt.feedback.BMLTSchedulingListener;
import asap.bml.feedback.BMLFeedbackListener;
import asap.bml.feedback.BMLListener;
import asap.bml.feedback.BMLWarningListener;
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
    
    public void addPlanningListener(BMLTSchedulingListener bpl)
    {
        realizer.addPlanningListener(bpl);
    }
    
    @Override
    public void removeAllListeners()
    {
        realizer.getScheduler().removeAllWarningListeners();
        realizer.getScheduler().removeAllFeedbackListeners();
        realizer.getScheduler().removeAllPlanningListeners();
    }
    
    @Override
    public void addListeners(BMLListener ... listeners)
    {
        for (BMLListener listener: listeners)
        {
            //note: one listener may be of more than one type!            
            if (listener instanceof BMLWarningListener)
            {
                realizer.addWarningListener((BMLWarningListener)listener);
            }
            if (listener instanceof BMLFeedbackListener)
            {
                realizer.addFeedbackListener((BMLFeedbackListener)listener);
            }
            if (listener instanceof BMLTSchedulingListener)
            {
                realizer.addPlanningListener((BMLTSchedulingListener)listener);
            }
        }
    }
    
}