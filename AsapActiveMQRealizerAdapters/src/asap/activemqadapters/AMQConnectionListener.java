/*******************************************************************************
 *******************************************************************************/
package asap.activemqadapters;

import javax.jms.TextMessage;

/**
 * Interface for objects that want to listen to messages received by an AMQConnection
 * @author dennisr
 * 
 */
public interface AMQConnectionListener
{
    void onMessage(TextMessage m);

}
