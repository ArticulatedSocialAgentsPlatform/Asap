package asap.realizer.bridge;

import asap.bml.ext.bmlt.feedback.BMLTSchedulingListener;
import asap.realizer.ElckerlycRealizer;
import saiba.bml.bridge.RealizerPort;
import saiba.bml.feedback.BMLExceptionListener;
import saiba.bml.feedback.BMLFeedbackListener;
import saiba.bml.feedback.BMLListener;
import saiba.bml.feedback.BMLWarningListener;

/** Access to an Elckerlyc VH through the RealizerBridge interface. */
public class ElckerlycRealizerPipe implements RealizerPort
{
    private ElckerlycRealizer realizer;
    
    public ElckerlycRealizerPipe(ElckerlycRealizer realizer)
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
        realizer.getScheduler().removeAllExceptionListeners();
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
            if(listener instanceof BMLExceptionListener)
            {
                realizer.addExceptionListener((BMLExceptionListener)listener);
            }
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