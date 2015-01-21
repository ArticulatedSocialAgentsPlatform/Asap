/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.animation.SkeletonPose;
import hmi.util.Resources;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.animationengine.restpose.RestPose;
import asap.animationengine.restpose.SkeletonPoseRestPose;

/**
 * Creates a RestPose from an XML description
 * @author welberge
 */
public class RestPoseAssembler extends XMLStructureAdapter
{
    private Resources resources;
    private RestPose restPose;
    
    public RestPose getRestPose()
    {
        return restPose;
    }

    public RestPoseAssembler(Resources r)
    {
        resources = r;
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        String type = getRequiredAttribute("type", attrMap, tokenizer);
        String file = getOptionalAttribute("file", attrMap, null);
        String className = getOptionalAttribute("class", attrMap, null);
        if (type.equals("SkeletonPose"))
        {
            SkeletonPose pose;
            try
            {
                pose = new SkeletonPose(new XMLTokenizer(resources.getReader(file)));
            }
            catch (IOException e)
            {
                throw new XMLScanException("Error reading skeletonpose file " + file, e);
            }
            restPose = new SkeletonPoseRestPose(pose);
        }
        else if (type.equals("PhysicalController"))
        {
            try
            {
                Class<?> c = Class.forName(className);
                restPose = c.asSubclass(RestPose.class).newInstance();
                restPose.setResource(resources);
            }
            catch (ClassNotFoundException e)
            {
                throw new XMLScanException("Physical restpose "+className+" not found.", e);
            }
            catch (InstantiationException e)
            {
                throw new XMLScanException("Physical restpose "+className+" not instantiated.", e);
            }
            catch (IllegalAccessException e)
            {
                throw new XMLScanException("Physical restpose "+className+" illegal access.", e);
            }
        }
    }

    private static final String XMLTAG = "RestPose";

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
