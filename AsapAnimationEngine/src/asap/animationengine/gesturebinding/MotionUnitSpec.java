/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.Behaviour;
import asap.animationengine.motionunit.AnimationUnit;
import asap.binding.SpecConstraints;
import asap.binding.SpecParameterDefault;
import asap.binding.SpecParameterDefaults;
import asap.binding.SpecParameterMap;

/**
 * XML parser for the MotionUnitSpec in a gesturebinding
 * @author Herwin
 * 
 */
@Slf4j
class MotionUnitSpec extends XMLStructureAdapter
{
    public AnimationUnit motionUnit;
    
    @Getter
    private String type;
    
    @Getter
    private String specnamespace;

    @Getter
    private SpecConstraints constraints = new SpecConstraints();
    private SpecParameterMap parameterMap = new SpecParameterMap();
    private SpecParameterDefaults parameterdefaults = new SpecParameterDefaults();

    private final Resources resources;

    public boolean satisfiesConstraints(Behaviour b)
    {
        return constraints.satisfiesConstraints(b);
    }

    public Set<String> getParameters()
    {
        return parameterMap.getParameters();
    }

    public MotionUnitSpec(Resources r)
    {
        resources = r;
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
        type = getRequiredAttribute("type", attrMap, tokenizer);
        specnamespace = getOptionalAttribute("namespace", attrMap, null);
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        String tag = "";
        try
        {
            while (tokenizer.atSTag())
            {
                tag = tokenizer.getTagName();
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
                else if (tag.equals(MotionUnitAssembler.xmlTag()))
                {
                    MotionUnitAssembler mua = new MotionUnitAssembler(resources);
                    mua.readXML(tokenizer);
                    motionUnit = mua.getMotionUnit();
                }
            }
        }
        catch (RuntimeException ex)
        {
            log.warn("Cannot read motion unit spec, dropping element from gesture binding. Tag: {} {}", tag);
            motionUnit = null;
        }
    }

    private static final String XMLTAG = "MotionUnitSpec";

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
