/*******************************************************************************
 *******************************************************************************/
package asap.hns;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;

/**
 * Parses a hns symbole
 * @author hvanwelbergen
 */
public class Symbol extends XMLStructureAdapter
{
    @Getter
    private String className;
    
    @Getter
    private String name;
    
    @Getter
    private Double value;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        className = getRequiredAttribute("class", attrMap, tokenizer);
        name = getRequiredAttribute("name", attrMap, tokenizer);
        String str = getRequiredAttribute("value", attrMap, tokenizer);
        if(!str.isEmpty())
        {
            value = Double.parseDouble(str);         
        }
        else
        {
            value = null;
        }
    }
    
    static final String XMLTAG = "symbol";

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
