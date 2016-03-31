/*******************************************************************************
 *******************************************************************************/
package asap.activemqadapters.loader;

import hmi.util.Clock;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import javax.jms.JMSException;

import lombok.extern.slf4j.Slf4j;
import asap.activemqadapters.ActiveMQToBMLRealizerAdapter;
import asap.realizerembodiments.PipeLoader;
import asap.realizerport.RealizerPort;

/**
 * Loader for and ActiveMQToRealizerAdapter
 * @author Dennis Reidsma
 * @author Herwin
 */
@Slf4j
public class ActiveMQToBMLRealizerAdapterLoader implements PipeLoader
{
    private RealizerPort adaptedRealizerPort = null;
    
    /**
     * @throws XMLScanException
     */
    @Override
    public void readXML(XMLTokenizer theTokenizer, String id, String vhId, String name, RealizerPort realizerPort, Clock theSchedulingClock)
            throws IOException
    {
        try
        {
            new ActiveMQToBMLRealizerAdapter(realizerPort);
        }
        catch (JMSException e)
        {
            log.error("Error registering at the ActiveMQ network: {}", e);
            throw new XMLScanException("Error registering at the ActiveMQ network");
        }
        adaptedRealizerPort = realizerPort;
        if (!theTokenizer.atETag("PipeLoader")) throw new XMLScanException("ActiveMQPipeLoader is an empty element");
    }

    @Override
    public RealizerPort getAdaptedRealizerPort()
    {
        return adaptedRealizerPort;
    }

    @Override
    public void shutdown()
    {
    }
}
