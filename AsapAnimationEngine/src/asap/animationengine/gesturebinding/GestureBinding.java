/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.neurophysics.BiologicalSwivelCostsEvaluator;
import hmi.util.Resources;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import saiba.bml.core.GazeShiftBehaviour;
import saiba.bml.core.PostureShiftBehaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gaze.RestGaze;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.restpose.RestPose;
import asap.binding.SpecParameterDefault;
import asap.hns.Hns;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;

/**
 * The GestureBinding maps from BML to a set of motionunits
 * @author Herwin van Welbergen
 */
@Slf4j
public class GestureBinding extends XMLStructureAdapter
{
    private List<MotionUnitSpec> muSpecs = new ArrayList<>();
    private List<TimedMotionUnitSpec> tmuSpecs = new ArrayList<>();
    private List<RestPoseSpec> restPoseSpecs = new ArrayList<>();
    private List<RestGazeSpec> restGazeSpecs = new ArrayList<>();
    private final Resources resources;
    private final FeedbackManager fbManager;

    public GestureBinding(Resources r, FeedbackManager fbm)
    {
        fbManager = fbm;
        resources = r;
    }

    public static BiologicalSwivelCostsEvaluator constructAutoSwivel(String scope)
    {
        if ("left_arm".equals(scope))
        {
            return new BiologicalSwivelCostsEvaluator(Hns.getMinSwivelLeft(), Hns.getMaxSwivelLeft(),
                    Hns.getSwivelSigmaOfGaussianCostsDistribution(), Hns.getSwivelFreedomOfTheGaussianMeanLeft());
        }
        return new BiologicalSwivelCostsEvaluator(Hns.getMinSwivelRight(), Hns.getMaxSwivelRight(),
                Hns.getSwivelSigmaOfGaussianCostsDistribution(), Hns.getSwivelFreedomOfTheGaussianMeanRight());        
    }   
    
    private boolean hasEqualNameSpace(Behaviour b, String ns)
    {
        if (b.getNamespace() == null && ns == null) return true;
        if (ns == null && b.getNamespace().equals(BMLInfo.BMLNAMESPACE)) return true;
        if (ns == null) return false;
        if (ns.equals(b.getNamespace())) return true;
        return false;
    }

    public RestGaze getRestGaze(GazeShiftBehaviour b, AnimationPlayer player)
    {
        for (RestGazeSpec s : restGazeSpecs)
        {
            if (hasEqualNameSpace(b, s.getSpecnamespace()))
            {
                if (s.satisfiesConstraints(b))
                {
                    RestGaze rg = s.getRestGaze().copy(player);
                    // set default parameter values
                    for (SpecParameterDefault mupc : s.getParameterDefaults())
                    {
                        try
                        {
                            rg.setParameterValue(mupc.name, mupc.value);
                        }
                        catch (ParameterException e)
                        {
                            log.warn("Error setting up restpose", e);
                        }
                    }

                    // map parameters
                    for (String param : s.getParameters())
                    {
                        if (b.specifiesParameter(param))
                        {
                            String value = b.getStringParameterValue(param);
                            try
                            {
                                rg.setParameterValue(s.getParameter(param), value);
                            }
                            catch (ParameterException e)
                            {
                                log.warn("Error setting up restpose", e);
                            }
                        }
                    }
                    return rg;
                }
            }
        }
        return null;
    }
    
    public RestPose getRestPose(PostureShiftBehaviour b, AnimationPlayer player)
    {
        for (RestPoseSpec s : restPoseSpecs)
        {
            if (hasEqualNameSpace(b, s.getSpecnamespace()))
            {
                if (s.satisfiesConstraints(b))
                {
                    RestPose rp = s.getRestPose().copy(player);
                    // set default parameter values
                    for (SpecParameterDefault mupc : s.getParameterDefaults())
                    {
                        try
                        {
                            rp.setParameterValue(mupc.name, mupc.value);
                        }
                        catch (ParameterException e)
                        {
                            log.warn("Error setting up restpose", e);
                        }
                    }

                    // map parameters
                    for (String param : s.getParameters())
                    {
                        if (b.specifiesParameter(param))
                        {
                            String value = b.getStringParameterValue(param);
                            try
                            {
                                rp.setParameterValue(s.getParameter(param), value);
                            }
                            catch (ParameterException e)
                            {
                                log.warn("Error setting up restpose", e);
                            }
                        }
                    }
                    return rp;
                }
            }
        }
        return null;
    }

    public List<TimedAnimationUnit> getMotionUnit(BMLBlockPeg bbPeg, Behaviour b, AnimationPlayer player, PegBoard pegBoard)
    {
        return getMotionUnit(bbPeg, b, player, pegBoard, null);
    }

