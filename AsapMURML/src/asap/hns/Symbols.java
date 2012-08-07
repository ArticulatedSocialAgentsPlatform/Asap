package asap.hns;

import java.io.IOException;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

/**
 * Parses hns symbols
 * @author hvanwelbergen
 *
 */
public class Symbols extends XMLStructureAdapter
{
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if(tag.equals(Symbol.xmlTag()))
            {
                Symbol s = new Symbol();
                s.readXML(tokenizer);   
                //TODO: actually do something with the symbol
            }            
            else
            {
                throw new XMLScanException("Invalid tag "+tag+" in <symbols>");
            }
        }
    }    
    
    static final String XMLTAG = "symbols";

    public final static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
