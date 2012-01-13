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
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.scheduler.LinearStretchResolver;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.elckerlyc.scheduler.UniModalResolver;

import java.util.*;

import asap.animationengine.gesturebinding.*;
import asap.animationengine.motionunit.TimedMotionUnit;


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
public class AnimationPlanner extends AbstractPlanner
{
    private final AnimationPlayer player;
    private GestureBinding gestureBinding;
    private final UniModalResolver resolver;

    public AnimationPlanner(FeedbackManager bfm, AnimationPlayer p, GestureBinding g, PlanManager planManager)
    {
        super(bfm,planManager);
        gestureBinding = g;        
        player = p;
        resolver = new LinearStretchResolver();
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
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs,
            TimedPlanUnit planElement) throws BehaviourPlanningException
    {
        List<SyncAndTimePeg> satps = new ArrayList<SyncAndTimePeg>();
        TimedMotionUnit tmu;

        if (planElement == null)
        {
            List<TimedMotionUnit> tmus = gestureBinding.getMotionUnit(bbPeg, b, player);
            if (tmus.isEmpty())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id + " could not be constructed from the gesture binding, behavior omitted.");
            }

            // for now, just add the first
            tmu = tmus.get(0);
        }
        else
        {
            tmu = (TimedMotionUnit) planElement;
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
    public TimedMotionUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)
            throws BehaviourPlanningException
    {
        List<TimedMotionUnit> tmus = gestureBinding.getMotionUnit(bbPeg, b, player);
        if (tmus.isEmpty())
        {
            throw new BehaviourPlanningException(b, "Behavior " + b.id + " could not be constructed from the gesture binding, behavior omitted.");
        }
        TimedMotionUnit tmu = tmus.get(0);
        tmu.resolveDefaultBMLKeyPositions();
        resolver.resolveSynchs(bbPeg, b, sac, tmu);
        return tmu;
    }

    // link synchpoints in sac to tmu
    private void linkSynchs(TimedMotionUnit tmu, List<TimePegAndConstraint> sacs)
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
        return list;
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return 0.5;
    }   
}
