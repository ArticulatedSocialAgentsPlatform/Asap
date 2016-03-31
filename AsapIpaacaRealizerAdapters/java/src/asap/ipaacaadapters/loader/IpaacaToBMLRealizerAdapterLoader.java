/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaadapters.loader;

import hmi.util.Clock;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.ipaacaadapters.IpaacaToBMLRealizerAdapter;
import asap.realizerembodiments.PipeLoader;
import asap.realizerport.RealizerPort;

/**
 * Loads a IpaacaToBMLRealizerAdapter from XML
 * @author Herwin
 *
 */
public class IpaacaToBMLRealizerAdapterLoader implements PipeLoader
{
    private RealizerPort adaptedRealizerPort = null;
    private IpaacaToBMLRealizerAdapter ipaacaAdapter;
    
    /**
     * @throws XMLScanException
     */
    @Override
    public void readXML(XMLTokenizer theTokenizer, String id, String vhId, String name, RealizerPort realizerPort, Clock theSchedulingClock)
            throws IOException
    {
        adaptedRealizerPort = realizerPort;
        ipaacaAdapter = new IpaacaToBMLRealizerAdapter(realizerPort, vhId);        
        if (!theTokenizer.atETag("PipeLoader")) throw new XMLScanException("IpaacaToBMLRealizerAdapterLoader should be an empty element");
    }

    @Override
    public RealizerPort getAdaptedRealizerPort()
    {
        return adaptedRealizerPort;
    }

    @Override
    public void shutdown()
    {
        ipaacaAdapter.close();
    }

}
