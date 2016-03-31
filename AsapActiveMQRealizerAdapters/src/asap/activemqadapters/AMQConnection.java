/*******************************************************************************
 *******************************************************************************/
package asap.activemqadapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import lombok.extern.slf4j.Slf4j;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * An easy way to connect to the ActiveMQ network for simple text or XML messages.
 * The current version can only connect to an ActiveMQ channel running on the localhost.
 * 
 * Listeners activity (adding, removing, notifying) is done in synchronized methods.
 * 
 * @author Dennis Reidsma
 * @author Mark ter Maat
 */
@Slf4j
public class AMQConnection implements MessageListener
{

    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private final String[] receiveTopics;
    private final String[] sendTopics;
    private String name = null;

    private Session session;
    private Map<String, MessageProducer> messageProducers;

    public AMQConnection(String name, String[] sendTopics, String[] receiveTopics)
    {
        this.sendTopics = Arrays.copyOf(sendTopics,sendTopics.length);
        this.receiveTopics = Arrays.copyOf(receiveTopics, receiveTopics.length);
        this.name = name;

        try
        {
            initAMQConnection();
        }
        catch (JMSException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void initAMQConnection() throws JMSException
    {
        messageProducers = new HashMap<String, MessageProducer>();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        /* Init Receivers */
        for (String topicName : receiveTopics)
        {
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createConsumer(topic);
            consumer.setMessageListener(this);
        }

        for (String topicName : sendTopics)
        {
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            messageProducers.put(topicName, producer);
        }

    }

    /**
     * Send a message
     * @param topicName The topic on which the message must be sent (this topic must have been initialized in the constructor)
     * @param msg A simple text or XML message
     * @throws JMSException
     */
    public void sendMessage(String topicName, String msg) throws JMSException
    {
        MessageProducer producer = messageProducers.get(topicName);
        if (producer != null)
        {
            TextMessage message = session.createTextMessage();
            message.setText(msg);
            producer.send(message);
        }
    }

    @Override
    public void onMessage(Message m)
    {
        if (m instanceof TextMessage)
        {
            TextMessage textMessage = (TextMessage) m;
            sendMessage(textMessage);
        }
        else
        {
            log.error("Received message of type {} on ActiveMQConnection {}: not a text message!", m.getClass().getName(), name);
        }
    }

    /** These listeners want to receive the messages sent on the receivetopic channels set at construction time */
    private List<AMQConnectionListener> amqcListeners = new ArrayList<AMQConnectionListener>();

    public synchronized void removeAllListeners()
    {
        amqcListeners.clear();
    }

    public synchronized void removeListener(AMQConnectionListener amqcl)
    {
        amqcListeners.remove(amqcl);
    }

    /** Add a listeners to receive the messages sent on the receivetopic channels set at construction time */
    public synchronized void addListeners(AMQConnectionListener... amqcls)
    {
        for (AMQConnectionListener listener : amqcls)
        {
            amqcListeners.add(listener);
        }
    }

    /**
     * Do the sending of the message to the listeners
     * @param m
     */
    private synchronized void sendMessage(TextMessage m)
    {
        for (AMQConnectionListener l : amqcListeners)
        {
            l.onMessage(m);
        }
    }

}
