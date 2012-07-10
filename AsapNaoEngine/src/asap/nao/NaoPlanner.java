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
package asap.nao;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import asap.nao.bml.NaoDoeIetsBehaviour;
import asap.nao.bml.NaoPlayChoregrapheClipBehaviour;
import asap.nao.bml.NaoSayBehaviour;
import asap.nao.naobinding.NaoBinding;
import asap.nao.planunit.TimedNaoUnit;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.LinearStretchResolver;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizer.scheduler.UniModalResolver;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class NaoPlanner extends AbstractPlanner<TimedNaoUnit>
{
    /* register the nao BML behaviors with the BML parser... */
    static
    {
        BMLInfo.addBehaviourType(NaoDoeIetsBehaviour.xmlTag(), NaoDoeIetsBehaviour.class);
        BMLInfo.addBehaviourType(NaoPlayChoregrapheClipBehaviour.xmlTag(), NaoPlayChoregrapheClipBehaviour.class);
        BMLInfo.addBehaviourType(NaoSayBehaviour.xmlTag(), NaoSayBehaviour.class);
    }
  
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(NaoPlanner.class.getName());

    private final Nao theNao;
    private final NaoBinding naoBinding;
    private UniModalResolver resolver;

    public NaoPlanner(FeedbackManager bfm, Nao n, NaoBinding nb, PlanManager<TimedNaoUnit> planManager)
    {
        super(bfm, planManager);
        naoBinding = nb;
        theNao = n;
        resolver = new LinearStretchResolver();
    }

    public NaoBinding getNaoBinding()
    {
        return naoBinding;
    }

    /**
     * Creates a TimedNaoUnit that satisfies sacs and adds it to the plan. All registered BMLFeedbackListener are linked to this TimedNaoUnit.
     */
    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs,
            TimedNaoUnit planElement) throws BehaviourPlanningException
    {
        List<SyncAndTimePeg> satps = new ArrayList<SyncAndTimePeg>();
        TimedNaoUnit tnu;

        if (planElement == null)
        {
            List<TimedNaoUnit> tnus = naoBinding.getNaoUnit(fbManager,bbPeg, b, theNao);
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

        // apply syncs to tnu
        tnu.resolveGestureKeyPositions();
        linkSynchs(tnu, sacs);

        planManager.addPlanUnit(tnu);

        for (KeyPosition kp : tnu.getPegs().keySet())
        {
            TimePeg p = tnu.getPegs().get(kp);
            satps.add(new SyncAndTimePeg(b.getBmlId(), b.id, kp.id, p));
        }
        return satps;
    }

    @Override
    public TimedNaoUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)
            throws BehaviourPlanningException
    {
        List<TimedNaoUnit> tnus = naoBinding.getNaoUnit(fbManager,bbPeg, b, theNao);
        if (tnus.isEmpty())
        {
            throw new BehaviourPlanningException(b, "Behavior " + b.id
                    + " could not be constructed from the nao binding (no matching constraints), behavior omitted.");
        }
        TimedNaoUnit tnu = tnus.get(0);

        if (!tnu.getNaoUnit().hasValidParameters())
        {
            throw new BehaviourPlanningException(b, "Behavior " + b.id
                    + " could not be constructed from the nao binding because the parameters are not valid, behavior omitted.");
        }

        tnu.resolveGestureKeyPositions();
        resolver.resolveSynchs(bbPeg, b, sac, tnu);
        return tnu;
    }

    // link synchpoints in sac to tnu
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
        list.add(NaoDoeIetsBehaviour.class);
        list.add(NaoPlayChoregrapheClipBehaviour.class);
        list.add(NaoSayBehaviour.class);
        return list;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        return list;
    }    
    @Override
    public double getRigidity(Behaviour beh)
    {
        return 0.5;  //actually, some of the physical movements are quite inflexible; more so than the average vjoint animation. Maybe 0.8 would express this better?
    }    
    
}