/*******************************************************************************
 *******************************************************************************/
package asap.realizerembodiments;

import hmi.util.Clock;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizer.bridge.LogPipe;
import asap.realizerport.RealizerPort;

/**
 * LogPipeLoader has one element as child: <Log> with optional attributes requestlog and feedbacklog
 */
public class LogPipeLoader implements PipeLoader
{

    private LogPipe adaptedRealizerPort = null;

    /**
     * @throws XMLScanException
     */
    @Override
    public void readXML(XMLTokenizer theTokenizer, String id, String vhId, String name, RealizerPort realizerPort,
            Clock theSchedulingClock) throws IOException
    {
        XMLStructureAdapter adapter = new XMLStructureAdapter();
        HashMap<String, String> attrMap = null;

        if (!theTokenizer.atSTag("Log")) throw new XMLScanException("LogPipeLoader can have only one <Log> child element");

        attrMap = theTokenizer.getAttributes();
        String requestLog = adapter.getOptionalAttribute("requestlog", attrMap);
        String feedbackLog = adapter.getOptionalAttribute("feedbacklog", attrMap);
        Logger rl = null;
        Logger fl = null;
        if (requestLog != null)
        {
            rl = LoggerFactory.getLogger(requestLog);
        }
        if (feedbackLog != null)
        {
            fl = LoggerFactory.getLogger(feedbackLog);
        }
        adaptedRealizerPort = new LogPipe(rl, fl, realizerPort, theSchedulingClock);
        theTokenizer.takeSTag("Log");
        theTokenizer.takeETag("Log");
        if (!theTokenizer.atETag("PipeLoader")) throw new XMLScanException("LogPipeLoader can have only one <Log> child element");
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
