package asap.murml;

import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private List<DynamicElement> dynamicElements = new ArrayList<>();

    @Getter
    private Slot slot;

    @Getter
    private String scope;

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            switch (tag)
            {
            case Keyframing.XMLTAG:
                keyframing = new Keyframing();
                keyframing.readXML(tokenizer);
                break;
            case DynamicElement.XMLTAG:
                DynamicElement dynamicElement = new DynamicElement();
                dynamicElement.readXML(tokenizer);
                dynamicElements.add(dynamicElement);
                break;
            default:
                throw new XMLScanException("Unkown tag " + tag + " in <dynamic>");
            }
        }
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        scope = getOptionalAttribute("scope", attrMap);
        String sl = getOptionalAttribute("slot", attrMap);
        if (sl != null)
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
