package asap.animationengine.restpose;

import hmi.animation.VJoint;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;

/**
 * The restpose is a dynamic controller that handles all animation
 * related to the restpose. Only one restpose is active at a time, 
 * and it should normally be played after all other motion units are executed
 * @author hvanwelbergen
 *
 */
public interface RestPose
{
    void setAnimationPlayer(AnimationPlayer player);
    /**
     * Play the rest pose at time time, given the kinematicJoints and physicalJoint that are in use     
     */
    void play(double time, Set<String>kinematicJoints, Set<String>physicalJoints);
    
    /**
     * Create a transition TMU that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    TimedAnimationUnit createTransitionToRest(FeedbackManager fbm, Set<String>joints, double startTime, 
            String bmlId, String id, BMLBlockPeg bmlBlockPeg);
    
    /**
     * Create a transition TMU that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    TimedAnimationUnit createTransitionToRest(FeedbackManager fbm, Set<String>joints, double startTime, double duration, 
            String bmlId, String id, BMLBlockPeg bmlBlockPeg);
    
    /**
     * Determine the duration of a transition from vCurrent to the rest pose, taking
     * into account only information from joints
     */
    double getTransitionToRestDuration(VJoint vCurrent, Set<String>joints);
    
    /**
     * Create a MotionUnit that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    AnimationUnit createTransitionToRest(Set<String>joints);
    
    /**
     * Sets the restpose to prev, next, curr on the animationplayer 
     */
    void setRestPose();
}
