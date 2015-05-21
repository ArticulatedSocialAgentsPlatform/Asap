/*******************************************************************************
 *******************************************************************************/
package asap.tcpipadapters.loader;

import hmi.util.Clock;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;
import asap.realizerembodiments.PipeLoader;
import asap.realizerport.RealizerPort;
import asap.tcpipadapters.TCPIPToBMLRealizerAdapter;

/**
 * Loads a TCPIPToBMLRealizerAdapterLoader from XML
 * @author Herwin
 *
 */
public class TCPIPToBMLRealizerAdapterLoader implements PipeLoader
{
    private RealizerPort adaptedRealizerPort = null;
    
    @Getter
    private TCPIPToBMLRealizerAdapter tcpIpAdapter;
    
    @Override
    /**
     * @throws XMLScanException on invalid loader XML
     */
    public void readXML(XMLTokenizer theTokenizer, String id, String vhId, String name, RealizerPort realizerPort, Clock theSchedulingClock)
            throws IOException
    {
        adaptedRealizerPort = realizerPort;
        if(!theTokenizer.atSTag("ServerOptions"))
        {
            throw new XMLScanException("TCPIPToBMLRealizerAdapterLoader requires an inner ServerOptions element");            
        }
        
        HashMap<String, String> attrMap = theTokenizer.getAttributes();
        XMLStructureAdapter adapter = new XMLStructureAdapter();
        String requestPort = adapter.getRequiredAttribute("bmlport", attrMap, theTokenizer);
        String feedbackPort = adapter.getRequiredAttribute("feedbackport", attrMap, theTokenizer);
        tcpIpAdapter = new TCPIPToBMLRealizerAdapter(realizerPort, Integer.parseInt(requestPort), Integer.parseInt(feedbackPort));
        theTokenizer.takeSTag("ServerOptions");
        theTokenizer.takeETag("ServerOptions");        
    }

    @Override
    public RealizerPort getAdaptedRealizerPort()
    {
        return adaptedRealizerPort;
    }

    @Override
    public void shutdown()
    {
        tcpIpAdapter.shutdown();        
    }
}
