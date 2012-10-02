package asap.animationengine.motionunit;

import java.util.List;
import java.util.Set;

import saiba.bml.core.Behaviour;

import asap.motionunit.TMUPlayException;
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
    public Set<String> getKinematicJoints();
    public Set<String> getPhysicalJoints();
    public void updateTiming(double time) throws TMUPlayException;    
    public void setSubUnit(boolean subUnit);
    
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)throws BehaviourPlanningException;    
}