    /**
     * Gets a list of timed motion units that satisfy the constraints of behaviour b
     */
    public List<TimedAnimationUnit> getMotionUnit(BMLBlockPeg bbPeg, Behaviour b, AnimationPlayer player, PegBoard pegBoard,
            MURMLMUBuilder murmlMUBuilder)
    {
        ArrayList<TimedAnimationUnit> mus = new ArrayList<>();

        for (TimedMotionUnitSpec s : tmuSpecs)
        {
            if (s.getType().equals(b.getXMLTag()) && hasEqualNameSpace(b, s.getSpecnamespace()))
            {
                if (s.satisfiesConstraints(b))
                {
                    if (s.getTimedMotionUnitConstructionInfo().getType().equals("MURML"))
                    {
                        if (murmlMUBuilder == null)
                        {
                            log.warn("Cannot construct MURML tmu: {},\n no murmlMUBuilder provided", s.getTimedMotionUnitConstructionInfo()
                                    .getContent());
                            continue;
                        }

                        TimedAnimationUnit tmu;
                        try
                        {
                            tmu = murmlMUBuilder.setupTMU(s.getTimedMotionUnitConstructionInfo().getContent(), fbManager, bbPeg,
                                    b.getBmlId(), b.id, pegBoard, player);
                        }
                        catch (TMUSetupException e)
                        {
                            log.warn("Cannot construct MURML tmu from content " + s.getTimedMotionUnitConstructionInfo().getContent(), e);
                            continue;
                        }
                        mus.add(tmu);
                    }
                    else
                    {
                        log.warn("Cannot construct TimedMotionUnit of type {}", s.getTimedMotionUnitConstructionInfo().getType());
                    }
                }
            }
        }

        for (MotionUnitSpec s : muSpecs)
        {
            if (s.getType().equals(b.getXMLTag()) && hasEqualNameSpace(b, s.getSpecnamespace()))
            {
                if (!s.satisfiesConstraints(b))
                {
                    log.debug("Constraint mismatch");
                }
                else
                {
                    AnimationUnit muCopy;
                    try
                    {
                        muCopy = s.motionUnit.copy(player);
                    }
                    catch (MUSetupException e1)
                    {
                        log.warn("Error in setting up motion unit", e1);
                        continue;
                    }
                    TimedAnimationMotionUnit tmu = muCopy.createTMU(fbManager, bbPeg, b.getBmlId(), b.id, pegBoard);

                    // set default parameter values
                    for (SpecParameterDefault mupc : s.getParameterDefaults())
                    {
                        try
                        {
                            muCopy.setParameterValue(mupc.name, mupc.value);
                        }
                        catch (ParameterException e)
                        {
                            log.warn("Error in setting default value in getMotionUnit " + mupc, e);
                            // continue;
                        }
                        log.debug("Setting parameter {}  to default {}", mupc.name, mupc.value);
                    }

                    // map parameters
                    for (String param : s.getParameters())
                    {
                        if (b.specifiesParameter(param))
                        {
                            String value = b.getStringParameterValue(param);
                            try
                            {
                                muCopy.setParameterValue(s.getParameter(param), value);
                            }
                            catch (ParameterException e)
                            {
                                log.warn("Error in parameter mapping in getMotionUnit, parameter " + param, e);
                                // continue;
                            }
                            log.debug("Setting parameter {} mapped to {}", param, s.getParameter(param));
                        }
                    }
                    mus.add(tmu);
                }
            }
        }
        return mus;
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(MotionUnitSpec.xmlTag()))
            {
                MotionUnitSpec muSpec = new MotionUnitSpec(resources);
                muSpec.readXML(tokenizer);
                if (muSpec.motionUnit != null) muSpecs.add(muSpec);
                else log.warn("Dropped motion unit spec because we could not construct the motion unit of type {}, constraints {}",
                        muSpec.getType(), muSpec.getConstraints());
            }
            else if (tag.equals(TimedMotionUnitSpec.xmlTag()))
            {
                TimedMotionUnitSpec spec = new TimedMotionUnitSpec();
                spec.readXML(tokenizer);
                tmuSpecs.add(spec);
            }
            else if (tag.equals(RestPoseSpec.xmlTag()))
            {
                RestPoseSpec rpSpec = new RestPoseSpec(resources);
                rpSpec.readXML(tokenizer);
                if (rpSpec.getRestPose() != null)
                {
                    restPoseSpecs.add(rpSpec);
                }
                else
                {
                    log.warn("Dropped RestPose spec " + rpSpec + " constraints " + rpSpec.getConstraints());
                }
            }
            else if (tag.equals(RestGazeSpec.xmlTag()))
            {
                RestGazeSpec rgSpec = new RestGazeSpec();
                rgSpec.readXML(tokenizer);
                if (rgSpec.getRestGaze() != null)
                {
                    restGazeSpecs.add(rgSpec);
                }
                else
                {
                    log.warn("Dropped RestGaze spec " + rgSpec + " constraints " + rgSpec.getConstraints());
                }
            }
            else
            {
                throw new XMLScanException("Invalid tag " + tag + " in gesturebinding");
            }
        }
    }

    /*
     * The XML Stag for XML encoding
     */
    private static final String XMLTAG = "gesturebinding";

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
