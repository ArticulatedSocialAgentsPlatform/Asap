package asap.bml.ext.maryxml;

import saiba.bml.core.SpeechBehaviour;
import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
/**
 * MaryTTS words behavior
 * @author reidsma
 */
public class MaryWordsBehaviour extends SpeechBehaviour
{
    public MaryWordsBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
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

    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        if (content != null) buf.append(content);
        return buf;
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
