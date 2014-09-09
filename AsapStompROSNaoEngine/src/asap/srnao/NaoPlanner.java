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
package asap.srnao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import saiba.bml.core.FaceLexemeBehaviour;
import saiba.bml.core.GazeBehaviour;
import saiba.bml.core.GestureBehaviour;
import saiba.bml.core.PostureBehaviour;
import saiba.bml.core.PostureShiftBehaviour;
import saiba.bml.core.ext.FaceFacsBehaviour;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.LinearStretchResolver;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizer.scheduler.UniModalResolver;
import asap.srnao.bml.MoveJointBehavior;
import asap.srnao.bml.RunChoregrapheClipBehavior;
import asap.srnao.bml.SetJointAngleBehavior;
import asap.srnao.naobinding.NaoBinding;
import asap.srnao.planunit.TimedNaoUnit;

public class NaoPlanner extends AbstractPlanner<TimedNaoUnit>
{
    static
    {
        BMLInfo.addBehaviourType(RunChoregrapheClipBehavior.xmlTag(), RunChoregrapheClipBehavior.class);
        BMLInfo.addBehaviourType(SetJointAngleBehavior.xmlTag(), SetJointAngleBehavior.class);
        BMLInfo.addBehaviourType(MoveJointBehavior.xmlTag(), MoveJointBehavior.class);
    }

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(NaoPlanner.class.getName());

    private final NaoBinding naoBinding;
    private UniModalResolver resolver;

    public NaoPlanner(FeedbackManager bfm, NaoBinding nb, PlanManager<TimedNaoUnit> planManager)
    {
        super(bfm, planManager);
        naoBinding = nb;
        resolver = new LinearStretchResolver();
    }

    public NaoBinding getNaoBinding()
    {
        return naoBinding;
    }

    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs, TimedNaoUnit planElement)
            throws BehaviourPlanningException
    {
        TimedNaoUnit tnu;

        if (planElement == null)
        {
            List<TimedNaoUnit> tnus = naoBinding.getNaoUnit(fbManager, bbPeg, b);
            if (tnus.isEmpty())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id
                        + " could not be constructed from the nao binding (no matching constraints), behavior omitted.");
            }

            // for now, just add the first
            tnu = tnus.get(0);
            if (!tnu.getNaoUnit().hasValidParameters())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id
                        + " could not be constructed from the nao binding because the parameters are not valid, behavior omitted.");
            }
        }
        else
        {
            tnu = (TimedNaoUnit) planElement;
        }

        resolveDefaultKeyPositions(b, tnu);
        linkSynchs(tnu, sacs);

        planManager.addPlanUnit(tnu);

        return constructSyncAndTimePegs(bbPeg,b,tnu);
    }

    @Override
    public TimedNaoUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        List<TimedNaoUnit> tnus = naoBinding.getNaoUnit(fbManager, bbPeg, b);
        if (tnus.isEmpty())
        {
            throw new BehaviourPlanningException(b, "Behavior " + b.id
                    + " could not be constructed from the nao binding (no matching constraints), behavior omitted.");
        }
        TimedNaoUnit tnu = tnus.get(0);

        if (!tnu.getNaoUnit().hasValidParameters())
        {
            throw new BehaviourPlanningException(b, "Behavior " + b.id
                    + " could not be constructed from the picture binding because the parameters are not valid, behavior omitted.");
        }

        resolveDefaultKeyPositions(b, tnu);
        resolver.resolveSynchs(bbPeg, b, sac, tnu);
        return tnu;
    }

    public void resolveDefaultKeyPositions(Behaviour b, TimedNaoUnit tnu)
    {
        if(b instanceof GazeBehaviour)
        {
            tnu.resolveGazeKeyPositions();
        }        
        else if(b instanceof PostureShiftBehaviour)
        {
            tnu.resolveStartAndEndKeyPositions();
        }
        else if(b instanceof PostureBehaviour)
        {
            tnu.resolvePostureKeyPositions();
        }
        else if(b instanceof FaceLexemeBehaviour)
        {
            tnu.resolveFaceKeyPositions();
        }
        else if(b instanceof FaceFacsBehaviour)
        {
            tnu.resolveFaceKeyPositions();
        }
        else if(b instanceof GestureBehaviour)
        {
		    tnu.resolveGestureKeyPositions();
		}
		else
		{
            tnu.resolveStartAndEndKeyPositions();
        }
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return 0.5;
    }

    private void linkSynchs(TimedNaoUnit tnu, List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            for (KeyPosition kp : tnu.getNaoUnit().getKeyPositions())
            {
                if (s.syncId.equals(kp.id))
                {
                    if (s.offset == 0)
                    {
                        tnu.setTimePeg(kp, s.peg);
                    }
                    else
                    {
                        tnu.setTimePeg(kp, new OffsetPeg(s.peg, -s.offset));
                    }
                }
            }
        }
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(RunChoregrapheClipBehavior.class);
        list.add(SetJointAngleBehavior.class);
        list.add(MoveJointBehavior.class);
        return list;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        return list;
    }
}
