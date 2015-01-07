/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.Getter;

/**
 * Parser for the MURML constraints element
 * @author hvanwelbergen
 * 
 */
public class Constraints extends MURMLElement
{
    @Getter
    private Dynamic dynamic;

    @Getter
    private Parallel parallel;

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        String tag = tokenizer.getTagName();
        if (tag.equals(Dynamic.xmlTag()))
        {
            dynamic = new Dynamic();
            dynamic.readXML(tokenizer);
        }
        else if (tag.equals(Parallel.xmlTag()))
        {
            parallel = new Parallel();
            parallel.readXML(tokenizer);
        }
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
