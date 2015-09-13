/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
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
    TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, double startTime, 
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb) throws TMUSetupException;
    
    /**
     * Create a transition TMU that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, double startTime, double duration, 
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb) throws TMUSetupException;
    
    /**
     * Create a transition TMU that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, TimePeg startPeg, TimePeg endPeg,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb) throws TMUSetupException;
    
    double getTransitionToRestDuration();
    
    /**
     * Create a MotionUnit that moves the joints from their current position 
     * to a position dictated by this resting pose.  
     */
    AnimationUnit createTransitionToRest() throws MUSetupException;
    
    void setParameterValue(String name, String value) throws ParameterException;
    
    void setFloatParameterValue(String name, float value) throws ParameterException;
    
    GazeShiftTMU createGazeShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, 
            String bmlId, String id, PegBoard pb) throws MUSetupException;
    
    Set<String> getKinematicJoints();    
}
