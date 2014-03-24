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
package asap.animationengine;

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
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.restpose.RestPose;
import asap.bml.ext.bmlt.BMLTControllerBehaviour;
import asap.bml.ext.bmlt.BMLTKeyframeBehaviour;
import asap.bml.ext.bmlt.BMLTNoiseBehaviour;
import asap.bml.ext.bmlt.BMLTProcAnimationBehaviour;
import asap.bml.ext.murml.MURMLGestureBehaviour;
import asap.hns.Hns;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
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
        if (hnsHandshapes == null) hnsHandshapes = new HnsHandshape(hnsNew);
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
            if(rp == null)
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id +" "+b.toXMLString()
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
            RestGaze rg = gestureBinding.getRestGaze((GazeShiftBehaviour)b, player);
            if(rg == null)
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id +" "+b.toXMLString()
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
        else
        {
            List<TimedAnimationUnit> tmus = gestureBinding.getMotionUnit(bbPeg, b, player, pegBoard, murmlMUBuilder);
            if (tmus.isEmpty())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id  +" "+b.toXMLString()
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
