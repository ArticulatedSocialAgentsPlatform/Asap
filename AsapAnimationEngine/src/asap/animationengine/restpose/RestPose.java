package asap.animationengine.restpose;

import hmi.elckerlyc.BMLBlockPeg;

import java.util.Set;

import asap.animationengine.motionunit.TimedMotionUnit;

/**
 * The restpose is a dynamic controller that handles all animation
 * related to the restpose. Only one restpose is active at a time, 
 * and it should normally be played after all other motion units are executed
 * @author hvanwelbergen
 *
 */
public interface RestPose
{
    /**
     * Play the rest pose at time time, on jointset joints     
     */
    void play(double time, Set<String>joints);
    
    /**
     * Create a transition TMU that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    TimedMotionUnit createTransitionToRest(Set<String>joints, double startTime, 
            String bmlId, String id, BMLBlockPeg bmlBlockPeg);
    
    /**
     * Create a transition TMU that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    TimedMotionUnit createTransitionToRest(Set<String>joints, double startTime, double duration, 
            String bmlId, String id, BMLBlockPeg bmlBlockPeg);
}
