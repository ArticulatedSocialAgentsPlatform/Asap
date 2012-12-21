package asap.ipaacaadapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;
import ipaaca.AbstractIU;
import ipaaca.HandlerFunctor;
import ipaaca.IUEventHandler;
import ipaaca.IUEventType;
import ipaaca.Initializer;
import ipaaca.InputBuffer;
import ipaaca.LocalMessageIU;
import ipaaca.OutputBuffer;
import ipaaca.util.ComponentNotifier;

/**
 * Submits BML through ipaaca messages; submits received feedback to registered listeners.
 * @author Herwin
 */
public class BMLRealizerToIpaacaAdapter implements RealizerPort
{
    static
    {
        Initializer.initializeIpaacaRsb();
    }

    private final InputBuffer inBuffer = new InputBuffer("BMLToIpaacaRealizerAdapter",
            ImmutableSet.of(IpaacaBMLConstants.BML_FEEDBACK_CATEGORY));
    private final OutputBuffer outBuffer = new OutputBuffer("BMLToIpaacaRealizerAdapter");
    private List<BMLFeedbackListener> feedbackListeners = Collections.synchronizedList(new ArrayList<BMLFeedbackListener>());

    public BMLRealizerToIpaacaAdapter()
    {
        EnumSet<IUEventType> types = EnumSet.of(IUEventType.ADDED);
        inBuffer.registerHandler(new IUEventHandler(new HandlerFunctor()
        {
            @Override
            public void handle(AbstractIU iu, IUEventType type, boolean local)
            {
                synchronized (feedbackListeners)
                {
                    for (BMLFeedbackListener l : feedbackListeners)
                    {
                        l.feedback(iu.getPayload().get(IpaacaBMLConstants.BML_FEEDBACK_KEY));
                    }
                }
            }
        }, types, ImmutableSet.of(IpaacaBMLConstants.BML_FEEDBACK_CATEGORY)));
        
        ComponentNotifier notifier = new ComponentNotifier("BMLToIpaacaRealizerAdapter", "bmlprovider",
                ImmutableSet.of(IpaacaBMLConstants.BML_CATEGORY),ImmutableSet.of(IpaacaBMLConstants.BML_FEEDBACK_CATEGORY),
                outBuffer, inBuffer);
        notifier.initialize();
    }

    @Override
    public void addListeners(BMLFeedbackListener... listeners)
    {
        feedbackListeners.addAll(ImmutableList.copyOf(listeners));
    }

    @Override
    public void removeAllListeners()
    {
        feedbackListeners.clear();
    }

    @Override
    public void performBML(String bmlString)
    {
        LocalMessageIU feedbackIU = new LocalMessageIU();
        feedbackIU.setCategory(IpaacaBMLConstants.BML_CATEGORY);
        feedbackIU.getPayload().put(IpaacaBMLConstants.BML_KEY, bmlString);
        outBuffer.add(feedbackIU);
    }

    public void close()
    {
        outBuffer.close();
        inBuffer.close();
    }
}
