/*******************************************************************************
 *******************************************************************************/
package asap.animationengine;

import hmi.animation.SkeletonInterpolator;
import hmi.util.Resources;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import saiba.bml.core.GazeBehaviour;
import saiba.bml.core.GazeShiftBehaviour;
import saiba.bml.core.GestureBehaviour;
import saiba.bml.core.HeadBehaviour;
import saiba.bml.core.PointingBehaviour;
import saiba.bml.core.PostureBehaviour;
import saiba.bml.core.PostureShiftBehaviour;
import asap.animationengine.gaze.RestGaze;
import asap.animationengine.gesturebinding.GestureBinding;
import asap.animationengine.gesturebinding.HnsHandshape;
import asap.animationengine.gesturebinding.MURMLMUBuilder;
import asap.animationengine.keyframe.KeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.procanimation.ProcAnimationGestureMU;
import asap.animationengine.procanimation.ProcAnimationMU;
import asap.animationengine.restpose.RestPose;
import asap.bml.ext.bmlt.BMLTControllerBehaviour;
import asap.bml.ext.bmlt.BMLTKeyframeBehaviour;
import asap.bml.ext.bmlt.BMLTNoiseBehaviour;
import asap.bml.ext.bmlt.BMLTProcAnimationBehaviour;
import asap.bml.ext.bmlt.BMLTProcAnimationGestureBehaviour;
import asap.bml.ext.murml.MURMLGestureBehaviour;
import asap.hns.Hns;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * Main use: take BML based behaviors, resolve timepegs, add to player. Uses GestureBinding to map BML behavior classes to, e.g., ProcAnimations
 * 
 * @author welberge
 */
public class AnimationPlanner extends AbstractPlanner<TimedAnimationUnit>
{
    private final AnimationPlayer player;
    private final PegBoard pegBoard;
    private GestureBinding gestureBinding;
    private MURMLMUBuilder murmlMUBuilder;

    static
    {
        BMLInfo.addBehaviourType(MURMLGestureBehaviour.xmlTag(), MURMLGestureBehaviour.class);
        BMLInfo.addDescriptionExtension(MURMLGestureBehaviour.xmlTag(), MURMLGestureBehaviour.class);
    }

    public AnimationPlanner(FeedbackManager bfm, AnimationPlayer p, GestureBinding g, Hns hns, HnsHandshape hnsHandshapes,
            PlanManager<TimedAnimationUnit> planManager, PegBoard pb)
    {
        super(bfm, planManager);
        pegBoard = pb;
        gestureBinding = g;
        player = p;
        Hns hnsNew = hns;
        if (hnsNew == null) hnsNew = new Hns();
        if (hnsHandshapes == null) hnsHandshapes = new HnsHandshape();
        murmlMUBuilder = new MURMLMUBuilder(hnsNew, hnsHandshapes);
    }

    public AnimationPlanner(FeedbackManager bfm, AnimationPlayer p, GestureBinding g, PlanManager<TimedAnimationUnit> planManager,
            PegBoard pb)
    {

        this(bfm, p, g, null, null, planManager, pb);
    }

    public void setGestureBinding(GestureBinding g)
    {
        gestureBinding = g;
    }

    public void resolveDefaultKeyPositions(Behaviour b, TimedAnimationMotionUnit tmu)
    {
        if (b instanceof GazeBehaviour)
        {
            tmu.resolveGazeKeyPositions();
        }
        else if (b instanceof PostureShiftBehaviour)
        {
            tmu.resolveStartAndEndKeyPositions();
        }
        else if (b instanceof PostureBehaviour)
        {
            tmu.resolvePostureKeyPositions();
        }
        else
        {
            tmu.resolveGestureKeyPositions();
        }
    }

