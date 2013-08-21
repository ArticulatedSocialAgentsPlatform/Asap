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
package asap.picture;

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
import asap.picture.bml.AddAnimationDirBehavior;
import asap.picture.bml.AddAnimationXMLBehavior;
import asap.picture.bml.AddImageBehavior;
import asap.picture.bml.SetImageBehavior;
import asap.picture.picturebinding.PictureBinding;
import asap.picture.planunit.TimedPictureUnit;
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

public class PicturePlanner extends AbstractPlanner<TimedPictureUnit>
{
    static
    {
        BMLInfo.addBehaviourType(SetImageBehavior.xmlTag(), SetImageBehavior.class);
        BMLInfo.addBehaviourType(AddImageBehavior.xmlTag(), AddImageBehavior.class);
        BMLInfo.addBehaviourType(AddAnimationDirBehavior.xmlTag(), AddAnimationDirBehavior.class);
        BMLInfo.addBehaviourType(AddAnimationXMLBehavior.xmlTag(), AddAnimationXMLBehavior.class);
    }

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(PicturePlanner.class.getName());

    private final PictureBinding pictureBinding;
    private UniModalResolver resolver;

    public PicturePlanner(FeedbackManager bfm, PictureBinding pb, PlanManager<TimedPictureUnit> planManager)
    {
        super(bfm, planManager);
        pictureBinding = pb;
        resolver = new LinearStretchResolver();
    }

    public PictureBinding getPictureBinding()
    {
        return pictureBinding;
    }

    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs, TimedPictureUnit planElement)
            throws BehaviourPlanningException
    {
        List<SyncAndTimePeg> satps = new ArrayList<SyncAndTimePeg>();
        TimedPictureUnit tpu;

        if (planElement == null)
        {
            List<TimedPictureUnit> tpus = pictureBinding.getPictureUnit(fbManager, bbPeg, b);
            if (tpus.isEmpty())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id
                        + " could not be constructed from the picture binding (no matching constraints), behavior omitted.");
            }

            // for now, just add the first
            tpu = tpus.get(0);
            if (!tpu.getPictureUnit().hasValidParameters())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id
                        + " could not be constructed from the picture binding because the parameters are not valid, behavior omitted.");
            }
        }
        else
        {
            tpu = (TimedPictureUnit) planElement;
        }

        resolveDefaultKeyPositions(b, tpu);
        linkSynchs(tpu, sacs);

        planManager.addPlanUnit(tpu);

        return constructSyncAndTimePegs(bbPeg,b,tpu);
    }

    @Override
    public TimedPictureUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        List<TimedPictureUnit> tpus = pictureBinding.getPictureUnit(fbManager, bbPeg, b);
        if (tpus.isEmpty())
        {
            throw new BehaviourPlanningException(b, "Behavior " + b.id
                    + " could not be constructed from the picture binding (no matching constraints), behavior omitted.");
        }
        TimedPictureUnit tpu = tpus.get(0);

        if (!tpu.getPictureUnit().hasValidParameters())
        {
            throw new BehaviourPlanningException(b, "Behavior " + b.id
                    + " could not be constructed from the picture binding because the parameters are not valid, behavior omitted.");
        }

        resolveDefaultKeyPositions(b, tpu);
        resolver.resolveSynchs(bbPeg, b, sac, tpu);
        return tpu;
    }

    public void resolveDefaultKeyPositions(Behaviour b, TimedPictureUnit tpu)
    {
        if(b instanceof GazeBehaviour)
        {
            tpu.resolveGazeKeyPositions();
        }        
        else if(b instanceof PostureShiftBehaviour)
        {
            tpu.resolveStartAndEndKeyPositions();
        }
        else if(b instanceof PostureBehaviour)
        {
            tpu.resolvePostureKeyPositions();
        }
        else if(b instanceof FaceLexemeBehaviour)
        {
            tpu.resolveFaceKeyPositions();
        }
        else if(b instanceof FaceFacsBehaviour)
        {
            tpu.resolveFaceKeyPositions();
        }
        else if(b instanceof GestureBehaviour)
        {
		    tpu.resolveGestureKeyPositions();
		}
		else
		{
            tpu.resolveStartAndEndKeyPositions();
        }
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return 0.5;
    }

    private void linkSynchs(TimedPictureUnit tpu, List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            for (KeyPosition kp : tpu.getPictureUnit().getKeyPositions())
            {
                if (s.syncId.equals(kp.id))
                {
                    if (s.offset == 0)
                    {
                        tpu.setTimePeg(kp, s.peg);
                    }
                    else
                    {
                        tpu.setTimePeg(kp, new OffsetPeg(s.peg, -s.offset));
                    }
                }
            }
        }
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(SetImageBehavior.class);
        list.add(AddImageBehavior.class);
        list.add(AddAnimationDirBehavior.class);
        list.add(AddAnimationXMLBehavior.class);
        return list;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        return list;
    }
}
