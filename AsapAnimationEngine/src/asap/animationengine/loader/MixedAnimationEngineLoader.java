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
import hmi.animationembodiments.MixedSkeletonEmbodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.math.Quat4f;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.mixedanimationenvironment.MixedAnimationPlayer;
import hmi.mixedanimationenvironment.MixedAnimationPlayerManager;
import hmi.physicsembodiments.PhysicalEmbodiment;
import hmi.util.Resources;
import hmi.worldobjectenvironment.WorldObjectEnvironment;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import asap.animationengine.AnimationPlanPlayer;
import asap.animationengine.AnimationPlanner;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gesturebinding.GestureBinding;
import asap.animationengine.gesturebinding.HnsHandshape;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.restpose.SkeletonPoseRestPose;
import asap.hns.Hns;
import asap.realizer.DefaultEngine;
import asap.realizer.Engine;
import asap.realizer.planunit.DefaultTimedPlanUnitPlayer;
import asap.realizer.planunit.PlanManager;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.EngineLoader;

/**
 * Loads the Asap AnimationEngine
 */
public class MixedAnimationEngineLoader implements EngineLoader
{

    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private MixedSkeletonEmbodiment mse = null;
    private PhysicalEmbodiment pe = null;
    private MixedAnimationEnvironment mae = null;

    private Engine engine = null;
    private PlanManager<TimedAnimationUnit> animationPlanManager = null;
    private MixedAnimationPlayer animationPlayer = null;
    private AnimationPlanner animationPlanner = null;
    private SkeletonPose restpose;
    private Hns hns = new Hns();
    private HnsHandshape hnsHandshape = new HnsHandshape(hns);
    private List<String> handShapeDir = new ArrayList<>();
    
    String id = "";
    // some variables cached during loading
    GestureBinding gesturebinding = null;

    private AsapRealizerEmbodiment are = null;
    private WorldObjectEnvironment we = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        for (Environment e : environments)
        {
            if (e instanceof MixedAnimationEnvironment) mae = (MixedAnimationEnvironment) e;
            if (e instanceof WorldObjectEnvironment) we = (WorldObjectEnvironment) e;
        }
        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof MixedSkeletonEmbodiment)
            {
                mse = (MixedSkeletonEmbodiment) ((EmbodimentLoader) e).getEmbodiment();
            }
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof PhysicalEmbodiment)
            {
                pe = (PhysicalEmbodiment) ((EmbodimentLoader) e).getEmbodiment();
            }
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof AsapRealizerEmbodiment) are = (AsapRealizerEmbodiment) ((EmbodimentLoader) e)
                    .getEmbodiment();
        }
        if (are == null)
        {
            throw new RuntimeException("MixedAnimationEngineLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        if (we == null)
        {
            throw new RuntimeException("MixedAnimationEngineLoader requires an WorldObjectEnvironment");
        }
        if (mae == null)
        {
            throw new RuntimeException("AnimationEngineLoader requires an Environment of type MixedAnimationEnvironment");
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
        // engine.shutdown();already done in scheduler...
        mae.removeAnimationPlayer(animationPlayer, mse.getCurrentVJoint(), mse.getAnimationVJoint());
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("GestureBinding"))
        {
            attrMap = tokenizer.getAttributes();
            gesturebinding = new GestureBinding(new Resources(adapter.getOptionalAttribute("basedir", attrMap, "")),
                    are.getFeedbackManager());
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
                restpose = new SkeletonPose(new XMLTokenizer(res.getReader(adapter.getRequiredAttribute("filename", attrMap, tokenizer))));
                VJoint vjNull[] = new VJoint[0];
                restpose.setTargets(mse.getNextVJoint().getParts().toArray(vjNull));
                restpose.setToTarget();
                mse.getNextVJoint().calculateMatrices();
            }
            catch (RuntimeException e)
            {
                throw tokenizer.getXMLScanException("Cannot load start pose ");
            }
            tokenizer.takeSTag("StartPose");
            tokenizer.takeETag("StartPose");
        }
        else if (tokenizer.atSTag("Hns"))
        {
            attrMap = tokenizer.getAttributes();
            Resources res = new Resources(adapter.getOptionalAttribute("resources", attrMap, ""));
            hns.readXML(res.getReader(adapter.getRequiredAttribute("filename", attrMap,tokenizer)));
            tokenizer.takeSTag("Hns");            
            tokenizer.takeETag("Hns");
        }
        else if (tokenizer.atSTag("HnsHandShape"))
        {
            attrMap = tokenizer.getAttributes();
            handShapeDir.add(adapter.getRequiredAttribute("dir",attrMap, tokenizer));
            tokenizer.takeSTag("HnsHandShape");            
            tokenizer.takeETag("HnsHandShape");
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
        animationPlanManager = new PlanManager<TimedAnimationUnit>();

        RestPose pose;
        if (restpose != null)
        {
            pose = new SkeletonPoseRestPose(restpose);
        }
        else
        {
            pose = new SkeletonPoseRestPose();
        }
        AnimationPlanPlayer animationPlanPlayer = new AnimationPlanPlayer(pose, are.getFeedbackManager(), animationPlanManager,
                new DefaultTimedPlanUnitPlayer(), are.getPegBoard());

        // public AnimationPlayer(VJoint vP, VJoint vC, VJoint vN, ArrayList<MixedSystem> m, float h, WorldObjectManager wom,
        // PlanPlayer planPlayer)

        animationPlayer = new AnimationPlayer(mse.getPreviousVJoint(), mse.getCurrentVJoint(), mse.getNextVJoint(), pe.getMixedSystems(),
                MixedAnimationPlayerManager.getH(), we.getWorldObjectManager(), animationPlanPlayer);

        pose.setAnimationPlayer((AnimationPlayer) animationPlayer);
        
        try
        {
            hnsHandshape = new HnsHandshape(hns, this.handShapeDir.toArray(new String[0]));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        // make planner
        animationPlanner = new AnimationPlanner(are.getFeedbackManager(), (AnimationPlayer) animationPlayer, gesturebinding, hns, hnsHandshape,
                animationPlanManager, are.getPegBoard());

        engine = new DefaultEngine<TimedAnimationUnit>(animationPlanner, (AnimationPlayer) animationPlayer, animationPlanManager);
        engine.setId(id);

        // propagate avatar resetpose into the animation player, vnext etc, ikbodies,
        // phumans... and also as reset poses for same.
        ((AnimationPlayer) animationPlayer).setResetPose();

        /**
         * then, after the avatar has been set in the right position, glue the feet to the floor to
         * facilitate balancing. Requires the physicalembodiment!!!
         */
        pe.glueFeetToFloor();

        // add engine to realizer;
        

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
        return (AnimationPlayer) animationPlayer;
    }

    public AnimationPlanner getAnimationPlanner()
    {
        return animationPlanner;
    }

    public PlanManager<TimedAnimationUnit> getPlanManager()
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
