/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import java.util.List;

import saiba.bml.core.Behaviour;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;

/**
 * Resolves the timing of a TimedPlanUnit, given certain time constraints upon it.
 * @author welberge
 */
public interface UniModalResolver
{
    void resolveSynchs(BMLBlockPeg bbPeg,Behaviour b, List<TimePegAndConstraint> sac, TimedPlanUnit pu)throws BehaviourPlanningException;
}
