/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.ssml;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import saiba.bml.core.SpeechBehaviour;

/**
 * Speech Synthesis Markup Language behaviour.
 * Content contains the speech text, minus the &ltspeak&gt tag. 
 * This allows implementations to send a custom speak header (in Mary TTS's case with the correct language locale for the currently selected voice).  
 * @author Herwin
 */
public class SSMLBehaviour extends SpeechBehaviour
{
    public SSMLBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId, tokenizer);        
    }
    
    public SSMLBehaviour(String bmlId, String id, XMLTokenizer tokenizer) throws IOException
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
    private static final String XMLTAG = "speak";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to
     * see if a given String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
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
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        if (content != null) buf.append(content);
        return buf;
    }
    
    static final String NAMESPACE = "http://www.w3.org/2001/10/synthesis";
    
    @Override
    public  String getNamespace() { return NAMESPACE; }
}
