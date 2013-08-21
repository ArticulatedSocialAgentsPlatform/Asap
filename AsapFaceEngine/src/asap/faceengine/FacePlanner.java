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
package asap.faceengine;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import saiba.bml.core.FaceLexemeBehaviour;
import saiba.bml.core.ext.FaceFacsBehaviour;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;

import java.util.ArrayList;
import java.util.List;

import asap.bml.ext.bmlt.BMLTFaceMorphBehaviour;
import asap.bml.ext.murml.MURMLFaceBehaviour;
import asap.faceengine.facebinding.FaceBinding;
import asap.faceengine.facebinding.MURMLFUBuilder;
import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.TimedFaceUnit;
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

/**
 * This planner will in the future support planning of face behaviors -- i.e. face expressions and such. In addition, the faceplanner allows for the
 * possiblities to set visemes at certain TimePegs. This functionality is mostly accessed by the verbalplanner.
 * @author Reidsma
 */
public class FacePlanner extends AbstractPlanner<TimedFaceUnit>
{
    private FaceController faceController;
    private FACSConverter facsConverter;
    private EmotionConverter emotionConverter;

    private final FaceBinding faceBinding;
    private UniModalResolver resolver;

    /* register the MURML BML face behaviors with the BML parser... */
    static
    {
        BMLInfo.addBehaviourType(MURMLFaceBehaviour.xmlTag(), MURMLFaceBehaviour.class);
        BMLInfo.addDescriptionExtension(MURMLFaceBehaviour.xmlTag(), MURMLFaceBehaviour.class);
    }

    public FacePlanner(FeedbackManager bfm, FaceController fc, FACSConverter fconv, EmotionConverter econv, FaceBinding fb,
            PlanManager<TimedFaceUnit> planManager)
    {
        super(bfm, planManager);
        faceBinding = fb;
        faceController = fc;
        facsConverter = fconv;
        emotionConverter = econv;

        resolver = new LinearStretchResolver();
    }

    public FaceBinding getFaceBinding()
    {
        return faceBinding;
    }

    private TimedFaceUnit createTfu(BMLBlockPeg bbPeg, Behaviour b) throws BehaviourPlanningException
    {
        TimedFaceUnit tfu;
        if (b instanceof MURMLFaceBehaviour)
        {
            FaceUnit fu = MURMLFUBuilder.setup(((MURMLFaceBehaviour) b).getMurmlDescription());
            FaceUnit fuCopy = fu.copy(faceController, facsConverter, emotionConverter);
            tfu = fuCopy.createTFU(fbManager, bbPeg, b.getBmlId(), b.id);
        }
        else
        {
            List<TimedFaceUnit> tfus = faceBinding.getFaceUnit(fbManager, bbPeg, b, faceController, facsConverter, emotionConverter);
            if (tfus.isEmpty())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id
                        + " could not be constructed from the face binding (no matching constraints), behavior omitted.");
            }

            // for now, just add the first
            tfu = tfus.get(0);
            if (!tfu.getFaceUnit().hasValidParameters())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id
                        + " could not be constructed from the face binding because the parameters are not valid, behavior omitted.");
            }
        }
        return tfu;
    }

    /**
     * Creates a TimedFaceUnit that satisfies sacs and adds it to the face plan. All registered BMLFeedbackListener are linked to this TimedFaceUnit.
     */
    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs, TimedFaceUnit tfu)
            throws BehaviourPlanningException
    {
        List<SyncAndTimePeg> satps = new ArrayList<SyncAndTimePeg>();
        if (tfu == null)
        {
            tfu = createTfu(bbPeg, b);
        }

        // apply syncs to tfu
        tfu.resolveFaceKeyPositions();
        linkSynchs(tfu, sacs);

        planManager.addPlanUnit(tfu);

        for (String sync : tfu.getAvailableSyncs())
        {
            TimePeg p = tfu.getTimePeg(sync);
            if (p == null)
            {
                p = new TimePeg(bbPeg);
                tfu.setTimePeg(sync,p);
            }
            satps.add(new SyncAndTimePeg(b.getBmlId(), b.id, sync, p));
        }
        return satps;
    }

    @Override
    public TimedFaceUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        TimedFaceUnit tfu = createTfu(bbPeg, b);
        tfu.resolveFaceKeyPositions();
        resolver.resolveSynchs(bbPeg, b, sac, tfu);
        return tfu;
    }

    // link synchpoints in sac to tfu
    private void linkSynchs(TimedFaceUnit tfu, List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            for (KeyPosition kp : tfu.getFaceUnit().getKeyPositions())
            {
                if (s.syncId.equals(kp.id))
                {
                    if (s.offset == 0)
                    {
                        tfu.setTimePeg(kp, s.peg);
                    }
                    else
                    {
                        tfu.setTimePeg(kp, new OffsetPeg(s.peg, -s.offset));
                    }
                }
            }
        }
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(FaceLexemeBehaviour.class);
        list.add(FaceFacsBehaviour.class);
        list.add(BMLTFaceMorphBehaviour.class);
        list.add(MURMLFaceBehaviour.class);
        return list;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(MURMLFaceBehaviour.class);
        return list;
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return 0.5;
    }
}
