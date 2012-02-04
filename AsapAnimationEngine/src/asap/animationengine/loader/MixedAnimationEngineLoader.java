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
package asap.animationengine.loader;

import hmi.animation.SkeletonPose;
import hmi.animation.VJoint;
import hmi.elckerlyc.DefaultEngine;
import hmi.elckerlyc.Engine;
import hmi.elckerlyc.planunit.DefaultTimedPlanUnitPlayer;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.math.Quat4f;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.mixedanimationenvironment.MixedAnimationPlayer;
import hmi.mixedanimationenvironment.MixedAnimationPlayerManager;
import hmi.physics.MixedSkeletonEmbodiment;
import hmi.physics.PhysicalEmbodiment;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.animationengine.AnimationPlanPlayer;
import asap.animationengine.AnimationPlanner;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gesturebinding.GestureBinding;
import asap.animationengine.motionunit.TimedMotionUnit;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.restpose.SkeletonPoseRestPose;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.environment.EmbodimentLoader;
import asap.environment.EngineLoader;
import asap.environment.Loader;
import asap.utils.Environment;

/**

*/
public class MixedAnimationEngineLoader implements EngineLoader
{
    
    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private MixedSkeletonEmbodiment mse = null;
    private PhysicalEmbodiment pe = null;
    private MixedAnimationEnvironment mae = null;
    private AsapEnvironment ae = null;

    private Engine engine = null;
    private PlanManager<TimedMotionUnit> animationPlanManager = null;
    private MixedAnimationPlayer animationPlayer = null;
    private AnimationPlanner animationPlanner = null;
    private SkeletonPose restpose;
    
    String id = "";
    // some variables cached during loading
    GestureBinding gesturebinding = null;
    AsapVirtualHuman theVirtualHuman = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, 
            Environment[] environments, Loader... requiredLoaders) throws IOException
    {
        id = newId;
        theVirtualHuman = avh;
        for (Environment e : environments)
        {
            if (e instanceof MixedAnimationEnvironment) mae = (MixedAnimationEnvironment) e;
            if (e instanceof AsapEnvironment) ae = (AsapEnvironment)e;
        }
        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof MixedSkeletonEmbodiment) mse = (MixedSkeletonEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof PhysicalEmbodiment) pe = (PhysicalEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
        }
        if (mae == null)
        {
            throw new RuntimeException("AnimationEngineLoader requires an Environment of type MixedAnimationEnvironment");
        }
        if (ae == null)
        {
            throw new RuntimeException("AnimationEngineLoader requires an Environment of type AsapEnvironment");
        }
        if (mse == null)
        {
            throw new RuntimeException(
                    "AnimationEngineLoader requires an EmbodimentLoader containing a MixedSkeletonEmbodiment (e.g., HmiRenderBodyEmbodiment)");
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
        //engine.shutdown();already done in scheduler...
        mae.removeAnimationPlayer(animationPlayer, mse.getCurrentVJoint(), mse.getAnimationVJoint());
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("GestureBinding"))
        {
            attrMap = tokenizer.getAttributes();
            gesturebinding = new GestureBinding(new Resources(adapter.getOptionalAttribute("basedir", attrMap, "")), theVirtualHuman
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
                restpose.setTargets(mse.getNextVJoint().getParts().toArray(vjNull));
                restpose.setToTarget();
                mse.getNextVJoint().calculateMatrices();
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
            mse.getNextVJoint().translate(startposition);
            mse.getNextVJoint().calculateMatrices();
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
            mse.getNextVJoint().rotate(qRot);
            mse.getNextVJoint().calculateMatrices();
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
            pose = new SkeletonPoseRestPose(restpose, theVirtualHuman.getElckerlycRealizer()
                    .getFeedbackManager());
        }
        else
        {
            pose = new SkeletonPoseRestPose(theVirtualHuman.getElckerlycRealizer()
                    .getFeedbackManager());
        }
        AnimationPlanPlayer animationPlanPlayer = new AnimationPlanPlayer(pose,theVirtualHuman.getElckerlycRealizer()
                .getFeedbackManager(), animationPlanManager, new DefaultTimedPlanUnitPlayer());
        
        //public AnimationPlayer(VJoint vP, VJoint vC, VJoint vN, ArrayList<MixedSystem> m, float h, WorldObjectManager wom,
        //PlanPlayer planPlayer)
        
        animationPlayer = new AnimationPlayer(mse.getPreviousVJoint(), mse.getCurrentVJoint(), mse.getNextVJoint(), 
                pe.getMixedSystems(), MixedAnimationPlayerManager.getH(), ae.getWorldObjectManager(), animationPlanPlayer);

        pose.setAnimationPlayer((AnimationPlayer)animationPlayer);
        // IKBody nextBody = new IKBody(se.getNextVJoint()); not used?

        // make planner
        animationPlanner = new AnimationPlanner(theVirtualHuman.getElckerlycRealizer().getFeedbackManager(), (AnimationPlayer)animationPlayer,
                gesturebinding, animationPlanManager);

        engine = new DefaultEngine<TimedMotionUnit>(animationPlanner, (AnimationPlayer)animationPlayer, animationPlanManager);
        engine.setId(id);

        // propagate avatar resetpose into the animation player, vnext etc, ikbodies,
        // phumans... and also as reset poses for same.
        ((AnimationPlayer)animationPlayer).setResetPose();

        /**
         * then, after the avatar has been set in the right position, glue the feet to the floor to
         * facilitate balancing. Requires the physicalembodiment!!!
         */
        pe.glueFeetToFloor();

        // add engine to realizer;
        theVirtualHuman.getElckerlycRealizer().addEngine(engine);

        // add player to playermanager
        mae.addAnimationPlayer(animationPlayer, mse.getNextVJoint(), mse.getAnimationVJoint());

    }

    /** Return the Engine that was constructed from the XML specification */
    public Engine getEngine()
    {
        return engine;
    }

    public AnimationPlayer getAnimationPlayer()
    {
        return (AnimationPlayer)animationPlayer;
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
