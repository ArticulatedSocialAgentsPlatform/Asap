/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

/**
 * Generic BMLT parameter-value pair
 * @author welberge
 *
 */
public class BMLTParameter extends XMLStructureAdapter
{
    private static final String BMLTNAMESPACE = "http://hmi.ewi.utwente.nl/bmlt";

    @Override
    public String getNamespace()
    {
        return BMLTNAMESPACE;
    }

    public String value = "";

    public String name = "";

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "parameter";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given
     * String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an
     * object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "name", name);
        appendAttribute(buf, "value", value);
        return super.appendAttributeString(buf);
    }

    @Override
    public boolean hasContent()
    {
        return false;
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        name = getRequiredAttribute("name", attrMap, tokenizer);
        value = getRequiredAttribute("value", attrMap, tokenizer);
        super.decodeAttributes(attrMap, tokenizer);
    }

}
