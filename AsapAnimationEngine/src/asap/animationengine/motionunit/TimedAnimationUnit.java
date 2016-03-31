/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.motionunit;

import java.util.List;
import java.util.Set;

import saiba.bml.core.Behaviour;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * A TimedPlanUnit for body animation
 * @author hvanwelbergen
 *
 */
public interface TimedAnimationUnit extends TimedPlanUnit
{
    Set<String> getKinematicJoints();
    Set<String> getPhysicalJoints();    
    void setSubUnit(boolean subUnit);
    
    void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)throws BehaviourPlanningException;
    
    double getPreparationDuration();
    double getRetractionDuration();
    double getStrokeDuration();
}
