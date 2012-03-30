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

import hmi.elckerlyc.*;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;

import java.util.*;

import asap.animationengine.gesturebinding.*;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.ext.murml.MURMLGestureBehaviour;

import hmi.bml.core.Behaviour;
import hmi.bml.core.GazeBehaviour;
import hmi.bml.core.GestureBehaviour;
import hmi.bml.core.HeadBehaviour;
import hmi.bml.core.PostureBehaviour;
import hmi.bml.ext.bmlt.BMLTControllerBehaviour;
import hmi.bml.ext.bmlt.BMLTKeyframeBehaviour;
import hmi.bml.ext.bmlt.BMLTProcAnimationBehaviour;
import hmi.bml.ext.bmlt.BMLTTransitionBehaviour;
import hmi.bml.ext.bmlt.BMLTNoiseBehaviour;

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

    public AnimationPlanner(FeedbackManager bfm, AnimationPlayer p, GestureBinding g, PlanManager<TimedAnimationUnit> planManager, PegBoard pb)
    {
        super(bfm, planManager);
        pegBoard = pb;
        gestureBinding = g;
        player = p;
    }

    public void setGestureBinding(GestureBinding g)
    {
        gestureBinding = g;
    }

    
    /**
     * Creates a TimedMotionUnit that satisfies sacs and adds it to the motion plan. All registered BMLFeedbackListener are linked to this
     * TimedMotionUnit.
     */
    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs, TimedAnimationUnit tmu)
            throws BehaviourPlanningException
    {
        List<SyncAndTimePeg> satps = new ArrayList<SyncAndTimePeg>();

        if (tmu == null)
        {
            tmu = createTAU(bbPeg, b);
        }

        // apply syncs to tmu
        tmu.resolveDefaultBMLKeyPositions();
        linkSynchs(tmu, sacs);

        planManager.addPlanUnit(tmu);

        for (KeyPosition kp : tmu.getPegs().keySet())
        {
            TimePeg p = tmu.getPegs().get(kp);
            satps.add(new SyncAndTimePeg(b.getBmlId(), b.id, kp.id, p));
        }
        return satps;
    }

    @Override
    public TimedAnimationUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        TimedAnimationUnit tmu = createTAU(bbPeg, b);
        tmu.resolveDefaultBMLKeyPositions();
        tmu.resolveSynchs(bbPeg, b, sac);
        return tmu;
    }

    private TimedAnimationUnit createTAU(BMLBlockPeg bbPeg, Behaviour b) throws BehaviourPlanningException
    {
        TimedAnimationUnit tmu;
        if(b instanceof MURMLGestureBehaviour)
        {
            AnimationUnit mu = MURMLMUBuilder.setup( ((MURMLGestureBehaviour)b).getMurmlDefinition());
            AnimationUnit muCopy;
            try
            {
                muCopy = mu.copy(player);
            }
            catch (MUSetupException e)
            {
                throw new BehaviourPlanningException(b, "MURMLGestureBehaviour " + b.id
                        + " could not be constructed.",e);
            }
            tmu = muCopy.createTMU(fbManager, bbPeg, b.getBmlId(), b.id, pegBoard);
        }
        else
        {
            List<TimedAnimationUnit> tmus = gestureBinding.getMotionUnit(bbPeg, b, player, pegBoard);
            if (tmus.isEmpty())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id
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
            for (KeyPosition kp : tmu.getMotionUnit().getKeyPositions())
            {
                if (s.syncId.equals(kp.id))
                {
                    if (s.offset == 0)
                    {
                        tmu.setTimePeg(kp, s.peg);
                    }
                    else
                    {
                        tmu.setTimePeg(kp, new OffsetPeg(s.peg, -s.offset));
                    }
                }
            }
        }
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(HeadBehaviour.class);
        list.add(GazeBehaviour.class);
        list.add(GestureBehaviour.class);
        list.add(PostureBehaviour.class);
        list.add(BMLTProcAnimationBehaviour.class);
        list.add(BMLTControllerBehaviour.class);
        list.add(BMLTTransitionBehaviour.class);
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
        list.add(BMLTTransitionBehaviour.class);
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
