package hmi.elckerlyc.wait;

import saiba.bml.core.Behaviour;
import saiba.bml.core.WaitBehaviour;
import hmi.elckerlyc.AbstractPlanner;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.SyncAndTimePeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Planner that manages wait behaviors.
 * @author Herwin van Welbergen
 *
 */
public class WaitPlanner extends AbstractPlanner<TimedWaitUnit>
{
    public WaitPlanner(FeedbackManager bfm, PlanManager<TimedWaitUnit> planManager)
    {
        super(bfm, planManager);        
    }

    private void validateSacs(Behaviour b, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        if (sacs.size() > 2)
        {
            throw new BehaviourPlanningException(b, "More than two synchronization constraints: " + sacs + " on wait behaviour " + b);
        }
        for (TimePegAndConstraint sac : sacs)
        {
            if (!sac.syncId.equals("start") && !sac.syncId.equals("end"))
            {
                throw new BehaviourPlanningException(b, "Wait behavior " + b + " has a synchronization constraint other than start or end.");
            }
        }
    }

    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs,
            TimedWaitUnit wu) throws BehaviourPlanningException
    {
        if (wu == null)
        {
            wu = new TimedWaitUnit(fbManager, bbPeg, b.getBmlId(), b.id);
        }
        validateSacs(b, sacs);
        planManager.addPlanUnit(wu);
        

        List<SyncAndTimePeg> list = new ArrayList<SyncAndTimePeg>();

        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("start"))
            {
                wu.setStartPeg(sac.peg);
            }
            if (sac.syncId.equals("end"))
            {
                wu.setEndPeg(sac.peg);
            }
            list.add(new SyncAndTimePeg(b.getBmlId(), b.id, sac.syncId, sac.peg));
        }

        return list;
    }

    @Override
    public TimedWaitUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sacs)
            throws BehaviourPlanningException
    {
        validateSacs(b, sacs);
        TimedWaitUnit wu = new TimedWaitUnit(fbManager, bbPeg, b.getBmlId(), b.id);

        TimePegAndConstraint sacStart = null;
        TimePegAndConstraint sacEnd = null;
        float maxDur = b.getFloatParameterValue("max-wait");

        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals("start"))
            {
                sacStart = sac;
            }
            if (sac.syncId.equals("end"))
            {
                sacEnd = sac;
            }
        }

        // resolve start peg
        TimePeg startPeg = null;
        if (sacStart != null)
        {
            if (sacStart.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                if (sacEnd == null || sacEnd.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN || maxDur <= 0)
                {
                    sacStart.peg.setLocalValue(0);
                }
                else
                {
                    sacStart.peg.setGlobalValue(sacEnd.peg.getGlobalValue() - maxDur);
                }
            }
            if(sacStart.offset==0)
            {
                startPeg = sacStart.peg;
            }
            else
            {
                startPeg = new OffsetPeg(sacStart.peg, -sacStart.offset);
            }
        }
        else
        // will never happen with SmartBody scheduling
        {
            if (sacEnd != null && sacEnd.peg.getGlobalValue() != TimePeg.VALUE_UNKNOWN && maxDur > 0)
            {
                startPeg = new OffsetPeg(sacEnd.peg, -maxDur);
            }
            else
            {
                // wait without start or end
                startPeg = new TimePeg(bbPeg);
                startPeg.setLocalValue(0);
            }
        }
        wu.setStartPeg(startPeg);

        // resolve end peg
        if (sacEnd == null)
        {
            if (maxDur <= 0)
            {
                // set unknown end
                TimePeg p = new TimePeg(bbPeg);
                wu.setEndPeg(p);
            }
            else
            {
                wu.setEndPeg(new OffsetPeg(startPeg, maxDur));
            }
        }
        else
        {
            if (sacEnd.peg.getGlobalValue() == TimePeg.VALUE_UNKNOWN && maxDur > 0)
            {
                sacEnd.peg.setGlobalValue(startPeg.getGlobalValue() + maxDur);
            }
            wu.setEndPeg(sacEnd.peg);
        }
        return wu;
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(WaitBehaviour.class);
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
        return 1;
    }
}
