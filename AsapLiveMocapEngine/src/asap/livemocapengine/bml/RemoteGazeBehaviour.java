/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.bml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

/**
 * Directly connects a gaze target to gaze output
 * @author welberge
 *
 */
public class RemoteGazeBehaviour extends LiveMocapBehaviour
{
    public RemoteGazeBehaviour(String bmlId)
    {
        super(bmlId);        
    }
    
    public RemoteGazeBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }
    
    private static final String XMLTAG = "remoteGaze";

    public static String xmlTag()
    {
        return XMLTAG;
    }
    
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
    
    private static final String BMLLIVENAMESPACE = "http://asap-project.org/livemocap";

    @Override
    public String getNamespace()
    {
        return BMLLIVENAMESPACE;
    }
}
