/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaadapters;

import ipaaca.AbstractIU;
import ipaaca.HandlerFunctor;
import ipaaca.IUEventHandler;
import ipaaca.IUEventType;
import ipaaca.Initializer;
import ipaaca.InputBuffer;
import ipaaca.LocalMessageIU;
import ipaaca.OutputBuffer;
import ipaaca.util.ComponentNotifier;

import java.util.EnumSet;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

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

    private final InputBuffer inBuffer;
    private final OutputBuffer outBuffer;
    private final RealizerPort realizerPort;

    public IpaacaToBMLRealizerAdapter(RealizerPort port, String characterId)
    {
        if (characterId != null)
        {
            inBuffer = new InputBuffer("IpaacaToBMLRealizerAdapter", ImmutableSet.of(IpaacaBMLConstants.BML_CATEGORY), characterId);
            outBuffer = new OutputBuffer("IpaacaToBMLRealizerAdapter", characterId);
        }
        else
        {
            inBuffer = new InputBuffer("IpaacaToBMLRealizerAdapter", ImmutableSet.of(IpaacaBMLConstants.BML_CATEGORY));
            outBuffer = new OutputBuffer("IpaacaToBMLRealizerAdapter");
        }

        this.realizerPort = port;
        realizerPort.addListeners(this);
        EnumSet<IUEventType> types = EnumSet.of(IUEventType.ADDED, IUEventType.MESSAGE);
        inBuffer.registerHandler(new IUEventHandler(new HandlerFunctor()
        {
            @Override
            public void handle(AbstractIU iu, IUEventType type, boolean local)
            {
                realizerPort.performBML(iu.getPayload().get(IpaacaBMLConstants.BML_KEY));
            }
        }, types, ImmutableSet.of(IpaacaBMLConstants.BML_CATEGORY)));

        ComponentNotifier notifier = new ComponentNotifier("IpaacaToBMLRealizerAdapter", "bmlrealizer",
                ImmutableSet.of(IpaacaBMLConstants.BML_FEEDBACK_CATEGORY), ImmutableSet.of(IpaacaBMLConstants.BML_CATEGORY), outBuffer,
                inBuffer);
        notifier.initialize();
    }

    public IpaacaToBMLRealizerAdapter(RealizerPort port)
    {
        this(port, null);
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
