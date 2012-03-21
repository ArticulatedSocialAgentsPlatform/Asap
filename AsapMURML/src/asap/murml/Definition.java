package asap.murml;

import java.io.IOException;

import lombok.Getter;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

/**
 * MURML definition parser
 * @author hvanwelbergen
 *
 */
public class Definition extends XMLStructureAdapter
{
    @Getter private Posture posture;
    @Getter private Keyframing keyframing; 
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        String tag = tokenizer.getTagName();
        if (tag.equals(Posture.xmlTag()))
        {
            posture = new Posture();
            posture.readXML(tokenizer);
        }
        if (tag.equals(Keyframing.xmlTag()))
        {
            keyframing = new Keyframing();
            keyframing.readXML(tokenizer);
        }        
    }
    
    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "definition";

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
}
