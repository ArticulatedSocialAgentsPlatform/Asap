package asap.murml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

/**
 * Parses the MURML dynamic element
 * @author hvanwelbergen
 */
public class Dynamic extends MURMLElement
{
    private Keyframing keyframing;
    
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        String tag = tokenizer.getTagName();
        if (tag.equals(Keyframing.xmlTag()))
        {
            keyframing = new Keyframing();
            keyframing.readXML(tokenizer);
        }
        //keyframing
        //dynamicElement
    }
    
    private static final String XMLTAG = "dynamic";

    public static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
