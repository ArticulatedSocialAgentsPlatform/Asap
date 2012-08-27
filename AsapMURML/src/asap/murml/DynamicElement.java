package asap.murml;

import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.Getter;

/**
 * dynamicElement parser
 * @author hvanwelbergen
 */
public class DynamicElement extends MURMLElement
{
    @Getter
    private String scope;
    
    @Getter
    private Type type;
    
    //value type->names mape
    private Map<String, String> valueMap = new HashMap<>();
    
    public enum Type
    {
        CURVE, LINEAR, CHOP;
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        scope = getOptionalAttribute("scope", attrMap);
        String typeString = getOptionalAttribute("type", attrMap);
        if(typeString!=null)
        {
            type = Type.valueOf(typeString.toUpperCase());
        }
    }
    
    public String getName(String type)
    {
        return valueMap.get(type);
    }
    
    public Set<Entry<String,String>> getValueNodes()
    {
        return valueMap.entrySet();
    }
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if(tag.equals(Value.xmlTag()))
            {
                Value v = new Value();
                v.readXML(tokenizer);
                valueMap.put(v.getType(), v.getName());
            }            
            else
            {
                throw new XMLScanException("Invalid tag "+tag+" in <dynamicElement>");
            }
        }
    }
    
    static final String XMLTAG = "dynamicElement";

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
