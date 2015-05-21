/*******************************************************************************
 *******************************************************************************/
package asap.activemqadapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.realizerport.BMLFeedbackListener;
import asap.realizerport.RealizerPort;

import com.google.common.collect.ImmutableList;

/**
 * Submits BML through activemq messages; submits received feedback to registered listeners.
 * @author Dennis Reidsma
 * @author Herwin
 */
@Slf4j
public class BMLRealizerToActiveMQAdapter implements RealizerPort, AMQConnectionListener
{
    private List<BMLFeedbackListener> feedbackListeners = Collections.synchronizedList(new ArrayList<BMLFeedbackListener>());

    private AMQConnection amqConnection = null;

    public BMLRealizerToActiveMQAdapter()
    {
        amqConnection = new AMQConnection("BMLRealizerToActiveMQAdapter", new String[] { AMQBMLConstants.BML },
                new String[] { AMQBMLConstants.BML_FEEDBACK });
        amqConnection.addListeners(this);
    }

    /* the message will be bml feedback... so send it on to any listeners */
    @Override
    public void onMessage(TextMessage m)
    {
        // TODO: figure out whether it is guaranteed that onMessage is called in-order on one thread
        // (this, because we want to be sure that the messages are also redirected to the listeners in-order)
        String msg = "";
        try
        {
            msg = m.getText();
            try
            {
                sendFeedback(msg);
            }
            catch (Exception ex)
            {
                // general catch because broken listeners should not crash this component
                log.warn("Error sending BMLFeedback to Listeners", ex);
            }
        }
        catch (JMSException ex)
        {
            throw new RuntimeException("Cannot get text from message");
        }
    }

    private void sendFeedback(String feedback)
    {
        synchronized (feedbackListeners)
        {
            for (BMLFeedbackListener fbl : feedbackListeners)
            {
                fbl.feedback(feedback);
            }
        }
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
    public void removeListener(BMLFeedbackListener l)
    {
        feedbackListeners.remove(l);        
    }   
    
    @Override
    public void performBML(String bmlString)
    {
        try
        {
            amqConnection.sendMessage(AMQBMLConstants.BML, bmlString);
        }
        catch (JMSException e)
        {
            // log failure; send feedback to BML Listeners
            BMLWarningFeedback feedback = new BMLWarningFeedback("no id", "CANNOT_SEND", "Failure to send BML over ActiveMQ.");
            try
            {
                sendFeedback(feedback.toXMLString());
            }
            catch (Exception ex)
            {
                // general catch because broken listeners should not crash this component
                log.warn("Error sending BMLFeedback to Listeners", ex);
            }
        }
    }

     
}
