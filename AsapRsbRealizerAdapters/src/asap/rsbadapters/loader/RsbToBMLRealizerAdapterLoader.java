package asap.rsbadapters.loader;

import hmi.util.Clock;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.bml.bridge.RealizerPort;
import asap.realizerembodiments.PipeLoader;
import asap.rsbadapters.RsbToBMLRealizerAdapter;

/**
 * Loads a RsbToBMLRealizerAdapter from XML
 * @author Herwin
 *
 */
public class RsbToBMLRealizerAdapterLoader implements PipeLoader
{
    private RealizerPort adaptedRealizerPort = null;
    
    @Override
    public void readXML(XMLTokenizer theTokenizer, String id, String vhId, String name, RealizerPort realizerPort, Clock theSchedulingClock)
            throws XMLScanException, IOException
    {
        adaptedRealizerPort = realizerPort;
        new RsbToBMLRealizerAdapter(realizerPort);         
        if (!theTokenizer.atETag("PipeLoader")) throw new XMLScanException("RsbToBMLRealizerAdapter should be an empty element");
    }

    @Override
    public RealizerPort getAdaptedRealizerPort()
    {
        return adaptedRealizerPort;
    }
    
}
