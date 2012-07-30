package asap.animationengine.gaze;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

public class GazeShiftTMU extends TimedAnimationUnit
{
    private final RestGaze restGaze;
    private AnimationPlayer aniPlayer;
    
    public GazeShiftTMU(BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationUnit m, PegBoard pb,
            RestGaze restGaze, AnimationPlayer ap)
    {
        super(bmlBlockPeg, bmlId, id, m, pb);
        this.restGaze = restGaze;
        this.aniPlayer = ap;
    }
    
    @Override
    protected void stopUnit(double time)
    {
        super.stopUnit(time);
        //aniPlayer.setRestGaze(restGaze);
    }

}
