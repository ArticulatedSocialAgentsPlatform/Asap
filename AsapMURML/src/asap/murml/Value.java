/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;

/**
 * Parses the MURML value element
 * @author hvanwelbergen
 */
public class Value extends MURMLElement
{
    @Getter
    private String type;

    @Getter
    private String name;
    
    @Getter
    private String id;

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        type = getOptionalAttribute("type", attrMap, "");
        id = getOptionalAttribute("id", attrMap, "");
        if(id.isEmpty() && type.isEmpty())
        {
            throw new XMLScanException("Value should have either and id or a type");
        }
        name = getRequiredAttribute("name", attrMap, tokenizer);        
    }

    @Override
    public StringBuilder appendAttributes(StringBuilder buf)
    {
        if(type!=null && !type.isEmpty())
        {
            appendAttribute(buf, "type", type.toString());
        }
        if(id!=null && !id.isEmpty())
        {
            appendAttribute(buf, "id", id);
        }
        appendAttribute(buf, "name", name);
        return buf;
    }
    
    private static final String XMLTAG = "value";

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
