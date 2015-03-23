/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.msapi;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import saiba.bml.core.SpeechBehaviour;

/**
 * Microsoft speech API behavior 
 * @author welberge
 */
public class MSApiBehaviour extends SpeechBehaviour
{
    public MSApiBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, tokenizer);        
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap,
            XMLTokenizer tokenizer)
    {
        // empty, 'cause id is not required
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        content = tokenizer.getXMLSectionContent();        
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "sapi";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to
     * see if a given String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        if (content != null) buf.append(content);
        return buf;
    }
    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time
     * xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
    
    @Override
    public  String getNamespace() { return null; }
}
