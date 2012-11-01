package asap.realizertester;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;
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
    public void addListeners(asap.realizerport.BMLFeedbackListener... listeners)
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
