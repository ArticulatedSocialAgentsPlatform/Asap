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
package hmi.shaderengine;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import hmi.shaderengine.bml.*;
import hmi.shaderengine.planunit.*;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.LinearStretchResolver;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizer.scheduler.UniModalResolver;
import asap.realizerport.RealizerPort;

import hmi.renderenvironment.*;

/**
 */
public class ShaderPlanner extends AbstractPlanner<TimedShaderUnit>
{
  
    //private static Logger logger = LoggerFactory.getLogger(ShaderPlanner.class.getName());

    private UniModalResolver resolver;
    
    private HmiRenderBodyEmbodiment hrbe = null;
	
    public ShaderPlanner(FeedbackManager bfm, PlanManager<TimedShaderUnit> planManager, HmiRenderBodyEmbodiment hrbe)
    {
        super(bfm, planManager);
        resolver = new LinearStretchResolver();
		this.hrbe = hrbe;
        /* register the Shader BML behaviors with the BML parser... */
        BMLInfo.addBehaviourType(SetShaderParameterBehaviour.xmlTag(), SetShaderParameterBehaviour.class);
    }


    /**
     * Creates a TimedShaderUnit that satisfies sacs and adds it to the plan. All registered BMLFeedbackListener are linked to this TimedShaderUnit.
     */
    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs,
            TimedShaderUnit tsu) throws BehaviourPlanningException
    {
        List<SyncAndTimePeg> satps = new ArrayList<SyncAndTimePeg>();
        
        if (tsu == null)
        {
          if (b instanceof SetShaderParameterBehaviour)
          {
            SetShaderParameterBehaviour sspb  = (SetShaderParameterBehaviour)b;
            SetShaderParameterSU su = new SetShaderParameterSU();
            try
            {
			  su.setParameterValue("mesh", sspb.getStringParameterValue("mesh"));
			  su.setParameterValue("material", sspb.getStringParameterValue("material"));
			  su.setParameterValue("parameter", sspb.getStringParameterValue("parameter"));
			  su.setParameterValue("value", sspb.getStringParameterValue("value"));
			  su.setEmbodiment(hrbe);
            }
            catch (Exception e)
            {
              e.printStackTrace();
              throw new BehaviourPlanningException(b, "Behavior " + b.id
                    + " could not be constructed because the parameters could not be set, behavior omitted.");
            }
            tsu = new TimedShaderUnit(fbManager, bbPeg,  b.getBmlId(),b.id, su);
            if (!tsu.getShaderUnit().hasValidParameters())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id
                        + " could not be constructed because the parameters are not valid, behavior omitted.");
            }
          }
          else
          {
              throw new BehaviourPlanningException(b, "Behavior " + b.id
                      + " could not be constructed because the behaviour type is unknown: " + b.getClass().getName());
          }
        }        

        // apply syncs to tsu
        tsu.resolveStartAndEndKeyPositions();
        linkSynchs(tsu, sacs);

        planManager.addPlanUnit(tsu);

        for (KeyPosition kp : tsu.getPegs().keySet())
        {
            TimePeg p = tsu.getPegs().get(kp);
            satps.add(new SyncAndTimePeg(b.getBmlId(), b.id, kp.id, p));
        }
        return satps;
    }

    @Override
    public TimedShaderUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)
            throws BehaviourPlanningException
    {
        TimedShaderUnit tsu;
          if (b instanceof SetShaderParameterBehaviour)
          {
            SetShaderParameterBehaviour sspb  = (SetShaderParameterBehaviour)b;
            SetShaderParameterSU su = new SetShaderParameterSU();
            try
            {
			  su.setParameterValue("mesh", sspb.getStringParameterValue("mesh"));
			  su.setParameterValue("material", sspb.getStringParameterValue("material"));
			  su.setParameterValue("parameter", sspb.getStringParameterValue("parameter"));
			  su.setFloatParameterValue("value", sspb.getFloatParameterValue("value"));
			  su.setEmbodiment(hrbe);
            }
            catch (Exception e)
            {
              e.printStackTrace();
              throw new BehaviourPlanningException(b, "Behavior " + b.id
                    + " could not be constructed because the parameters could not be set, behavior omitted.");
            }
            tsu = new TimedShaderUnit(fbManager, bbPeg,  b.getBmlId(),b.id, su);
            if (!tsu.getShaderUnit().hasValidParameters())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id
                        + " could not be constructed because the parameters are not valid, behavior omitted.");
            }
          }
          else
          {
              throw new BehaviourPlanningException(b, "Behavior " + b.id
                      + " could not be constructed because the behaviour type is unknown: " + b.getClass().getName());
          }

        // apply syncs to tsu
        tsu.resolveStartAndEndKeyPositions();
        resolver.resolveSynchs(bbPeg, b, sac, tsu);
        return tsu;
    }

    // link synchpoints in sac to tsu
    private void linkSynchs(TimedShaderUnit tsu, List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            for (KeyPosition kp : tsu.getShaderUnit().getKeyPositions())
            {
                if (s.syncId.equals(kp.id))
                {
                    if (s.offset == 0)
                    {
                        tsu.setTimePeg(kp, s.peg);
                    }
                    else
                    {
                        tsu.setTimePeg(kp, new OffsetPeg(s.peg, -s.offset));
                    }
                }
            }
        }
    }


    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(SetShaderParameterBehaviour.class);
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
        return 0;
    }    
    
}