    /**
     * Creates a TimedMotionUnit that satisfies sacs and adds it to the motion plan. All registered BMLFeedbackListener are linked to this
     * TimedMotionUnit.
     */
    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs, TimedAnimationUnit tmu)
            throws BehaviourPlanningException
    {

        if (tmu == null)
        {
            tmu = createTAU(bbPeg, b);
        }

        // XXX ugly...
        if (tmu instanceof TimedAnimationMotionUnit)
        {
            // apply syncs to tmu
            resolveDefaultKeyPositions(b, (TimedAnimationMotionUnit) tmu);
        }

        linkSynchs(tmu, sacs);

        List<SyncAndTimePeg> satps = constructSyncAndTimePegs(bbPeg, b, tmu);
        planManager.addPlanUnit(tmu);
        return satps;
    }

    @Override
    public TimedAnimationUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)
            throws BehaviourPlanningException
    {
        TimedAnimationUnit tmu = createTAU(bbPeg, b);
        if (tmu instanceof TimedAnimationMotionUnit) // XXX ugly...
        {
            resolveDefaultKeyPositions(b, (TimedAnimationMotionUnit) tmu);
        }
        tmu.resolveSynchs(bbPeg, b, sac);
        return tmu;
    }

    private TimedAnimationUnit createTAU(BMLBlockPeg bbPeg, Behaviour b) throws BehaviourPlanningException
    {
        TimedAnimationUnit tmu;
        if (b instanceof MURMLGestureBehaviour)
        {
            try
            {
                tmu = murmlMUBuilder.setupTMU(((MURMLGestureBehaviour) b).getMurmlDescription(), fbManager, bbPeg, b.getBmlId(), b.id,
                        pegBoard, player);
            }
            catch (TMUSetupException e)
            {
                throw new BehaviourPlanningException(b, "MURMLGestureBehaviour " + b.id + " could not be constructed: " + e.getMessage(), e);
            }
        }
        else if (b instanceof PostureShiftBehaviour)
        {
            RestPose rp = gestureBinding.getRestPose((PostureShiftBehaviour) b, player);
            if (rp == null)
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id + " " + b.toXMLString()
                        + " could not be constructed from the gesture binding, behavior omitted.");
            }
            try
            {
                tmu = rp.createPostureShiftTMU(fbManager, bbPeg, b.getBmlId(), b.id, pegBoard);
            }
            catch (MUSetupException e)
            {
                throw new BehaviourPlanningException(b, "PostureShiftBehaviour " + b.id + " could not be constructed.", e);
            }
        }
        else if (b instanceof GazeShiftBehaviour)
        {
            RestGaze rg = gestureBinding.getRestGaze((GazeShiftBehaviour) b, player);
            if (rg == null)
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id + " " + b.toXMLString()
                        + " could not be constructed from the gesture binding, behavior omitted.");
            }
            try
            {
                tmu = rg.createGazeShiftTMU(fbManager, bbPeg, b.getBmlId(), b.id, pegBoard);
            }
            catch (MUSetupException e)
            {
                throw new BehaviourPlanningException(b, "GazeShiftBehaviour " + b.id + " could not be constructed.", e);
            }
        }
        else if (b instanceof BMLTKeyframeBehaviour && b.hasContent())
        {
            BMLTKeyframeBehaviour beh = (BMLTKeyframeBehaviour) b;
            AnimationUnit mu;
            try
            {
                mu = new KeyframeMU(new SkeletonInterpolator(new XMLTokenizer(beh.content)));
                mu = mu.copy(player);
                if (beh.specifiesParameter("mirror"))
                {
                    mu.setParameterValue("mirror", beh.getStringParameterValue("mirror"));
                }
                if (beh.specifiesParameter("joints"))
                {
                    mu.setParameterValue("joints", beh.getStringParameterValue("joints"));
                }
            }
            catch (IOException e)
            {
                throw new BehaviourPlanningException(b, "BMLTKeyframeBehaviour " + b.id + " could not be constructed.", e);
            }
            catch (ParameterException e)
            {
                throw new BehaviourPlanningException(b, "BMLTKeyframeBehaviour " + b.id + " could not be constructed.", e);
            }
            catch (MUSetupException e)
            {
                throw new BehaviourPlanningException(b, "BMLTKeyframeBehaviour " + b.id + " could not be constructed.", e);
            }
            tmu = mu.createTMU(fbManager, bbPeg, b.getBmlId(), b.id, pegBoard);
        }
        else if (b instanceof BMLTProcAnimationGestureBehaviour)
        {
            BMLTProcAnimationGestureBehaviour beh = (BMLTProcAnimationGestureBehaviour) b;
            ProcAnimationMU mup = new ProcAnimationMU();
            if(beh.getContent()!=null)
            {
                mup.readXML(beh.getContent());
            }
            else if(beh.getFileName()!=null)
            {
                try
                {
                    mup.readXML(new Resources("").getReader(beh.getFileName()));
                }
                catch (IOException e)
                {
                    throw new BehaviourPlanningException(b, "BMLTProcAnimationGestureBehaviour " + b.id + " could not be constructed.", e);
                }
            }
            else
            {
                throw new BehaviourPlanningException(b, "BMLTProcAnimationGestureBehaviour " + b.id + " could not be constructed, no filename or inner ProcAnimation defined.");
            }
            ProcAnimationGestureMU mu = new ProcAnimationGestureMU();
          
            try
            {
                mu = mu.copy(player);
                mu.setGestureUnit(mup);                
            }
            catch (MUSetupException e)
            {
                throw new BehaviourPlanningException(b, "BMLTProcAnimationGestureBehaviour " + b.id + " could not be constructed.", e);
            }
            
            try
            {
                if (beh.specifiesParameter("mirror"))
                {
                    mu.setParameterValue("mirror", beh.getStringParameterValue("mirror"));
                }
                if (beh.specifiesParameter("joints"))
                {
                    mu.setParameterValue("joints", beh.getStringParameterValue("joints"));
                }
            }
            catch (ParameterException e)
            {
                throw new BehaviourPlanningException(b, "BMLTKeyframeBehaviour " + b.id + " could not be constructed.", e);
            }
            
            tmu = mu.createTMU(fbManager, bbPeg, b.getBmlId(), b.id, pegBoard);
        }
        else
        {
            List<TimedAnimationUnit> tmus = gestureBinding.getMotionUnit(bbPeg, b, player, pegBoard, murmlMUBuilder);
            if (tmus.isEmpty())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id + " " + b.toXMLString()
                        + " could not be constructed from the gesture binding, behavior omitted.");
            }
            tmu = tmus.get(0);
        }
        return tmu;
    }

    // link synchpoints in sac to tmu
    private void linkSynchs(TimedAnimationUnit tmu, List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            if (s.offset == 0)
            {
                tmu.setTimePeg(s.syncId, s.peg);
            }
            else
            {
                tmu.setTimePeg(s.syncId, new OffsetPeg(s.peg, -s.offset));
            }
        }
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(HeadBehaviour.class);
        list.add(GazeBehaviour.class);
        list.add(GazeShiftBehaviour.class);
        list.add(GestureBehaviour.class);
        list.add(PostureBehaviour.class);
        list.add(PostureShiftBehaviour.class);
        list.add(PointingBehaviour.class);
        list.add(BMLTProcAnimationBehaviour.class);
        list.add(BMLTProcAnimationGestureBehaviour.class);
        list.add(BMLTControllerBehaviour.class);
        list.add(BMLTKeyframeBehaviour.class);
        list.add(BMLTNoiseBehaviour.class);
        list.add(MURMLGestureBehaviour.class);
        return list;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(BMLTProcAnimationBehaviour.class);
        list.add(BMLTControllerBehaviour.class);
        list.add(BMLTKeyframeBehaviour.class);
        list.add(MURMLGestureBehaviour.class);
        return list;
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return 0.5;
    }
}
