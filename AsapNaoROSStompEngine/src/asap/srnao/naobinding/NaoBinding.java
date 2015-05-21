/*******************************************************************************
 *******************************************************************************/
package asap.srnao.naobinding;

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
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.ParameterException;
import asap.srnao.display.PictureDisplay;
import asap.srnao.planunit.NUPrepareException;
import asap.srnao.planunit.NaoUnit;
import asap.srnao.planunit.TimedNaoUnit;

public class NaoBinding extends XMLStructureAdapter
{
    private ArrayList<NaoUnitSpec> specs = new ArrayList<NaoUnitSpec>();
    private Logger logger = LoggerFactory.getLogger(NaoBinding.class.getName());
    private PictureDisplay display = null;

    public NaoBinding(PictureDisplay display)
    {
        this.display = display;
    }

    public List<TimedNaoUnit> getNaoUnit(FeedbackManager fbManager, BMLBlockPeg bbPeg, Behaviour b)
    {
        ArrayList<TimedNaoUnit> tnus = new ArrayList<TimedNaoUnit>();
        for (NaoUnitSpec s : specs)
        {
            if (s.getType().equals(b.getXMLTag())
                    && hasEqualNameSpace(b,s.getSpecnamespace()) )
            {
                if (!s.satisfiesConstraints(b))
                {
                     //System.out.println("Constraint mismatch: "+b.getNamespace()+","+s.getSpecnamespace()+","+b.getXMLTag()+","+s.getType());
                }
                else
                {
                    //System.out.println("Found type and constraint match");
                    NaoUnit nuCopy = s.naoUnit.copy(display);
                    TimedNaoUnit tnu = nuCopy.createTNU(fbManager, bbPeg, b.getBmlId(), b.id);
                    tnus.add(tnu);

                    // System.out.println("set def params");
                    // set default parameter values
                    for (SpecParameterDefault nupc : s.getParameterDefaults())
                    {
                        try
                        {
                            nuCopy.setParameterValue(nupc.name, nupc.value);
                        }
                        catch (ParameterException e)
                        {
                            logger.warn("Error in setting default value in getNaoUnit, parameter " + nupc.name, e);
                        }
                        logger.debug("Setting parameter {} to default {}", nupc.name, nupc.value);
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
                                nuCopy.setParameterValue(s.getParameter(param), value);
                            }
                            catch (ParameterException e)
                            {
                                logger.warn("Error in parameter mapping in getPictureUnit, parameter " + param, e);
                            }
                            logger.debug("Setting parameter {} mapped to  {}", param, s.getParameter(param));
                        }
                    }
                    // TODO
                    // after initting the params, preload the image in the PictureDisplay...
                    logger.debug("prepareimages should not have been needed --why does the deep copy not set the proper images?");

                    // If preparation fails, drop this PU
                    try
                    {
                        nuCopy.prepareImages();
                    }
                    catch (NUPrepareException e)
                    {
                        logger.error(e.getMessage());
                        tnus.remove(tnu);
                    }
                }
            }
        }
        return tnus;

    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(NaoUnitSpec.xmlTag()))
            {
                NaoUnitSpec puSpec = new NaoUnitSpec();
                puSpec.readXML(tokenizer);
                if (puSpec.naoUnit != null) specs.add(puSpec); // don't add failed picture units to the binding
                else logger.warn("Dropped picture unit spec because we could not construct the picture unit");
                // println(null) causes error in Android
                // System.out.println(puSpec.getSpecnamespace());
            }
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "naobinding";

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
    private boolean hasEqualNameSpace(Behaviour b, String ns)
    {
        if(b.getNamespace() == null && ns == null) return true;
        if(ns==null && b.getNamespace().equals(BMLInfo.BMLNAMESPACE))return true;
        if(ns==null)return false;
        if(ns.equals(b.getNamespace()))return true;
        return false;
    }
 
}
