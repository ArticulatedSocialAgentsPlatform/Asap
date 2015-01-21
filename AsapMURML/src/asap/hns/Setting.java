/*******************************************************************************
 *******************************************************************************/
package asap.hns;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;

/**
 * hns setting parser
 * @author hvanwelbergen
 *
 */
public class Setting extends XMLStructureAdapter
{
    @Getter
    private String name;
    
    @Getter
    private String value;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        name = getRequiredAttribute("name", attrMap, tokenizer);
        value = getRequiredAttribute("value", attrMap, tokenizer);        
    }
    
    private static final String XMLTAG = "setting";

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
