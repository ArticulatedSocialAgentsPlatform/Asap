package asap.hns;

import java.util.HashMap;

import lombok.Getter;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

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
    private String value;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        className = getRequiredAttribute("class", attrMap, tokenizer);
        name = getRequiredAttribute("name", attrMap, tokenizer);
        value = getRequiredAttribute("value", attrMap, tokenizer);        
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
