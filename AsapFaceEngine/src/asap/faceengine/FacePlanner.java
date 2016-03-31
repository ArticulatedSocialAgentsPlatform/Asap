/*******************************************************************************
 *******************************************************************************/
package asap.faceengine;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.FaceInterpolator;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.BMLInfo;
import saiba.bml.core.Behaviour;
import saiba.bml.core.FaceLexemeBehaviour;
import saiba.bml.core.ext.FaceFacsBehaviour;
import asap.bml.ext.bmlt.BMLTFaceKeyframeBehaviour;
import asap.bml.ext.bmlt.BMLTFaceMorphBehaviour;
import asap.bml.ext.murml.MURMLFaceBehaviour;
import asap.faceengine.facebinding.FaceBinding;
import asap.faceengine.facebinding.MURMLFUBuilder;
import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.KeyframeFaceUnit;
import asap.faceengine.faceunit.KeyframeFacsFU;
import asap.faceengine.faceunit.KeyframeFapsMU;
import asap.faceengine.faceunit.KeyframeMorphFU;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.LinearStretchResolver;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizer.scheduler.UniModalResolver;

import com.google.common.collect.ImmutableList;

/**
 * This planner will in the future support planning of face behaviors -- i.e. face expressions and such. In addition, the faceplanner allows for the
 * possiblities to set visemes at certain TimePegs. This functionality is mostly accessed by the verbalplanner.
 * @author Reidsma, welberge
 */
public class FacePlanner extends AbstractPlanner<TimedFaceUnit>
{
    private FaceController faceController;
    private FACSConverter facsConverter;
    private EmotionConverter emotionConverter;

    private final FaceBinding faceBinding;
    private UniModalResolver resolver;
    private final PegBoard pegBoard;

    /* register the MURML BML face behaviors with the BML parser... */
    static
    {
        BMLInfo.addBehaviourType(MURMLFaceBehaviour.xmlTag(), MURMLFaceBehaviour.class);
        BMLInfo.addDescriptionExtension(MURMLFaceBehaviour.xmlTag(), MURMLFaceBehaviour.class);
    }

    public FacePlanner(FeedbackManager bfm, FaceController fc, FACSConverter fconv, EmotionConverter econv, FaceBinding fb,
            PlanManager<TimedFaceUnit> planManager, PegBoard pb)
    {
        super(bfm, planManager);
        faceBinding = fb;
        faceController = fc;
        facsConverter = fconv;
        emotionConverter = econv;
        pegBoard = pb;
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
            tfu = fuCopy.createTFU(fbManager, bbPeg, b.getBmlId(), b.id, pegBoard);
        }
        else if (b instanceof BMLTFaceKeyframeBehaviour && !b.specifiesParameter("name"))
        {
            BMLTFaceKeyframeBehaviour beh = (BMLTFaceKeyframeBehaviour) b;
            FaceInterpolator mi = new FaceInterpolator();
            mi.readXML(beh.content);
            KeyframeFaceUnit fu;
            switch (beh.getType())
            {
            default:
            case MORPH:
                fu = new KeyframeMorphFU(mi);
                break;
            case FACS:
                KeyframeFacsFU kfu = new KeyframeFacsFU(mi);
                kfu = kfu.copy(faceController, facsConverter, emotionConverter);
                fu = kfu;
                break;
            case FAPS:
                fu = new KeyframeFapsMU(mi);
                break;
            }
            fu.setFaceController(faceController);
            tfu = fu.createTFU(fbManager, bbPeg, beh.getBmlId(), beh.id, pegBoard);
        }
        else
        {
            List<TimedFaceUnit> tfus = faceBinding.getFaceUnit(fbManager, bbPeg, b, faceController, facsConverter, emotionConverter,
                    pegBoard);
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

        if (tfu == null)
        {
            tfu = createTfu(bbPeg, b);
        }

        // apply syncs to tfu
        tfu.resolveFaceKeyPositions();
        linkSynchs(tfu, sacs);

        planManager.addPlanUnit(tfu);
        return constructSyncAndTimePegs(bbPeg, b, tfu);
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
        return ImmutableList.of(FaceLexemeBehaviour.class, FaceFacsBehaviour.class, BMLTFaceMorphBehaviour.class,
                BMLTFaceKeyframeBehaviour.class, MURMLFaceBehaviour.class);
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
