/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;

/**
 * Parser for the MURML function element
 * @author hvanwelbergen
 *
 */
public class Function extends MURMLElement
{
    @Getter
    private String name;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        name = getRequiredAttribute("name", attrMap, tokenizer);
    }
    
    private static final String XMLTAG = "function";

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
