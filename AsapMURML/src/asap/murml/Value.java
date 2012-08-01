package asap.murml;

import hmi.xml.XMLTokenizer;

import java.util.HashMap;
import java.util.List;

import lombok.Getter;

import com.google.common.collect.Lists;

/**
 * Parses the MURML value element
 * @author hvanwelbergen
 */
public class Value extends MURMLElement
{
    @Getter
    private String type;

    @Getter
    private List<String> names;

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        type = getRequiredAttribute("type", attrMap, tokenizer);
        String n = getRequiredAttribute("name", attrMap, tokenizer);
        names = Lists.newArrayList(decodeStringArray(n, " "));
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
