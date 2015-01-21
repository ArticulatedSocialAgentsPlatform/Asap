/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.bml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

/**
 * Directly connects recorded Face behaviour (in facs) to face output
 * @author welberge
 *
 */
public class RemoteFaceFACSBehaviour extends LiveMocapBehaviour
{

    public RemoteFaceFACSBehaviour(String bmlId)
    {
        super(bmlId);        
    }
    
    public RemoteFaceFACSBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }
    
    
    private static final String XMLTAG = "remoteFaceFACS";

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
