/*******************************************************************************
 *******************************************************************************/
package asap.nao.bml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

/**
 * Lets the Nao speak
 * @author Robin ten Buuren
 */
public class NaoSayBehaviour extends NaoBehaviour
{

    private String text;

    /**
     * The actions needs text, so the Nao knows what he should say
     */

    @Override
    public boolean satisfiesConstraint(String n, String value)
    {
        if (n.equals("text") && text.toString().equals(value)) return true;
        return false;
    }

    public NaoSayBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "text", text.toString());
        return super.appendAttributeString(buf);
    }

    @SuppressWarnings("static-access")
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        text = text.valueOf(getRequiredAttribute("text", attrMap, tokenizer));
        super.decodeAttributes(attrMap, tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "naosay";

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
    public float getFloatParameterValue(String arg0)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * The actions needs text, so the Nao knows what he should say
     */

    @Override
    public String getStringParameterValue(String name)
    {
        if (name.equals("text"))
        {
            return text;
        }
        return "";
    }

    /**
     * The actions needs text, so the Nao knows what he should say
     */

    @Override
    public boolean specifiesParameter(String name)
    {
        return name.equals("text");
    }
}
