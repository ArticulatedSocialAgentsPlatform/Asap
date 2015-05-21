/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.facebinding;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import asap.binding.SpecParameterDefault;
import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;

/**
 * The FaceBinding maps from BML to a set of faceunits
 * different avatars have really different FaceBindings, because some avatars only support morphing, or other only FAPs, etc....
 * 
 * @author Dennis Reidsma
 */
public class FaceBinding extends XMLStructureAdapter
{
    private ArrayList<FaceUnitSpec> specs = new ArrayList<FaceUnitSpec>();
    private Logger logger = LoggerFactory.getLogger(FaceBinding.class.getName());

    private boolean hasEqualNameSpace(Behaviour b, String ns)
    {
        if(b.getNamespace() == null && ns == null) return true;
        if(ns==null && b.getNamespace().equals(BMLInfo.BMLNAMESPACE))return true;
        if(ns==null)return false;
        if(ns.equals(b.getNamespace()))return true;
        return false;
    }
    
    /**
     * Gets a list of timed face units that satisfy the constraints of behaviour b
     */
    public List<TimedFaceUnit> getFaceUnit(FeedbackManager fbManager, BMLBlockPeg bbPeg, Behaviour b, FaceController fc,
            FACSConverter fconv, EmotionConverter econv, PegBoard pb)
    {
        ArrayList<TimedFaceUnit> fus = new ArrayList<TimedFaceUnit>();
        // System.out.println("Mapping face binding for "+b.getXMLTag());
        for (FaceUnitSpec s : specs)
        {
            // System.out.println("testing "+s.getType());
            if (s.getType().equals(b.getXMLTag())
                    && hasEqualNameSpace(b,s.getSpecnamespace()))
            {
                if (s.satisfiesConstraints(b))
                {
                    // System.out.println("Found type and constraint match");
                    FaceUnit fuCopy = s.faceUnit.copy(fc, fconv, econv);
                    TimedFaceUnit tfu = fuCopy.createTFU(fbManager, bbPeg, b.getBmlId(), b.id, pb);
                    fus.add(tfu);

                    // System.out.println("set def params");
                    // set default parameter values
                    for (SpecParameterDefault fupc : s.getParameterDefaults())
                    {
                        try
                        {
                            fuCopy.setParameterValue(fupc.name, fupc.value);
                        }
                        catch (ParameterException e)
                        {
                            logger.warn("Error in setting default value in getFaceUnit, parameter " + fupc.name, e);
                        }
                        logger.debug("Setting parameter {} to default {}", fupc.name, fupc.value);
                    }

                    // System.out.println("Map params");
                    // map parameters
                    for (String param : s.getParameters())
                    {
                        if (b.specifiesParameter(param))
                        {
                            String value = b.getStringParameterValue(param);
                            try
                            {
                                fuCopy.setParameterValue(s.getParameter(param), value);
                            }
                            catch (ParameterException e)
                            {
                                logger.warn("Error in parameter mapping in getFaceUnit, parameter " + param, e);
                            }
                            logger.debug("Setting parameter {} mapped to  {}", param, s.getParameter(param));
                        }
                    }
                }
            }
        }
        return fus;

    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(FaceUnitSpec.xmlTag()))
            {
                FaceUnitSpec fuSpec = new FaceUnitSpec();
                fuSpec.readXML(tokenizer);
                if (fuSpec.faceUnit != null) specs.add(fuSpec); // don't add failed face units to the binding
                else logger.warn("Dropped face unit spec because we could not construct the face unit");
            }
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "facebinding";

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
