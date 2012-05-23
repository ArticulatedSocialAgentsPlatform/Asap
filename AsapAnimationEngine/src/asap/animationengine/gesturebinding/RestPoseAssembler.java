package asap.animationengine.gesturebinding;

import java.io.IOException;
import java.util.HashMap;

import asap.animationengine.restpose.RestPose;
import asap.animationengine.restpose.SkeletonPoseRestPose;
import asap.realizer.pegboard.PegBoard;
import hmi.animation.SkeletonPose;
import hmi.util.Resources;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

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

    private PegBoard pegBoard;

    public RestPoseAssembler(Resources r, PegBoard pb)
    {
        resources = r;
        pegBoard = pb;
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
            restPose = new SkeletonPoseRestPose(pose, pegBoard);
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "RestPose";

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
