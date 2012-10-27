package asap.ipaacaadapters.loader;

import hmi.util.Clock;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.bml.bridge.RealizerPort;
import asap.ipaacaadapters.IpaacaToBMLRealizerAdapter;
import asap.realizerembodiments.PipeLoader;

/**
 * Loads a IpaacaToBMLRealizerAdapter from XML
 * @author Herwin
 *
 */
public class IpaacaToBMLRealizerAdapterLoader implements PipeLoader
{
    private RealizerPort adaptedRealizerPort = null;
    
    @Override
    public void readXML(XMLTokenizer theTokenizer, String id, String vhId, String name, RealizerPort realizerPort, Clock theSchedulingClock)
            throws XMLScanException, IOException
    {
        adaptedRealizerPort = realizerPort;
        new IpaacaToBMLRealizerAdapter(realizerPort);        
        if (!theTokenizer.atETag("PipeLoader")) throw new XMLScanException("IpaacaToBMLRealizerAdapterLoader should be an empty element");
    }

    @Override
    public RealizerPort getAdaptedRealizerPort()
    {
        return adaptedRealizerPort;
    }

}
