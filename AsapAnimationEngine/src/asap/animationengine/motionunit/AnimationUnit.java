/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.motionunit;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.motionunit.MotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

/**
 * An animation, typically with a peak-like structure, parameterized by a parameter set 
 * Contains a set of keys that map to 'world' time to animation time
 * @author welberge
 */
public interface AnimationUnit extends MotionUnit
{
    
    Set<String> getPhysicalJoints();
    Set<String> getKinematicJoints();
    Set<String> getAdditiveJoints();
    
    /**
     * Creates the TimedMotionUnit corresponding to this motion unit
     * @param bmlId     BML block id
     * @param id         behaviour id
     * @return          the TMU
     */
    TimedAnimationMotionUnit createTMU(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId,String id, PegBoard pb);
    
    /**
     * Create a copy of this motion unit and link it to the animationplayer
     */
    AnimationUnit copy(AnimationPlayer p) throws MUSetupException;
}
