package hmi.elckerlyc.activate;

import saiba.bml.core.Behaviour;
import hmi.bml.ext.bmlt.BMLTActivateBehaviour;
import hmi.elckerlyc.AbstractPlanner;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.SyncAndTimePeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.scheduler.BMLScheduler;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Planner for the bmlt activate behaviors 
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
        if (!(b instanceof BMLTActivateBehaviour))
        {
            throw new BehaviourPlanningException(b, "Behaviour is not a BMLTActivateBehaviour");
        }
        BMLTActivateBehaviour ab = (BMLTActivateBehaviour) b;

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
        list.add(BMLTActivateBehaviour.class);
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
