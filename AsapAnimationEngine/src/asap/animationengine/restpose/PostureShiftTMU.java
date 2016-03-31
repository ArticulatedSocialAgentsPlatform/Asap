/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.restpose;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

/**
 * Runs the TimedAnimationUnit, ends with setting the new rest pose state.
 * @author welberge
 * 
 */
public class PostureShiftTMU extends TimedAnimationMotionUnit
{

    private final RestPose restPose;
    private AnimationPlayer aniPlayer;

    public PostureShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationUnit mu, PegBoard pb,
            RestPose restPose, AnimationPlayer ap)
    {
        super(bbf, bmlBlockPeg, bmlId, id, mu, pb, ap);
        this.restPose = restPose;
        aniPlayer = ap;
    }

    @Override
    protected void stopUnit(double time)
    {
        super.stopUnit(time);
        aniPlayer.setRestPose(restPose);
        restPose.start(time);
    }
}
