package asap.realizertester;

import asap.bml.bridge.RealizerPort;
import asap.bml.feedback.BMLFeedbackListener;
import bml.realizertestport.XMLRealizerTestPort;

/**
 * Maps RealizerPort feedback to RealizerTestPort feedback
 * @author welberge
 */
public class AsapRealizerPort extends XMLRealizerTestPort implements RealizerPort
{
    private final RealizerPort realizerPort;

    public AsapRealizerPort(RealizerPort port)
    {
        realizerPort = port;
        realizerPort.addListeners(new MyListener());
    }

    @Override
    public void performBML(String bmlString)
    {
        realizerPort.performBML(bmlString);
    }

    @Override
    public void addListeners(asap.bml.feedback.BMLFeedbackListener... listeners)
    {
        realizerPort.addListeners(listeners);
    }

    @Override
    public void removeAllListeners()
    {
        realizerPort.removeAllListeners();
    }
    
    private class MyListener implements BMLFeedbackListener
    {
        @Override
        public void feedback(String feedback)
        {
            AsapRealizerPort.this.feedback(feedback);
        }        
    }
}
