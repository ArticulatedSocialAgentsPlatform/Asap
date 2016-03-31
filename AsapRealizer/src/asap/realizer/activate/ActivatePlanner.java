/*******************************************************************************
 *******************************************************************************/
package asap.realizer.activate;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.core.Behaviour;
import asap.bml.ext.bmla.BMLAActivateBehaviour;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * Planner for the bmla activate behaviors 
 * @author welberge
 */
public class ActivatePlanner extends AbstractPlanner<TimedActivateUnit>
{
    private BMLScheduler scheduler;
    
    public ActivatePlanner(FeedbackManager bfm,BMLScheduler s, PlanManager<TimedActivateUnit> planManager)
    {
        this(bfm,planManager);
        scheduler = s;
    }

    /**
     * Should call setScheduler before actual use
     */
    public ActivatePlanner(FeedbackManager bfm, PlanManager<TimedActivateUnit> planManager)
    {
        super(bfm, planManager);        
    }

    public void setScheduler(BMLScheduler s)
    {
        scheduler = s;
    }

    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac,
            TimedActivateUnit au) throws BehaviourPlanningException
    {
        if (!(b instanceof BMLAActivateBehaviour))
        {
            throw new BehaviourPlanningException(b, "Behaviour is not a BMLAActivateBehaviour");
        }
        BMLAActivateBehaviour ab = (BMLAActivateBehaviour) b;

        if (au == null)
        {
            au = new TimedActivateUnit(fbManager, bbPeg, b.getBmlId(), b.id, ab.getTarget(), scheduler);
        }
        
        validateSacs(b, sac);
        au.setStartPeg(sac.get(0).peg);

        planManager.addPlanUnit(au);

        List<SyncAndTimePeg> list = new ArrayList<SyncAndTimePeg>();
        list.add(new SyncAndTimePeg(b.getBmlId(), b.id, "start", sac.get(0).peg));
        return list;
    }

    private void validateSacs(Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        if (sac.size() > 1)
        {
            throw new BehaviourPlanningException(b, "Multiple synchronization constraints on to activate behavior " + b);
        }
        if (!sac.get(0).syncId.equals("start"))
        {
            throw new BehaviourPlanningException(b, "Activate behavior " + b + " has a synchronization constraint other than start.");
        }
    }

    @Override
    public TimedActivateUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)
            throws BehaviourPlanningException
    {
        TimedActivateUnit iu = new TimedActivateUnit(fbManager, bbPeg, b.getBmlId(), b.id, b.getStringParameterValue("target"), scheduler);
        validateSacs(b, sac);

        TimePegAndConstraint sacStart = sac.get(0);
        if (sacStart.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
        {
            sacStart.peg.setLocalValue(0);
        }

        TimePeg start;
        if(sacStart.offset==0)
        {
            start = sacStart.peg;
        }
        else
        {
            start = new OffsetPeg(sacStart.peg,-sacStart.offset);
        }
        iu.setStartPeg(start);
        return iu;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(BMLAActivateBehaviour.class);
        return list;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedDescriptionExtensions()
    {
        return new ArrayList<Class<? extends Behaviour>>();
    }

    @Override
    public double getRigidity(Behaviour beh)
    {
        return 0;
    }
}
