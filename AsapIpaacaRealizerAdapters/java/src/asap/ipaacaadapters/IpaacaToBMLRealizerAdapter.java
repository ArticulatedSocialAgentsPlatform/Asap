package asap.ipaacaadapters;

import ipaaca.AbstractIU;
import ipaaca.HandlerFunctor;
import ipaaca.IUEventHandler;
import ipaaca.IUEventType;
import ipaaca.Initializer;
import ipaaca.InputBuffer;
import ipaaca.LocalMessageIU;
import ipaaca.OutputBuffer;

import java.util.EnumSet;

import asap.bml.bridge.RealizerPort;
import asap.bml.feedback.BMLFeedbackListener;

import com.google.common.collect.ImmutableSet;

/**
 * Submits ipaaca messages (from an InputBuffer) to a RealizerPort; submits RealizerPort feedbacks to the OutputBuffer. 
 * Assumes that the connected realizerport is threadsafe (or at least that its performBML function is).
 * @author Herwin
 */
public class IpaacaToBMLRealizerAdapter implements BMLFeedbackListener
{
    static
    {
        Initializer.initializeIpaacaRsb();
    }
    

    private final InputBuffer inBuffer = new InputBuffer("IpaacaToBMLRealizerAdapter", ImmutableSet.of(IpaacaBMLConstants.BML_CATEGORY));
    private final OutputBuffer outBuffer = new OutputBuffer("IpaacaToBMLRealizerAdapter");
    private final RealizerPort realizerPort;

    public IpaacaToBMLRealizerAdapter(RealizerPort port)
    {
        this.realizerPort = port;
        realizerPort.addListeners(this);
        EnumSet<IUEventType> types = EnumSet.of(IUEventType.ADDED);
        inBuffer.registerHandler(new IUEventHandler(new HandlerFunctor()
        {
            @Override
            public void handle(AbstractIU iu, IUEventType type, boolean local)
            {
                realizerPort.performBML(iu.getPayload().get(IpaacaBMLConstants.BML_KEY));
            }
        }, types, ImmutableSet.of(IpaacaBMLConstants.BML_CATEGORY)));
    }

    @Override
    public void feedback(String feedback)
    {
        LocalMessageIU feedbackIU = new LocalMessageIU();
        feedbackIU.setCategory(IpaacaBMLConstants.BML_FEEDBACK_CATEGORY);
        feedbackIU.getPayload().put(IpaacaBMLConstants.BML_FEEDBACK_KEY, feedback);
        outBuffer.add(feedbackIU);
    }

    public void close()
    {
        outBuffer.close();
        inBuffer.close();
    }
}
