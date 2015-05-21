/*******************************************************************************
 *******************************************************************************/
package asap.picture.bml;

import hmi.xml.XMLFormatting;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

/**
 * Adds an image to the canvas on a specified layer
 */
public class AddAnimationDirBehavior extends PictureBehaviour
{

    private String resourcePath;
    private String directoryName;
    private float layer;

    @Override
    public boolean satisfiesConstraint(String name, String value)
    {
        if (name.equals("resourcePath"))
        {
            return true;
        }
        if (name.equals("directoryName"))
        {
            return true;
        }
        return super.satisfiesConstraint(name, value);
    }

    public AddAnimationDirBehavior(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf, XMLFormatting fmt)
    {
        appendAttribute(buf, "resourcePath", resourcePath.toString());
        appendAttribute(buf, "directoryName", directoryName.toString());
        appendAttribute(buf, "layer", layer);
        return super.appendAttributeString(buf, fmt);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        resourcePath = getRequiredAttribute("resourcePath", attrMap, tokenizer);
        directoryName = getRequiredAttribute("directoryName", attrMap, tokenizer);
        layer = getRequiredFloatAttribute("layer", attrMap, tokenizer);
        super.decodeAttributes(attrMap, tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "addAnimationDir";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to
     * see if a given String equals the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time
     * xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }

    @Override
    public String getStringParameterValue(String name)
    {
        if (name.equals("resourcePath"))
        {
            return resourcePath.toString();
        }
        if (name.equals("directoryName"))
        {
            return directoryName.toString();
        }
        return super.getStringParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name)
    {
        if (name.equals("layer"))
        {
            return layer;
        }
        return super.getFloatParameterValue(name);
    }

    @Override
    public boolean specifiesParameter(String name)
    {
        if (name.equals("resourcePath") || name.equals("directoryName") || name.equals("layer"))
        {
            return true;
        }
        return super.specifiesParameter(name);
    }
}
