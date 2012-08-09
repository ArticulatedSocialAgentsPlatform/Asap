package asap.hns;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

public class SymbolMatrices  extends XMLStructureAdapter
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
                throw new XMLScanException("Invalid tag "+tag+" in <symbolMatrices>");
            }
        }
    }    
    
    static final String XMLTAG = "symbolMatrices";

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
