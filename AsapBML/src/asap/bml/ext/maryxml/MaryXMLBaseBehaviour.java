/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.maryxml;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import saiba.bml.core.SpeechBehaviour;

/**
 * Base class for all MaryTTSBehaviours
 * @author Herwin
 *
 */
class MaryXMLBaseBehaviour extends SpeechBehaviour
{
    public MaryXMLBaseBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, tokenizer);        
    }
    
    public MaryXMLBaseBehaviour(String bmlId, String id, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, id, tokenizer);        
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        content = tokenizer.getXMLSectionContent();        
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "maryxml";

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
    
    static final String NAMESPACE = "http://mary.dfki.de/2002/MaryXML";
    
    @Override
    public  String getNamespace() { return NAMESPACE; }
}
