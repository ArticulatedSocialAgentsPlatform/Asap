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

import java.util.*;

import hmi.animation.SkeletonInterpolator;
import hmi.physics.controller.*;
import hmi.util.Resources;
import hmi.xml.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.animationengine.controller.CompoundController;
import asap.animationengine.controller.ControllerMU;
import asap.animationengine.keyframe.KeyframeMU;
import asap.animationengine.motionunit.*;
import asap.animationengine.procanimation.*;
import asap.animationengine.transitions.TransitionMU;


class MotionUnitAssembler extends XMLStructureAdapter
{
    private static Logger logger = LoggerFactory
            .getLogger(MotionUnitAssembler.class.getName());
              
    private Resources resources;

    private MotionUnit motionUnit = null;

    public MotionUnitAssembler(Resources r)
    {
        resources = r;
    }

    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        String type = getRequiredAttribute("type", attrMap, tokenizer);
        String file = getOptionalAttribute("file", attrMap, null);
        String className = getOptionalAttribute("class", attrMap, null);

        if (type.equals("ProcAnimation"))
        {
            ProcAnimationMU p = new ProcAnimationMU();
            if (file != null)
            {
                try
                {
                    p.readXML(resources.getReader(file));
                    motionUnit = p;
                }
                catch (Exception e)
                {
                    motionUnit = null;
                    logger.warn("Cannot read ProcAnimation from file \"{}\"", file);
                    logger.warn("Exception: ", e);
                }
                
            }
        }
        else if (type.equals("CompoundController"))
        {
            CompoundController cc = new CompoundController();
            if (file != null)
            {
                try
                {
                    cc.readXML(resources.getReader(file));
                    ControllerMU p = new ControllerMU(cc, null);
                    motionUnit = p;
                }
                catch (Exception e)
                {
                    motionUnit = null;
                    logger.warn("Cannot read CompoundController from file \"{}\"", file);
                    logger.warn("Exception: ", e);
                }
            }
        }
        else if (type.equals("Keyframe"))
        {
            SkeletonInterpolator si;
            try
            {
                si = new SkeletonInterpolator(new XMLTokenizer(resources.getReader(file)));
                motionUnit = new KeyframeMU(si);
            }
            catch (Exception e)
            {
                motionUnit = null;
                logger.warn("Cannot read KeyFrame animation from file \"{}\"", file);
                logger.warn("Exception: ", e);
            }
        }
        else if (type.equals("PhysicalController"))
        {
            PhysicalController pc = null;
            if (className != null)
            {
                try
                {
                    pc = (PhysicalController) (Class.forName(className).newInstance());
                    ControllerMU p = new ControllerMU(pc, null);
                    motionUnit = p;
                }
                catch (InstantiationException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate PhysicalController \"{}\"", className);
                    logger.warn("Exception: ", e);
                }
                catch (IllegalAccessException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate PhysicalController \"{}\"", className);
                    logger.warn("Exception: ", e);
                }
                catch (ClassNotFoundException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate PhysicalController \"{}\"", className);
                    logger.warn("Exception: ", e);
                }
            }
        }
        else if (type.equals("Gesture"))
        {
            if (className != null)
            {
                try
                {
                    GestureUnit gu = (GestureUnit)(Class.forName(className).newInstance());
                    gu.setResource(resources);
                    motionUnit = gu;
                }
                catch (InstantiationException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate GestureUnit \"{}\"", className);
                    logger.warn("Exception: ", e);
                }
                catch (IllegalAccessException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate GestureUnit \"{}\"", className);
                    logger.warn("Exception: ", e);
                }
                catch (ClassNotFoundException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate GestureUnit \"{}\"", className);
                    logger.warn("Exception: ", e);
                }
            }
        }
        else if (type.equals("Transition"))
        {
            TransitionMU t = null;
            if (className != null)
            {
                try
                {
                    t = (TransitionMU) (Class.forName(className).newInstance());
                    motionUnit = t;
                }
                catch (InstantiationException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate Transition \"{}\"", className);
                    logger.warn("Exception: ", e);
                }
                catch (IllegalAccessException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate Transition \"{}\"", className);
                    logger.warn("Exception: ", e);
                }
                catch (ClassNotFoundException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate Transition \"{}\"", className);
                    logger.warn("Exception: ", e);
                }
            }
            
        }
        else if (type.equals("class"))
        {
            if (className != null)
            {
                Class<?> muClass;
                try
                {
                    muClass = Class.forName(className);
                }
                catch (ClassNotFoundException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate MotionUnit \"{}\"", className);
                    logger.warn("Exception: ", e);
                    return;
                }
                if (!MotionUnit.class.isAssignableFrom(muClass))
                {
                    motionUnit = null;
                    logger.warn("{} does not implement the MotionUnit interface", className);
                    return;
                }
                
                try
                {
                    motionUnit = (MotionUnit) (muClass.newInstance());
                }
                catch (InstantiationException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate MotionUnit \"{}\"", className);
                    logger.warn("Exception: ", e);

                }
                catch (IllegalAccessException e)
                {
                    motionUnit = null;
                    logger.warn("Cannot instantiate MotionUnit \"{}\"", className);
                    logger.warn("Exception: ", e);
                }                
            }
        }
    }

    /**
     * @return the motionUnit, null if not set up/invalid
     */
    public MotionUnit getMotionUnit()
    {
        return motionUnit;
    }

   /*
    * The XML Stag for XML encoding
    */
   private static final String XMLTAG = "MotionUnit";
 
   /**
    * The XML Stag for XML encoding -- use this static method when you want to see if a given String equals
    * the xml tag for this class
    */
   public static String xmlTag() { return XMLTAG; }
 
   /**
    * The XML Stag for XML encoding -- use this method to find out the run-time xml tag of an object
    */
   @Override
   public String getXMLTag() {
      return XMLTAG;
   }
}
