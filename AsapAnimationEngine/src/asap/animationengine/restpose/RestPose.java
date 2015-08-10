/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.restpose;

import hmi.animation.VJoint;
import hmi.util.Resources;

import java.util.Collection;
import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;

/**
 * The restpose is a dynamic controller that handles all animation
 * related to the restpose. Only one restpose is active at a time, 
 * and it should normally be played after all other motion units are executed
 * @author hvanwelbergen
 *
 */
public interface RestPose
{
    RestPose copy(AnimationPlayer player);
    
    void setAnimationPlayer(AnimationPlayer player);
    
    void setResource(Resources res);
    /**
     * Play the rest pose at time time, given the kinematicJoints and physicalJoint that are in use     
     */
    void play(double time, Set<String>kinematicJoints, Set<String>physicalJoints);
    
    /**
     * Create a transition TMU that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    TimedAnimationUnit createTransitionToRest(FeedbackManager fbm, Set<String>joints, double startTime, 
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb);
    
    /**
     * Create a transition TMU that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String>joints, double startTime, double duration, 
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb);
    
    /**
     * Create a transition TMU that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String>joints, TimePeg startPeg, TimePeg endPeg, 
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb);
    
    /**
     * Determine the duration of a transition from vCurrent to the rest pose, taking
     * into account only information from joints
     */
    double getTransitionToRestDuration(VJoint vCurrent, Set<String>joints);
    
    /**
     * Create a MotionUnit that moves the joints from their current position 
     * to a position dictated by this resting pose. The motionunit steers vNext.
     */
    AnimationUnit createTransitionToRest(Set<String>joints);
    
    /**
     * Create a MotionUnit that moves the joints from their current position 
     * to a position dictated by this resting pose. The motionunit steers joints.
     */
    AnimationUnit createTransitionToRestFromVJoints(Collection<VJoint> joints);
    
    /**
     * Sets this rest posture as the initial one, that is: e.g. sets the restpose to prev, next, curr on the animationplayer 
     */
    void initialRestPose(double time);
    
    /**
     * Starts the rest pose, called before first play via the PostureShiftTMU
     */
    void start(double time);
    
    void setParameterValue(String name, String value) throws ParameterException;
    
    PostureShiftTMU createPostureShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, 
            String bmlId, String id, PegBoard pb) throws MUSetupException;    
}
