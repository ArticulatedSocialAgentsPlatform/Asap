package asap.rsbadapters;

import lombok.extern.slf4j.Slf4j;
import rsb.AbstractEventHandler;
import rsb.Event;
import rsb.Factory;
import rsb.Informer;
import rsb.InitializeException;
import rsb.Listener;
import rsb.RSBException;
import asap.bml.bridge.RealizerPort;
import asap.bml.feedback.BMLFeedbackListener;

/**
 * Submits rsb BML messages to a RealizerPort; submits RealizerPort feedbacks to the rsb. 
 * Assumes that the connected realizerport is threadsafe (or at least that its performBML function is).
 * @author Herwin
 */
@Slf4j
public class RsbToBMLRealizerAdapter implements BMLFeedbackListener
{
    private final RealizerPort realizerPort;
    private final Informer<String> informer;
    private final Listener listener;
    
    public RsbToBMLRealizerAdapter(RealizerPort port)
    {
        realizerPort = port;
        realizerPort.addListeners(this);
        Factory factory = Factory.getInstance();

        // setup feedback sender
        informer = factory.createInformer(RsbAdapterConstants.BML_FEEDBACK_SCOPE);        

        // setup BML receiver
        listener = factory.createListener(RsbAdapterConstants.BML_SCOPE);
        listener.addHandler(new AbstractEventHandler()
        {
            @Override
            public void handleEvent(Event event)
            {
                realizerPort.performBML(event.getData().toString());
            }
        }, true);
        
        try
        {
            listener.activate();
            informer.activate();
        }
        catch (InitializeException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Submit feedback over rsb
     */
    @Override
    public void feedback(String feedback)
    {
        try
        {
            informer.send(feedback);
        }
        catch (RSBException e)
        {
            log.warn("Could not submit feedback over RSB.", e);
        }
    }

    public void close()
    {
        informer.deactivate();
        listener.deactivate();
    }
}
