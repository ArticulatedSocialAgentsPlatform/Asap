/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmla;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

/**
 * Encodes the trajectory of a BMLT parametervalue change behavior.
 * @author welberge
 *
 */
public class BMLATrajectory extends XMLStructureAdapter
{
    @Override
    public String getNamespace()
    {
        return BMLAInfo.BMLA_NAMESPACE;
    }

    public String type = "";

    public String initialValue = "";
    
    public String targetValue = "";

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "trajectory";

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

    public float getFloatParameterValue(String name)
    {
        if(name.equals("targetValue"))return Float.parseFloat(targetValue);
        else if(name.equals("initialValue") && !initialValue.isEmpty())return Float.parseFloat(initialValue);
        return 0;
    }

    public String getStringParameterValue(String name)
    {
        if (name.equals("type")) return type;
        else if(name.equals("targetValue"))return targetValue;
        else if(name.equals("initialValue") && !initialValue.isEmpty())return initialValue;
        return null;
    }

    public boolean specifiesParameter(String name)
    {
        if (name.equals("type")) return true;
        else if(name.equals("targetValue"))return true;
        else if(name.equals("initialValue") && !initialValue.isEmpty())return true;
        return false;
    }
    
    public boolean satisfiesConstraint(String n, String value)
    {
        if (n.equals("type")) return type.equals(value);
        else if(n.equals("targetValue"))return targetValue.equals(value);
        else if(n.equals("initialValue")) return initialValue.equals(value);
        return false;
    }
    
    @Override
    public boolean hasContent()
    {
        return false;
    }
    
    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "type", type);
        if(!initialValue.isEmpty())
        {
            appendAttribute(buf, "initialValue", initialValue);
        }
        appendAttribute(buf, "targetValue", targetValue);
        return super.appendAttributeString(buf);
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        type = getRequiredAttribute("type", attrMap, tokenizer);
        initialValue = getOptionalAttribute("initialValue", attrMap, "");
        targetValue = getRequiredAttribute("targetValue", attrMap, tokenizer);
        super.decodeAttributes(attrMap, tokenizer);
    }

}
