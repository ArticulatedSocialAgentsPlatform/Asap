/*******************************************************************************
 *******************************************************************************/
package asap.picture.picturebinding;

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
import asap.picture.display.PictureDisplay;
import asap.picture.planunit.PUPrepareException;
import asap.picture.planunit.PictureUnit;
import asap.picture.planunit.TimedPictureUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.ParameterException;

public class PictureBinding extends XMLStructureAdapter
{
    private ArrayList<PictureUnitSpec> specs = new ArrayList<PictureUnitSpec>();
    private Logger logger = LoggerFactory.getLogger(PictureBinding.class.getName());
    private PictureDisplay display = null;

    public PictureBinding(PictureDisplay display)
    {
        this.display = display;
    }

    public List<TimedPictureUnit> getPictureUnit(FeedbackManager fbManager, BMLBlockPeg bbPeg, Behaviour b)
    {
        ArrayList<TimedPictureUnit> tpus = new ArrayList<TimedPictureUnit>();
        for (PictureUnitSpec s : specs)
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
                    PictureUnit puCopy = s.pictureUnit.copy(display);
                    TimedPictureUnit tpu = puCopy.createTPU(fbManager, bbPeg, b.getBmlId(), b.id);
                    tpus.add(tpu);

                    // System.out.println("set def params");
                    // set default parameter values
                    for (SpecParameterDefault pupc : s.getParameterDefaults())
                    {
                        try
                        {
                            puCopy.setParameterValue(pupc.name, pupc.value);
                        }
                        catch (ParameterException e)
                        {
                            logger.warn("Error in setting default value in getPictureUnit, parameter " + pupc.name, e);
                        }
                        logger.debug("Setting parameter {} to default {}", pupc.name, pupc.value);
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
                                puCopy.setParameterValue(s.getParameter(param), value);
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
                        puCopy.prepareImages();
                    }
                    catch (PUPrepareException e)
                    {
                        logger.error(e.getMessage());
                        tpus.remove(tpu);
                    }
                }
            }
        }
        return tpus;

    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(PictureUnitSpec.xmlTag()))
            {
                PictureUnitSpec puSpec = new PictureUnitSpec();
                puSpec.readXML(tokenizer);
                if (puSpec.pictureUnit != null) specs.add(puSpec); // don't add failed picture units to the binding
                else logger.warn("Dropped picture unit spec because we could not construct the picture unit");
                // println(null) causes error in Android
                // System.out.println(puSpec.getSpecnamespace());
            }
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "picturebinding";

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
