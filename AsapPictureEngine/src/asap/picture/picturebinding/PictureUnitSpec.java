/*******************************************************************************
 *******************************************************************************/
package asap.picture.picturebinding;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import saiba.bml.core.Behaviour;
import asap.binding.SpecConstraints;
import asap.binding.SpecParameterDefault;
import asap.binding.SpecParameterDefaults;
import asap.binding.SpecParameterMap;
import asap.picture.planunit.PictureUnit;

/**
 * XML parser for the PictureUnitSpec in a picturebinding
 * @author Herwin
 */
public class PictureUnitSpec extends XMLStructureAdapter
{
    public PictureUnit pictureUnit;
    private String type;
    private String specnamespace;
    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @return the specnamespace
     */
    public String getSpecnamespace()
    {
        return specnamespace;
    }

    private SpecConstraints constraints = new SpecConstraints();
    private SpecParameterMap parametermap = new SpecParameterMap();
    private SpecParameterDefaults parameterdefaults = new SpecParameterDefaults();

    public boolean satisfiesConstraints(Behaviour b)
    {
        return constraints.satisfiesConstraints(b);        
    }

    public Set<String> getParameters()
    {
        return parametermap.getParameters();
    }    

    
    /**
     * Get motion unit parameter for BML parameter src
     */
    public String getParameter(String src)
    {
        return parametermap.getParameter(src);
    }

    /**
     * Get motion unit parameter for BML parameter src
     */
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
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(SpecConstraints.xmlTag()))
            {
                constraints.readXML(tokenizer);                                
            }
            else if (tag.equals(SpecParameterMap.xmlTag()))
            {
                parametermap.readXML(tokenizer);
            }
            else if (tag.equals(SpecParameterDefaults.xmlTag()))
            {
                parameterdefaults.readXML(tokenizer);
            }
            else if (tag.equals(PictureUnitAssembler.xmlTag()))
            {
                PictureUnitAssembler pua = new PictureUnitAssembler();
                pua.readXML(tokenizer);
                pictureUnit = pua.getPictureUnit();
            }
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "PictureUnitSpec";

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
