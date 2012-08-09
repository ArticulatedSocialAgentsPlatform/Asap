package asap.murml;

import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

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
    private ListMultimap<String, String> valueMap = ArrayListMultimap.create();
    
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
    
    public List<String> getNames(String type)
    {
        return valueMap.get(type);
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
                valueMap.putAll(v.getType(), v.getNames());
            }            
            else
            {
                throw new XMLScanException("Invalid tag "+tag+" in <gesture>");
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
