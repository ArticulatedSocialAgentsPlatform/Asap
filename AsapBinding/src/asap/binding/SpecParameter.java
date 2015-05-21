/*******************************************************************************
 *******************************************************************************/
package asap.binding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

/**
 * Connects a BML behavior parameter name (src) to a planunit parameter name (dst)
 * @author welberge
 */
class SpecParameter extends XMLStructureAdapter
{
    public String src, dst;

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        src = getRequiredAttribute("src", attrMap, tokenizer);
        dst = getRequiredAttribute("dst", attrMap, tokenizer);
    }

    private static final String XMLTAG = "parameter";

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
