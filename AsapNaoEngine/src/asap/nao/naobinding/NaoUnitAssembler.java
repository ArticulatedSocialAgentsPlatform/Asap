/*******************************************************************************
 *******************************************************************************/
package asap.nao.naobinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.nao.planunit.DoeIetsNU;
import asap.nao.planunit.NaoSayNU;
import asap.nao.planunit.NaoUnit;
import asap.nao.planunit.PlayChoregrapheClipNU;

public class NaoUnitAssembler extends XMLStructureAdapter
{
    private static Logger logger = LoggerFactory.getLogger(NaoUnitAssembler.class.getName());

    private NaoUnit naoUnit;

    public NaoUnitAssembler()
    {
    }

    /**
     * Decode the BMl to see, which NaoUnit should be created.
     */

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        String type = getRequiredAttribute("type", attrMap, tokenizer);

        if (type.equals("DoeIets"))
        {
            naoUnit = new DoeIetsNU();
        }
        else if (type.equals("PlayChoregrapheClip"))
        {
            naoUnit = new PlayChoregrapheClipNU();
        }
        else if (type.equals("NaoSay"))
        {
            naoUnit = new NaoSayNU();
        }
        else
        {
            logger.warn("Cannot read NaoUnit type \"{}\" in NaoBinding; omitting this NaoUnit", type);
        }
    }

    /**
     * Returns the NaoUnit
     * @return the naoUnit
     */
    public NaoUnit getNaoUnit()
    {
        return naoUnit;
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "NaoUnit";

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
