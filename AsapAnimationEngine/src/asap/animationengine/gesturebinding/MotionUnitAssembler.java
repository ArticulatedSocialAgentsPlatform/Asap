/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.animation.SkeletonInterpolator;
import hmi.physics.controller.PhysicalController;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.controller.CompoundController;
import asap.animationengine.controller.ControllerMU;
import asap.animationengine.keyframe.KeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.procanimation.GestureUnit;
import asap.animationengine.procanimation.ProcAnimationMU;
import asap.animationengine.transitions.TransitionMU;

/**
 * Creates a motion unit from an XML description. For example:<br>
 * &lt;MotionUnit type="PhysicalController" class="hmi.physics.controller.BalanceController"/&gt;<br>
 * &lt;MotionUnit type="ProcAnimation" file="Humanoids/shared/procanimation/breathe_clavicular.xml"/&gt;
 * @author welberge
 */
@Slf4j
class MotionUnitAssembler extends XMLStructureAdapter
{
    private Resources resources;

    private AnimationUnit motionUnit = null;

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
                    log.warn("Cannot read ProcAnimation from file \"{}\"", file);
                    log.warn("Exception: ", e);
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
                    log.warn("Cannot read CompoundController from file \"{}\"", file);
                    log.warn("Exception: ", e);
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
                log.warn("Cannot read KeyFrame animation from file \"{}\"", file);
                log.warn("Exception: ", e);
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
                    log.warn("Cannot instantiate PhysicalController \"{}\"", className);
                    log.warn("Exception: ", e);
                }
                catch (IllegalAccessException e)
                {
                    motionUnit = null;
                    log.warn("Cannot instantiate PhysicalController \"{}\"", className);
                    log.warn("Exception: ", e);
                }
                catch (ClassNotFoundException e)
                {
                    motionUnit = null;
                    log.warn("Cannot instantiate PhysicalController \"{}\"", className);
                    log.warn("Exception: ", e);
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
                    log.warn("Cannot instantiate GestureUnit \"{}\"", className);
                    log.warn("Exception: ", e);
                }
                catch (IllegalAccessException e)
                {
                    motionUnit = null;
                    log.warn("Cannot instantiate GestureUnit \"{}\"", className);
                    log.warn("Exception: ", e);
                }
                catch (ClassNotFoundException e)
                {
                    motionUnit = null;
                    log.warn("Cannot instantiate GestureUnit \"{}\"", className);
                    log.warn("Exception: ", e);
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
                    log.warn("Cannot instantiate Transition \"{}\"", className);
                    log.warn("Exception: ", e);
                }
                catch (IllegalAccessException e)
                {
                    motionUnit = null;
                    log.warn("Cannot instantiate Transition \"{}\"", className);
                    log.warn("Exception: ", e);
                }
                catch (ClassNotFoundException e)
                {
                    motionUnit = null;
                    log.warn("Cannot instantiate Transition \"{}\"", className);
                    log.warn("Exception: ", e);
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
                    log.warn("Cannot instantiate MotionUnit \"{}\"", className);
                    log.warn("Exception: ", e);
                    return;
                }
                if (!AnimationUnit.class.isAssignableFrom(muClass))
                {
                    motionUnit = null;
                    log.warn("{} does not implement the MotionUnit interface", className);
                    return;
                }
                
                try
                {
                    motionUnit = (AnimationUnit) (muClass.newInstance());
                }
                catch (InstantiationException e)
                {
                    motionUnit = null;
                    log.warn("Cannot instantiate MotionUnit \"{}\"", className);
                    log.warn("Exception: ", e);

                }
                catch (IllegalAccessException e)
                {
                    motionUnit = null;
                    log.warn("Cannot instantiate MotionUnit \"{}\"", className);
                    log.warn("Exception: ", e);
                }                
            }
        }
    }

    /**
     * @return the motionUnit, null if not set up/invalid
     */
    public AnimationUnit getMotionUnit()
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
