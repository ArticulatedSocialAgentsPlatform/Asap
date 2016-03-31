/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.util.Resources;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import lombok.Getter;
import saiba.bml.core.PostureShiftBehaviour;
import asap.animationengine.restpose.RestPose;
import asap.binding.SpecConstraints;
import asap.binding.SpecParameterDefault;
import asap.binding.SpecParameterDefaults;
import asap.binding.SpecParameterMap;

/**
 * XML parser for the RestPoseSpec in a gesturebinding
 * @author welberge
 */
public class RestPoseSpec extends XMLStructureAdapter
{
    @Getter private SpecConstraints constraints = new SpecConstraints();
    private SpecParameterMap parameterMap = new SpecParameterMap();
    private SpecParameterDefaults parameterdefaults = new SpecParameterDefaults();
    
    @Getter
    private RestPose restPose;
    
    
    @Getter
    private String specnamespace;
    private final Resources resources;    

    public RestPoseSpec(Resources r)
    {
        resources = r;        
    }

    public boolean satisfiesConstraints(PostureShiftBehaviour b)
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
            else if (tag.equals(RestPoseAssembler.xmlTag()))
            {
                RestPoseAssembler rpa = new RestPoseAssembler(resources);
                rpa.readXML(tokenizer);
                restPose = rpa.getRestPose();
            }
            else
            {
                throw new XMLScanException("Invalid tag "+tag +" in RestPoseSpec");
            }
        }
    }

    private static final String XMLTAG = "RestPoseSpec";

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
