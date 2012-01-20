/*******************************************************************************
 * 
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
package asap.animationengine.vhloader;

import java.io.IOException;
import java.util.*;

import hmi.environment.vhloader.*;
import hmi.environment.vhloader.impl.AnimationEnvironment;
import hmi.xml.*;
import hmi.util.*;
import hmi.elckerlyc.*;
import hmi.animation.*;
import asap.animationengine.AnimationPlanPlayer;
import asap.animationengine.AnimationPlanner;
import asap.animationengine.AnimationPlayer;
import hmi.animationengine.AnimationPlayerManager;
import asap.animationengine.gesturebinding.GestureBinding;
import asap.animationengine.motionunit.TimedMotionUnit;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.restpose.SkeletonPoseRestPose;
import hmi.math.*;
import hmi.elckerlyc.planunit.*;

/**

*/
public class AnimationEngineLoader implements EngineLoader
{
    public AnimationEngineLoader()
    {
    }

    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private SkeletonEmbodiment se = null;
    private PhysicalEmbodiment pe = null;
    private AnimationEnvironment ae = null;

    private Engine engine = null;
    private PlanManager<TimedMotionUnit> animationPlanManager = null;
    private AnimationPlayer animationPlayer = null;
    private AnimationPlanner animationPlanner = null;
    private SkeletonPose restpose;
    
