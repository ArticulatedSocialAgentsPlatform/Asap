/*******************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.util.*;
import hmi.xml.*;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.*;
import asap.animationengine.restpose.RestPose;
import asap.binding.SpecParameterDefault;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import saiba.bml.core.PostureShiftBehaviour;

/**
 * The GestureBinding maps from BML to a set of motionunits
 * @author Herwin van Welbergen
 */
public class GestureBinding extends XMLStructureAdapter
{
    private ArrayList<MotionUnitSpec> specs = new ArrayList<MotionUnitSpec>();
    private ArrayList<RestPoseSpec> restPoseSpecs = new ArrayList<RestPoseSpec>();
    private final Resources resources;
    private final Logger logger = LoggerFactory.getLogger(GestureBinding.class.getName());
    private final FeedbackManager fbManager;

    public GestureBinding(Resources r, FeedbackManager fbm)
    {
        fbManager = fbm;
        resources = r;
    }
    
    private boolean hasEqualNameSpace(Behaviour b, String ns)
    {
        if(b.getNamespace() == null && ns == null) return true;
        if(ns==null && b.getNamespace().equals(BMLInfo.BMLNAMESPACE))return true;
        if(ns==null)return false;
        if(ns.equals(b.getNamespace()))return true;
        return false;
    }
    
    public RestPose getRestPose(PostureShiftBehaviour b, AnimationPlayer player)
    {
        for (RestPoseSpec s : restPoseSpecs)
        {
            if (hasEqualNameSpace(b,s.getSpecnamespace()))
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
                            logger.warn("Error setting up restpose", e);                            
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
                                logger.warn("Error setting up restpose", e);
                            }
                        }
                    }
                    return rp;
                }
            }
        }
        return null;
    }

    /**
     * Gets a list of timed motion units that satisfy the constraints of behaviour b
     */
    public List<TimedAnimationUnit> getMotionUnit(BMLBlockPeg bbPeg, Behaviour b, AnimationPlayer player, PegBoard pegBoard)
    {
        ArrayList<TimedAnimationUnit> mus = new ArrayList<TimedAnimationUnit>();
        for (MotionUnitSpec s : specs)
        {
            if (s.getType().equals(b.getXMLTag())
                    && hasEqualNameSpace(b,s.getSpecnamespace()))
            {
                if (!s.satisfiesConstraints(b))
                {
                    logger.debug("Constraint mismatch");
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
                        logger.warn("Error in setting up motion unit", e1);
                        continue;
                    }
                    TimedAnimationUnit tmu = muCopy.createTMU(fbManager, bbPeg, b.getBmlId(), b.id, pegBoard);

                    // set default parameter values
                    for (SpecParameterDefault mupc : s.getParameterDefaults())
                    {
                        try
                        {
                            muCopy.setParameterValue(mupc.name, mupc.value);
                        }
                        catch (ParameterException e)
                        {
                            logger.warn("Error in setting default value in getMotionUnit " + mupc, e);
                            // continue;
                        }
                        logger.debug("Setting parameter {}  to default {}", mupc.name, mupc.value);
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
                                logger.warn("Error in parameter mapping in getMotionUnit, parameter " + param, e);
                                // continue;
                            }
                            logger.debug("Setting parameter {} mapped to {}", param, s.getParameter(param));
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
                if (muSpec.motionUnit != null) specs.add(muSpec);
                else logger.warn("Dropped motion unit spec because we could not construct the motion unit of type {}, constraints {}",
                        muSpec.getType(), muSpec.getConstraints());
            }
            else if (tag.equals(RestPoseSpec.xmlTag()))
            {
                RestPoseSpec rpSpec = new RestPoseSpec(resources);
                rpSpec.readXML(tokenizer);
                if(rpSpec.getRestPose()!=null)
                {
                    restPoseSpecs.add(rpSpec);
                }
                else
                {
                    logger.warn("Dropped RestPose spec "+rpSpec+" constraints "+rpSpec.getConstraints());
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
