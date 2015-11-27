package asap.ipaacaeventengine;

import ipaaca.LocalMessageIU;
import ipaaca.OutputBuffer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of outputbuffers to send messages on the right scope
 * @author herwinvw
 *
 */
public class MessageManager
{
    private Map<String, OutputBuffer> outputbuffers = new ConcurrentHashMap<>();
    private final String id;

    public MessageManager(String id)
    {
        this.id = id;
    }
    
    private OutputBuffer getOutBuffer(String channel)
    {
        return outputbuffers.computeIfAbsent(channel, x -> new OutputBuffer(id, channel));
    }

    public void sendMessage(LocalMessageIU message)
    {
        sendMessage(message, "default");
    }

    public void sendMessage(LocalMessageIU message, String channel)
    {
        getOutBuffer(channel).add(message);
    }

    public void close()
    {
        outputbuffers.forEach((k, v) -> v.close());
    }
}
