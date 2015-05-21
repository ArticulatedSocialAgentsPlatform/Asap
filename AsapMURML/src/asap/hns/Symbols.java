/*******************************************************************************
 *******************************************************************************/
package asap.hns;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * Parses hns symbols
 * @author hvanwelbergen
 *
 */
public class Symbols extends XMLStructureAdapter
{
    @Getter
    private Map<String, Map<String, Double>> symbols = new HashMap<>(); // className->(name->value)
    
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
                Map<String, Double> map = symbols.get(s.getClassName());
                if(map==null)
                {
                    map = new HashMap<>();
                    symbols.put(s.getClassName(),map);
                }
                map.put(s.getName(), s.getValue());
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
