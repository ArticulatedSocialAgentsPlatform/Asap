package asap.murml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

/**
 * Parser for the MURML constraints element
 * @author hvanwelbergen
 *
 */
public class Constraints extends MURMLElement
{
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        //dynamic
        //parallel
    }
    
    private static final String XMLTAG = "constraints";

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
