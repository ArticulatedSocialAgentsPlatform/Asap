package asap.murml;

import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;

/**
 * Parser for the static murml element
 * @author hvanwelbergen
 *
 */
public class Static extends MURMLElement implements MovementConstraint
{
    @Getter
    private String scope;
    
    @Getter
    private Slot slot;
    
    @Getter
    private String value;
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        scope = getOptionalAttribute("scope", attrMap);
        String sl = getOptionalAttribute("slot", attrMap);
        if(sl!=null)
        {
            slot = Slot.valueOf(sl);
        }
        value = getRequiredAttribute("value", attrMap, tokenizer);        
    }
    
    static final String XMLTAG = "static";

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
