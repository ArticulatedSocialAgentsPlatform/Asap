/*******************************************************************************
 *******************************************************************************/
package asap.realizer.parametervaluechange;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.core.Behaviour;
import asap.bml.ext.bmla.BMLAParameterValueChangeBehaviour;
import asap.realizer.AbstractPlanner;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * Planner for ParameterValueChange behaviors
 * @author Herwin van Welbergen
 * 
 */
public class ParameterValueChangePlanner extends AbstractPlanner<TimedParameterValueChangeUnit>
{
    private BMLScheduler scheduler;
    private final TrajectoryBinding trajectoryBinding;

    public ParameterValueChangePlanner(FeedbackManager bfm, BMLScheduler scheduler, TrajectoryBinding trajBinding,
            PlanManager<TimedParameterValueChangeUnit> planManager)
    {
        this(bfm, trajBinding, planManager);
        this.scheduler = scheduler;
    }

    /**
     * Should call setScheduler before actual use
     */
    public ParameterValueChangePlanner(FeedbackManager bfm, TrajectoryBinding trajBinding,
            PlanManager<TimedParameterValueChangeUnit> planManager)
    {
        super(bfm, planManager);
        trajectoryBinding = trajBinding;
    }

    public void setScheduler(BMLScheduler s)
    {
        scheduler = s;
    }

    private TimedParameterValueChangeUnit createTimedParameterValueChangeUnit(BMLBlockPeg bbPeg, Behaviour b)
            throws BehaviourPlanningException
    {
        ParameterValueInfo paramValInfo;
        String[] target = b.getStringParameterValue("target").split(":");

        if (!b.specifiesParameter("initialValue"))
        {
            paramValInfo = new ParameterValueInfo(target[0], target[1], b.getStringParameterValue("paramId"),
                    b.getFloatParameterValue("targetValue"));
        }
        else
        {
            paramValInfo = new ParameterValueInfo(target[0], target[1], b.getStringParameterValue("paramId"),
                    b.getFloatParameterValue("initialValue"), b.getFloatParameterValue("targetValue"));
        }
        ParameterValueTrajectory traj;
        try
        {
            traj = trajectoryBinding.getTrajectory(b.getStringParameterValue("type"));
        }
        catch (TrajectoryBindingException e)
        {
            BehaviourPlanningException ex = new BehaviourPlanningException(b, "Error binding trajectory type");
            ex.initCause(e);
            throw ex;
        }
        return new TimedParameterValueChangeUnit(fbManager, bbPeg, b.getBmlId(), b.id, scheduler, paramValInfo, traj);
    }

    private void setupPegs(TimedParameterValueChangeUnit tpvu, List<TimePegAndConstraint> sac)
    {
        if (getSacStart(sac) != null)
        {
            setTimePegFromSac("start",getSacStart(sac), tpvu);
            TimePeg start = tpvu.getTimePeg("start");
            if (start.getGlobalValue() == TimePeg.VALUE_UNKNOWN)
            {
                start.setLocalValue(0);
            }
        }
        else
        {
            setTimePegFromSac("start",getSacEnd(sac), tpvu);            
        }
        if (getSacEnd(sac) != null)
        {
            setTimePegFromSac("end",getSacEnd(sac), tpvu);            
        }
        else
        {
            setTimePegFromSac("end",getSacStart(sac), tpvu);                        
        }
    }

    @Override
    public List<SyncAndTimePeg> addBehaviour(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac,
            TimedParameterValueChangeUnit pvcu) throws BehaviourPlanningException
    {
        if (pvcu == null)
        {
            pvcu = createTimedParameterValueChangeUnit(bbPeg, b);
        }
        validateSacs(b, sac);
        setupPegs(pvcu, sac);

        planManager.addPlanUnit(pvcu);

        List<SyncAndTimePeg> list = new ArrayList<SyncAndTimePeg>();
        list.add(new SyncAndTimePeg(b.getBmlId(), b.id, "start", pvcu.getTimePeg("start")));
        list.add(new SyncAndTimePeg(b.getBmlId(), b.id, "end", pvcu.getTimePeg("end")));
        return list;
    }

    @Override
    public TimedParameterValueChangeUnit resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)
            throws BehaviourPlanningException
    {
        TimedParameterValueChangeUnit tpvu = createTimedParameterValueChangeUnit(bbPeg, b);
        validateSacs(b, sac);
        setupPegs(tpvu, sac);
        return tpvu;
    }

    

    private void validateSacs(Behaviour b, List<TimePegAndConstraint> sacs) throws BehaviourPlanningException
    {
        if (sacs.size() > 2)
        {
            throw new BehaviourPlanningException(b, "Multiple synchronization constraints: " + sacs
                    + " on to ParameterValueChange behavior " + b);
        }
        if (sacs.size() < 1)
        {
            throw new BehaviourPlanningException(b, "No synchronization constraints on to ParameterValueChange behavior " + b);
        }
        for (TimePegAndConstraint sac : sacs)
        {
            if (!sac.syncId.equals("start") && !sac.syncId.equals("end"))
            {
                throw new BehaviourPlanningException(b, "ParameterValueChange behavior " + b
                        + " has a synchronization constraint other than start or end, on sync " + sac.syncId);
            }
        }
    }

    @Override
    public List<Class<? extends Behaviour>> getSupportedBehaviours()
    {
        List<Class<? extends Behaviour>> list = new ArrayList<Class<? extends Behaviour>>();
        list.add(BMLAParameterValueChangeBehaviour.class);
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
