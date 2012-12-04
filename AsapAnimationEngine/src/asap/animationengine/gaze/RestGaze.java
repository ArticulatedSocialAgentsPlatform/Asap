package asap.animationengine.gaze;

import hmi.animation.VJoint;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;

/**
 * The restgaze is a dynamic controller that handles all animation
 * related to the gaze to and from the rest position. Only one restgaze is active at a time, 
 * and it should normally be played after all other motion units are executed
 * @author hvanwelbergen
 */
public interface RestGaze
{
    RestGaze copy(AnimationPlayer player);

    void setAnimationPlayer(AnimationPlayer player);
    
    /**
     * Play the rest pose at time time, given the kinematicJoints and physicalJoint that are in use     
     */
    void play(double time, Set<String>kinematicJoints, Set<String>physicalJoints);
    
    /**
     * Create a transition TMU that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String>joints, double startTime, 
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb);
    
    /**
     * Create a transition TMU that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String>joints, double startTime, double duration, 
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb);
    
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
    
    void setParameterValue(String name, String value) throws ParameterException;
    
    GazeShiftTMU createGazeShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, 
            String bmlId, String id, PegBoard pb) throws MUSetupException;
}
