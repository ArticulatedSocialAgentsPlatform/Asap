/*******************************************************************************
 *******************************************************************************/
package asap.nao.bml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

/**
 * Plays Choregraphe clip
 * @author Robin ten Buuren
 */
public class NaoPlayChoregrapheClipBehaviour extends NaoBehaviour
{

    private String filename;

    /**
     * The actions needs a filename, so the Nao knows which file it should play
     */

    @Override
    public boolean satisfiesConstraint(String n, String value)
    {
        if (n.equals("filename") && filename.toString().equals(value)) return true;
        return false;
    }

    public NaoPlayChoregrapheClipBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    @Override
    public StringBuilder appendAttributeString(StringBuilder buf)
    {
        appendAttribute(buf, "filename", filename.toString());
        return super.appendAttributeString(buf);
    }

    @SuppressWarnings("static-access")
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        filename = filename.valueOf(getRequiredAttribute("filename", attrMap, tokenizer));
        super.decodeAttributes(attrMap, tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "playchoregrapheclip";

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
     * The actions needs a filename, so the Nao knows which file it should play
     */

    @Override
    public String getStringParameterValue(String name)
    {
        if (name.equals("filename"))
        {
            return filename;
        }
        return "";
    }

    /**
     * The actions needs a filename, so the Nao knows which file it should play
     */

    @Override
    public boolean specifiesParameter(String name)
    {
        return name.equals("filename");
    }
}
