/*******************************************************************************
 *******************************************************************************/
package asap.nao.bml;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

/**
 * Does something
 * @author Robin ten Buuren
 */
public class NaoDoeIetsBehaviour extends NaoBehaviour
{
    public NaoDoeIetsBehaviour(String bmlId, XMLTokenizer tokenizer) throws IOException
    {
        super(bmlId);
        readXML(tokenizer);
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "doeiets";

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

    @Override
    public String getStringParameterValue(String arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean specifiesParameter(String arg0)
    {
        // TODO Auto-generated method stub
        return false;
    }
}
