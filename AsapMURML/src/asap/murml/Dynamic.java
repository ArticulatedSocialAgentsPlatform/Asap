package asap.murml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;

/**
 * Parses the MURML dynamic element
 * @author hvanwelbergen
 */
public class Dynamic extends MURMLElement
{
    @Getter
    private Keyframing keyframing;
    
    @Getter
    private DynamicElement dynamicElement;
    
    
    @Getter
    private Slot slot;
    
    @Getter
    private String scope;
    
    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        String tag = tokenizer.getTagName();
        if (tag.equals(Keyframing.xmlTag()))
        {
            keyframing = new Keyframing();
            keyframing.readXML(tokenizer);
        }    
        else if (tag.equals(DynamicElement.xmlTag()))
        {
            dynamicElement = new DynamicElement();
            dynamicElement.readXML(tokenizer);
        }
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        scope = getOptionalAttribute("scope", attrMap);
        String sl = getOptionalAttribute("slot", attrMap);
        if(sl!=null)
        {
            slot = Slot.valueOf(sl);
        }
    }
    
    private static final String XMLTAG = "dynamic";

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
