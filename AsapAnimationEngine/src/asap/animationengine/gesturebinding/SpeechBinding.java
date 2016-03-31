/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.binding.SpecParameterDefault;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;

/**
 * The SpeechBinding maps from visemes to TimedMotionUnits (e.g. jaw movement).
 * @author welberge
 */
public class SpeechBinding extends XMLStructureAdapter
{
    private Map<Integer, VisimeSpec> specs = new HashMap<Integer, VisimeSpec>();
    private final static Logger logger = LoggerFactory.getLogger(SpeechBinding.class.getName());
    private final Resources resources;

    public SpeechBinding(Resources r)
    {
        resources = r;
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(VisimeSpec.xmlTag()))
            {
                VisimeSpec viSpec = new VisimeSpec(resources);
                viSpec.readXML(tokenizer);
                if (viSpec.getMotionUnit() != null) specs.put(viSpec.getVisime(), viSpec);
                else logger.warn("Dropped motion unit spec because we could not construct the motion unit");
            }
        }
    }

    public TimedAnimationMotionUnit getMotionUnit(int visime, BMLBlockPeg bbPeg, String bmlId, String id, AnimationPlayer player, PegBoard pegBoard)
            throws MUSetupException
    {
        return getMotionUnit(NullFeedbackManager.getInstance(), visime, bbPeg, bmlId, id, player, pegBoard);
    }

    public TimedAnimationMotionUnit getMotionUnit(FeedbackManager fbm, int visime, BMLBlockPeg bbPeg, String bmlId, String id,
            AnimationPlayer player, PegBoard pegBoard) throws MUSetupException
    {
        VisimeSpec viSpec = specs.get(visime);
        if (viSpec != null)
        {
            AnimationUnit mu = viSpec.getMotionUnit();
            AnimationUnit muCopy = mu.copy(player);
            TimedAnimationMotionUnit tmu = muCopy.createTMU(fbm, bbPeg, bmlId, id, pegBoard);

            // set default parameter values
            for (SpecParameterDefault mupc : viSpec.getParameterDefaults())
            {
                try
                {
                    muCopy.setParameterValue(mupc.name, mupc.value);
                }
                catch (ParameterException e)
                {
                    logger.warn("ParameterException in " + mu, e);
                    return null;
                }
                logger.debug("Setting parameter {}  to default {}", mupc.name, mupc.value);
            }
            return tmu;
        }
        return null;
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "speechbinding";

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
