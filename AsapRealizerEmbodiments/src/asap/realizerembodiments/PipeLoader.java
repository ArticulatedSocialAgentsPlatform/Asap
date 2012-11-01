package asap.realizerembodiments;

import java.io.IOException;

import hmi.util.Clock;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;
import asap.realizerport.RealizerPort;

/**
 * A PipeLoader can adapt the realizerport of the BMLRealizer loaded into a AsapRealizerEmbodment.
 * PipeLoaders are added to the pipe in the order that they are specified in the XML
 **/
public interface PipeLoader
{
    /**
     * read the piping specification from the XML, and adapt the given realizerPort
     * @param theSchedulingClock
     * @throws IOException
     */
    void readXML(XMLTokenizer theTokenizer, String id, String vhId, String name, RealizerPort realizerPort, Clock theSchedulingClock)
            throws XMLScanException, IOException;

    /** return the adapted realizerPort */
    RealizerPort getAdaptedRealizerPort();
}
