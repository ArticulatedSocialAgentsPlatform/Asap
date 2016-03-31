/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.Getter;
import asap.animationengine.gaze.RestGaze;

/**
 * Creates a RestGaze from an XML description
 * @author welberge
 */
public class RestGazeAssembler extends XMLStructureAdapter
{
    @Getter
    private RestGaze restGaze;
    
    private static final String XMLTAG = "RestGaze";

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        String type = getRequiredAttribute("type", attrMap, tokenizer);
        String className = getOptionalAttribute("class", attrMap, null);
        if(type.equals("class"))
        {
            try
            {
                Class<?> c = Class.forName(className);
                restGaze = c.asSubclass(RestGaze.class).newInstance();                
            }
            catch (ClassNotFoundException e)
            {
                throw new XMLScanException("RestGaze "+className+" not found.", e);
            }
            catch (InstantiationException e)
            {
                throw new XMLScanException("RestGaze "+className+" not instantiated.", e);
            }
            catch (IllegalAccessException e)
            {
                throw new XMLScanException("RestGaze "+className+" illegal access.", e);
            }
        }
    }
    
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
