/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.bml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

/**
 * Directly connects input head movement to output head movement 
 * @author welberge
 */
public class RemoteHeadBehaviour extends LiveMocapBehaviour
{
    public RemoteHeadBehaviour(String bmlId)
    {
        super(bmlId);        
    }
    
    public RemoteHeadBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }
    
    private static final String XMLTAG = "remoteHead";

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
