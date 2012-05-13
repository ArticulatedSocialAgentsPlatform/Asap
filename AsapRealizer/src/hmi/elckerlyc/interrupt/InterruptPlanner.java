package hmi.elckerlyc.interrupt;

import saiba.bml.core.Behaviour;
import hmi.bml.ext.bmlt.BMLTInterruptBehaviour;
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
 * Planner for the bmlt interrupt behavior 
 * @author welberge
 */
public class InterruptPlanner extends AbstractPlanner<TimedInterruptUnit>
{
    private BMLScheduler scheduler;    
    
    public InterruptPlanner(FeedbackManager bfm,BMLScheduler s, PlanManager<TimedInterruptUnit> planManager)
    {
        this(bfm,planManager);
        scheduler = s;
    }

    /**
     * Should call setScheduler before actual use
     */
    public InterruptPlanner(FeedbackManager bfm, PlanManager<TimedInterruptUnit> planManager)
    {
        super(bfm, planManager);        
    }

    
    
    public void setScheduler(BMLScheduler s)
    {
        scheduler = s;
    }

    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac,
            TimedInterruptUnit iu) throws BehaviourPlanningException
    {
        if (!(b instanceof BMLTInterruptBehaviour))
        {
            throw new BehaviourPlanningException(b, "Behaviour is not a BMLTInterruptBehaviour");
        }
        BMLTInterruptBehaviour ib = (BMLTInterruptBehaviour) b;

        if (iu == null)
        {
            iu = new TimedInterruptUnit(fbManager, bbPeg, b.getBmlId(), b.id, ib.getTarget(), scheduler);
        }
        iu.setInclude(ib.getInclude());
        iu.setExclude(ib.getExclude());
        
        validateSacs(b, sac);
        iu.setStartPeg(sac.get(0).peg);

        planManager.addPlanUnit(iu);

        List<SyncAndTimePeg> list = new ArrayList<SyncAndTimePeg>();
        list.add(new SyncAndTimePeg(b.getBmlId(), b.id, "start", sac.get(0).peg));
        return list;
    }

    private void validateSacs(Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        if (sac.size() > 1)
        {
            throw new BehaviourPlanningException(b, "Multiple synchronization constraints on to interrupt behavior " + b);
        }
        if (!sac.get(0).syncId.equals("start"))
        {
            throw new BehaviourPlanningException(b, "Interrupt behavior " + b + " has a synchronization constraint other than start.");
        }
    }

    @Override
    public TimedInterruptUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)
            throws BehaviourPlanningException
    {
        TimedInterruptUnit iu = new TimedInterruptUnit(fbManager, bbPeg, b.getBmlId(), b.id, b.getStringParameterValue("target"), scheduler);
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
        list.add(BMLTInterruptBehaviour.class);
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
