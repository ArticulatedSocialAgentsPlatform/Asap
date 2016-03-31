/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;

import com.google.common.collect.ImmutableList;

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
    private List<Value> values = new ArrayList<Value>();
    
    public enum Type
    {
        CURVE, LINEAR, CHOP, VIA;
    }
    
    public DynamicElement copy()
    {
        DynamicElement de = new DynamicElement();
        de.scope = scope;
        de.type = type;
        de.values = ImmutableList.copyOf(values);
        return de;
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
    
    @Override
    public StringBuilder appendAttributes(StringBuilder buf)
    {
        if (scope != null)
        {
            appendAttribute(buf, "scope", scope);
        }
        if (type != null)
        {
            appendAttribute(buf, "type", type.toString());
        }
        return buf;
    }
    
    @Override
    public StringBuilder appendContent(StringBuilder buf, XMLFormatting fmt)
    {
        for(Value v:values)
        {
            v.appendXML(buf);
        }
        return buf;
    }
    
    public String getName(String typeOrId)
    {
        for(Value v:values)
        {
            if(v.getType().equals(typeOrId)||v.getId().equals(typeOrId))
            {
                return v.getName();
            }
        }
        return null;
    }
    
    /**
     * Get an unmodifiable view of the values in this dynamic element
     * @return
     */
    public List<Value> getValues()
    {
        return Collections.unmodifiableList(values);
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
                values.add(v);
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
