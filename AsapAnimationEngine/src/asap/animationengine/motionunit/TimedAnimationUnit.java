package asap.animationengine.motionunit;

import java.util.Set;

import asap.motionunit.TMUPlayException;
import asap.realizer.planunit.TimedPlanUnit;

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
}
