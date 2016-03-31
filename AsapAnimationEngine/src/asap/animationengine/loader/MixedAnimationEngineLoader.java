/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.loader;

import hmi.animationembodiments.MixedSkeletonEmbodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.math.Quat4f;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsembodiments.PhysicalEmbodiment;
import hmi.util.ArrayUtils;
import hmi.util.Resources;
import hmi.worldobjectenvironment.WorldObjectEnvironment;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import asap.animationengine.AnimationPlanPlayer;
import asap.animationengine.AnimationPlanner;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gaze.ForwardRestGaze;
import asap.animationengine.gaze.GazeInfluence;
import asap.animationengine.gaze.RestGaze;
import asap.animationengine.gesturebinding.GestureBinding;
import asap.animationengine.gesturebinding.HnsHandshape;
import asap.animationengine.gesturebinding.RestPoseAssembler;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.restpose.SkeletonPoseRestPose;
import asap.hns.Hns;
import asap.realizer.DefaultEngine;
import asap.realizer.Engine;
import asap.realizer.planunit.DefaultTimedPlanUnitPlayer;
import asap.realizer.planunit.ParameterException;
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
    private AnimationPlayer animationPlayer = null;
    private AnimationPlanner animationPlanner = null;
    private Hns hns = new Hns();
    private HnsHandshape hnsHandshape = new HnsHandshape();
    private List<String> handShapeDir = new ArrayList<>();

    private String id = "";
    // some variables cached during loading
    private GestureBinding gesturebinding = null;

    private AsapRealizerEmbodiment are = null;
    private WorldObjectEnvironment we = null;
    private RestPose pose;
    
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        mae = ArrayUtils.getFirstClassOfType(environments, MixedAnimationEnvironment.class);
        we = ArrayUtils.getFirstClassOfType(environments, WorldObjectEnvironment.class);

        for (EmbodimentLoader e : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (e.getEmbodiment() instanceof MixedSkeletonEmbodiment)
            {
                mse = (MixedSkeletonEmbodiment) e.getEmbodiment();
            }
            if (e.getEmbodiment() instanceof PhysicalEmbodiment)
            {
                pe = (PhysicalEmbodiment) e.getEmbodiment();
            }
            if (e.getEmbodiment() instanceof AsapRealizerEmbodiment)
            {
                are = (AsapRealizerEmbodiment) e.getEmbodiment();
            }
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

    static class Parameters extends XMLStructureAdapter
    {
        @Getter
        private List<Parameter> parameters = new ArrayList<>();

        @Override
        public void decodeContent(XMLTokenizer tokenizer) throws IOException
        {
            while (tokenizer.atSTag())
            {
                String tag = tokenizer.getTagName();
                switch (tag)
                {
                case Parameter.XMLTAG:
                    Parameter p = new Parameter();
                    p.readXML(tokenizer);
                    parameters.add(p);
                    break;

                default:
                    throw new XMLScanException("unknown tag " + tag);
                }
            }
        }

        public static final String XMLTAG = "parameters";

        @Override
        public String getXMLTag()
        {
            return XMLTAG;
        }
    }

    static class Parameter extends XMLStructureAdapter
    {
        public String name;
        public String value;

        @Override
        public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {
            name = getRequiredAttribute("name", attrMap, tokenizer);
            value = getRequiredAttribute("value", attrMap, tokenizer);   
            super.decodeAttributes(attrMap, tokenizer);
        }

        public static final String XMLTAG = "parameter";

        @Override
        public String getXMLTag()
        {
            return XMLTAG;
        }
    }

    static class StartPose extends XMLStructureAdapter
    {
        private RestPose restPose;
        private Parameters params = new Parameters();

        private RestPose getRestPose()
        {
            for (Parameter param : params.getParameters())
            {
                try
                {
                    restPose.setParameterValue(param.name, param.value);
                }
                catch (ParameterException e)
                {
                    throw new RuntimeException(e);
                }
            }
            return restPose;
        }

        public boolean decodeAttribute(String attrName, String attrValue, XMLTokenizer tokenizer)
        {
            throw new XMLScanException("StartPose may not contain attributes.");            
        }
        @Override
        public void decodeContent(XMLTokenizer tokenizer) throws IOException
        {
            while (tokenizer.atSTag())
            {
                String tag = tokenizer.getTagName();
                switch (tag)
                {
                case RestPoseAssembler.XMLTAG:
                    RestPoseAssembler rpa = new RestPoseAssembler(new Resources(""));
                    rpa.readXML(tokenizer);
                    restPose = rpa.getRestPose();
                    break;
                case Parameters.XMLTAG:
                    params.readXML(tokenizer);
                    break;
                default:
                    throw new XMLScanException("unknown tag " + tag);
                }
            }
        }

        private static final String XMLTAG = "StartPose";

        @Override
        public String getXMLTag()
        {
            return XMLTAG;
        }
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
            StartPose sp = new StartPose();
            sp.readXML(tokenizer);
            pose = sp.getRestPose();
        }
        else if (tokenizer.atSTag("Hns"))
        {
            attrMap = tokenizer.getAttributes();
            Resources res = new Resources(adapter.getOptionalAttribute("resources", attrMap, ""));
            hns.readXML(res.getReader(adapter.getRequiredAttribute("filename", attrMap, tokenizer)));
            tokenizer.takeSTag("Hns");
            tokenizer.takeETag("Hns");
        }
        else if (tokenizer.atSTag("HnsHandShape"))
        {
            attrMap = tokenizer.getAttributes();
            handShapeDir.add(adapter.getRequiredAttribute("dir", attrMap, tokenizer));
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

        
        if(pose == null)
        {
            pose = new SkeletonPoseRestPose();
        }
        RestGaze defRestGaze = new ForwardRestGaze(GazeInfluence.EYES);
        AnimationPlanPlayer animationPlanPlayer = new AnimationPlanPlayer(pose, defRestGaze, are.getFeedbackManager(),
                animationPlanManager, new DefaultTimedPlanUnitPlayer(), are.getPegBoard());

        animationPlayer = new AnimationPlayer(mse.getPreviousVJoint(), mse.getCurrentVJoint(), mse.getNextVJoint(), pe.getMixedSystems(),
                mae.getH(), we.getWorldObjectManager(), animationPlanPlayer);

        pose.setAnimationPlayer(animationPlayer);
        pose.initialRestPose(0);
        defRestGaze.setAnimationPlayer(animationPlayer);
        
        try
        {
            hnsHandshape = new HnsHandshape(handShapeDir.toArray(new String[0]));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // make planner
        animationPlanner = new AnimationPlanner(are.getFeedbackManager(), animationPlayer, gesturebinding, hns, hnsHandshape,
                animationPlanManager, are.getPegBoard());

        engine = new DefaultEngine<TimedAnimationUnit>(animationPlanner, animationPlayer, animationPlanManager);
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
        are.addEngine(engine);

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
