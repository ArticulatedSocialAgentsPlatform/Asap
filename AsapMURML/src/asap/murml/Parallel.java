package asap.murml;

import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Parser for the MURML parallel element
 * @author hvanwelbergen
 *
 */
public class Parallel extends MURMLElement
{
    @Getter
    private List<Dynamic> dynamics = new ArrayList<Dynamic>();
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(Dynamic.xmlTag()))
            {
                Dynamic dynamic = new Dynamic();
                dynamic.readXML(tokenizer);
                dynamics.add(dynamic);
            }
            //TODO: static
            else
            {
                throw new XMLScanException("Unknown element "+tag+" in parallel");
            }
        }
    }
    
    private static final String XMLTAG = "parallel";

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
