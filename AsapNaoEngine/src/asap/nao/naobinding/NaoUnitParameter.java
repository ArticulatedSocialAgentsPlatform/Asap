/*******************************************************************************
 *******************************************************************************/
package asap.nao.naobinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

public class NaoUnitParameter extends XMLStructureAdapter
{
    public String filename;
    public String text;

    /**
     * Decode the parameters in the BML. There are two types: the text the Nao should say and the name of the file the Nao should play
     */

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {

        filename = getOptionalAttribute("filename", attrMap);
        text = getOptionalAttribute("text", attrMap);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "parameter";

    /**
     * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
     * the xml tag for this class
     */
    public static String xmlTag()
    {
        return XMLTAG;
    }

    /**
     * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
     */
    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }

}