    String id = "";
    // some variables cached during loading
    GestureBinding gesturebinding = null;
    ElckerlycRealizerLoader theRealizerLoader = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String newId, ElckerlycVirtualHuman evh, ElckerlycRealizerLoader realizerLoader,
            Environment[] environments, Loader... requiredLoaders) throws IOException
    {
        id = newId;
        theRealizerLoader = realizerLoader;
        for (Environment e : environments)
        {
            if (e instanceof AnimationEnvironment) ae = (AnimationEnvironment) e;
        }
        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof SkeletonEmbodiment) se = (SkeletonEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof PhysicalEmbodiment) pe = (PhysicalEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
        }
        if (ae == null)
        {
            throw new RuntimeException("AnimationEngineLoader requires an Environment of type AnimationEnvironment");
        }
        if (se == null)
        {
            throw new RuntimeException(
                    "AnimationEngineLoader requires an EmbodimentLoader containing a SkeletonEmbodiment (e.g., HmiRenderBodyEmbodiment)");
        }
        if (pe == null)
        {
            throw new RuntimeException("AnimationEngineLoader requires an EmbodimentLoader containing a PhysicalEmbodiment");
        }
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }
        constructEngine(tokenizer);

    }

    @Override
    public void unload()
    {
        // TODO?
        //ae.removeAnimationPlayer(animationPlayer);
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("GestureBinding"))
        {
            attrMap = tokenizer.getAttributes();
            gesturebinding = new GestureBinding(new Resources(adapter.getOptionalAttribute("basedir", attrMap, "")), theRealizerLoader
                    .getElckerlycRealizer().getFeedbackManager());
            try
            {
                gesturebinding.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter
                        .getRequiredAttribute("filename", attrMap, tokenizer)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException("Cannnot load GestureBinding: " + e);
            }
            tokenizer.takeEmptyElement("GestureBinding");
        }
        else if (tokenizer.atSTag("StartPose"))
        {
            attrMap = tokenizer.getAttributes();
            try
            {
                Resources res = new Resources(adapter.getOptionalAttribute("resources", attrMap, ""));
                restpose = new SkeletonPose(new XMLTokenizer(res.getReader(adapter.getRequiredAttribute("filename", attrMap,
                        tokenizer))));
                VJoint vjNull[] = new VJoint[0];
                restpose.setTargets(se.getNextVJoint().getParts().toArray(vjNull));
                restpose.setToTarget();
                se.getNextVJoint().calculateMatrices();
            }
            catch (Exception e)
            {
                throw tokenizer.getXMLScanException("Cannot load start pose ");
            }
            tokenizer.takeSTag("StartPose");
            tokenizer.takeETag("StartPose");
        }
        else if (tokenizer.atSTag("StartPosition"))
        {
            attrMap = tokenizer.getAttributes();
            tokenizer.takeSTag("StartPosition");
            String offsetString = adapter.getRequiredAttribute("offset", attrMap, tokenizer);
            float[] startposition = XMLStructureAdapter.decodeFloatArray(offsetString);
            if (startposition.length != 3) throw tokenizer.getXMLScanException("startposition.offset must containg a 3-float array");
            se.getNextVJoint().translate(startposition);
            se.getNextVJoint().calculateMatrices();
            tokenizer.takeETag("StartPosition");
        }
        else if (tokenizer.atSTag("StartRotation"))
        {
            attrMap = tokenizer.getAttributes();
            tokenizer.takeSTag("StartRotation");
            String rotationString = adapter.getRequiredAttribute("rotation", attrMap, tokenizer);
            float[] startrotation = XMLStructureAdapter.decodeFloatArray(rotationString);
            if (startrotation.length != 4) throw tokenizer.getXMLScanException("startrotation.rotation must containg a 4-float array");
            float[] qRot = new float[4];
            Quat4f.setFromAxisAngle4f(qRot, startrotation);
            se.getNextVJoint().rotate(qRot);
            se.getNextVJoint().calculateMatrices();
            tokenizer.takeETag("StartRotation");
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }
    }

    /** tokenizer used for throwing scanexceptions */
    private void constructEngine(XMLTokenizer tokenizer)
    {
        // startpose etc have been set...
        // gesturebinding exists...
        // now: make animation player and planner and everything; using se and gesturebinding and speechbinding.
        if (gesturebinding == null) throw tokenizer.getXMLScanException("gesturebinding is null, cannot build animation planner ");
        animationPlanManager = new PlanManager<TimedMotionUnit>();
        
        RestPose pose;
        if(restpose!=null)
        {
            pose = new SkeletonPoseRestPose(restpose, theRealizerLoader.getElckerlycRealizer()
                    .getFeedbackManager());
        }
        else
        {
            pose = new SkeletonPoseRestPose(theRealizerLoader.getElckerlycRealizer()
                    .getFeedbackManager());
        }
        PlanPlayer animationPlanPlayer = new AnimationPlanPlayer(pose,theRealizerLoader.getElckerlycRealizer()
                .getFeedbackManager(), animationPlanManager, new DefaultTimedPlanUnitPlayer());
        
        //public AnimationPlayer(VJoint vP, VJoint vC, VJoint vN, ArrayList<MixedSystem> m, float h, WorldObjectManager wom,
        //PlanPlayer planPlayer)
        
        animationPlayer = new AnimationPlayer(se.getPreviousVJoint(), se.getCurrentVJoint(), se.getNextVJoint(), 
                pe.getMixedSystems(), AnimationPlayerManager.getH(), ae.getWorldObjectManager(), animationPlanPlayer);

        pose.setAnimationPlayer(animationPlayer);
        // IKBody nextBody = new IKBody(se.getNextVJoint()); not used?

        // make planner
        animationPlanner = new AnimationPlanner(theRealizerLoader.getElckerlycRealizer().getFeedbackManager(), animationPlayer,
                gesturebinding, animationPlanManager);

        engine = new DefaultEngine<TimedMotionUnit>(animationPlanner, animationPlayer, animationPlanManager);
        engine.setId(id);

        // propagate avatar resetpose into the animation player, vnext etc, ikbodies,
        // phumans... and also as reset poses for same.
        animationPlayer.setResetPose();

        /**
         * then, after the avatar has been set in the right position, glue the feet to the floor to
         * facilitate balancing. Requires the physicalembodiment!!!
         */
        pe.glueFeetToFloor();

        // add engine to realizer;
        theRealizerLoader.getElckerlycRealizer().addEngine(engine);

        // add player to playermanager
        ae.addAnimationPlayer(animationPlayer);

    }

    /** Return the Engine that was constructed from the XML specification */
    public Engine getEngine()
    {
        return engine;
    }

    public AnimationPlayer getAnimationPlayer()
    {
        return animationPlayer;
    }

    public AnimationPlanner getAnimationPlanner()
    {
        return animationPlanner;
    }

    public PlanManager<TimedMotionUnit> getPlanManager()
    {
        return animationPlanManager;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String newId)
    {
        id = newId;
    }
}
