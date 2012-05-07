package hmi.elckerlyc.scheduler;

import hmi.bml.core.Behaviour;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.TimedPlanUnit;

import java.util.List;

/**
 * Resolves the timing of a TimedPlanUnit, given certain time constraints upon it.
 * @author welberge
 */
public interface UniModalResolver
{
    void resolveSynchs(BMLBlockPeg bbPeg,Behaviour b, List<TimePegAndConstraint> sac, TimedPlanUnit pu)throws BehaviourPlanningException;
}
