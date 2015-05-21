/*******************************************************************************
 *******************************************************************************/
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
import asap.srnao.bml.AddAnimationDirBehavior;
import asap.srnao.bml.AddAnimationXMLBehavior;
import asap.srnao.bml.AddImageBehavior;
import asap.srnao.bml.SetImageBehavior;
import asap.srnao.naobinding.NaoBinding;
import asap.srnao.planunit.TimedNaoUnit;

public class PicturePlanner extends AbstractPlanner<TimedNaoUnit>
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

    private final NaoBinding pictureBinding;
    private UniModalResolver resolver;

    public PicturePlanner(FeedbackManager bfm, NaoBinding pb, PlanManager<TimedNaoUnit> planManager)
    {
        super(bfm, planManager);
        pictureBinding = pb;
        resolver = new LinearStretchResolver();
    }

    public NaoBinding getPictureBinding()
    {
        return pictureBinding;
    }

    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs, TimedNaoUnit planElement)
            throws BehaviourPlanningException
    {
        TimedNaoUnit tpu;

        if (planElement == null)
        {
            List<TimedNaoUnit> tpus = pictureBinding.getNaoUnit(fbManager, bbPeg, b);
            if (tpus.isEmpty())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id
                        + " could not be constructed from the picture binding (no matching constraints), behavior omitted.");
            }

            // for now, just add the first
            tpu = tpus.get(0);
            if (!tpu.getNaoUnit().hasValidParameters())
            {
                throw new BehaviourPlanningException(b, "Behavior " + b.id
                        + " could not be constructed from the picture binding because the parameters are not valid, behavior omitted.");
            }
        }
        else
        {
            tpu = (TimedNaoUnit) planElement;
        }

        resolveDefaultKeyPositions(b, tpu);
        linkSynchs(tpu, sacs);

        planManager.addPlanUnit(tpu);

        return constructSyncAndTimePegs(bbPeg,b,tpu);
    }

    @Override
    public TimedNaoUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        List<TimedNaoUnit> tpus = pictureBinding.getNaoUnit(fbManager, bbPeg, b);
        if (tpus.isEmpty())
        {
            throw new BehaviourPlanningException(b, "Behavior " + b.id
                    + " could not be constructed from the picture binding (no matching constraints), behavior omitted.");
        }
        TimedNaoUnit tpu = tpus.get(0);

        if (!tpu.getNaoUnit().hasValidParameters())
        {
            throw new BehaviourPlanningException(b, "Behavior " + b.id
                    + " could not be constructed from the picture binding because the parameters are not valid, behavior omitted.");
        }

        resolveDefaultKeyPositions(b, tpu);
        resolver.resolveSynchs(bbPeg, b, sac, tpu);
        return tpu;
    }

    public void resolveDefaultKeyPositions(Behaviour b, TimedNaoUnit tpu)
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

    private void linkSynchs(TimedNaoUnit tpu, List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint s : sacs)
        {
            for (KeyPosition kp : tpu.getNaoUnit().getKeyPositions())
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
