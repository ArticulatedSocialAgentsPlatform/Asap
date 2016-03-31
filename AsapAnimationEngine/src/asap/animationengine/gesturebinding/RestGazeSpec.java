/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import lombok.Getter;
import saiba.bml.core.GazeShiftBehaviour;
import asap.animationengine.gaze.RestGaze;
import asap.binding.SpecConstraints;
import asap.binding.SpecParameterDefault;
import asap.binding.SpecParameterDefaults;
import asap.binding.SpecParameterMap;

/**
 * Parses restgazespecs
 * @author hvanwelbergen
 * 
 */
public class RestGazeSpec extends XMLStructureAdapter
{
    @Getter
    private SpecConstraints constraints = new SpecConstraints();
    private SpecParameterMap parameterMap = new SpecParameterMap();
    private SpecParameterDefaults parameterdefaults = new SpecParameterDefaults();

    @Getter
    private String specnamespace;

    @Getter
    RestGaze restGaze;

    public boolean satisfiesConstraints(GazeShiftBehaviour b)
    {
        return constraints.satisfiesConstraints(b);
    }

    public Set<String> getParameters()
    {
        return parameterMap.getParameters();
    }

    /**
     * Get motion unit parameter for BML parameter src
     */
    public String getParameter(String src)
    {
        return parameterMap.getParameter(src);
    }

    public Collection<SpecParameterDefault> getParameterDefaults()
    {
        return parameterdefaults.getParameterDefaults();
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        specnamespace = getOptionalAttribute("namespace", attrMap, null);
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(SpecConstraints.xmlTag()))
            {
                constraints.readXML(tokenizer);
            }
            else if (tag.equals(SpecParameterMap.xmlTag()))
            {
                parameterMap.readXML(tokenizer);
            }
            else if (tag.equals(SpecParameterDefaults.xmlTag()))
            {
                parameterdefaults.readXML(tokenizer);
            }
            else if (tag.equals(RestGazeAssembler.xmlTag()))
            {
                RestGazeAssembler rga = new RestGazeAssembler();
                rga.readXML(tokenizer);
                restGaze = rga.getRestGaze();
            }
            else
            {
                throw new XMLScanException("Invalid tag " + tag + " in RestPoseSpec");
            }
        }
    }

    private static final String XMLTAG = "RestGazeSpec";

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
