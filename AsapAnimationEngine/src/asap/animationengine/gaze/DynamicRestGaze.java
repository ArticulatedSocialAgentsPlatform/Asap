package asap.animationengine.gaze;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;

/**
 * Dynamically keeps the gaze on target. Creates transitions that are also dynamic.
 * @author harsens
 * 
 */
public class DynamicRestGaze implements RestGaze
{
    private AnimationPlayer aniPlayer;
    private String target;
    
    public DynamicRestGaze()
    {
        
    }

    @Override
    public DynamicRestGaze copy(AnimationPlayer player)
    {
        DynamicRestGaze copy = new DynamicRestGaze();
        copy.setAnimationPlayer(aniPlayer);
        return copy;
    }

    @Override
    public void setAnimationPlayer(AnimationPlayer player)
    {
        aniPlayer = player;
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {

        // TODO Auto-generated method stub

    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, double startTime, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, double startTime, double duration,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getTransitionToRestDuration()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public AnimationUnit createTransitionToRest()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setRestPose()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public GazeShiftTMU createGazeShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
            throws MUSetupException
    {
        DynamicGazeMU mu = new DynamicGazeMU();
        mu.target = target;
        return new GazeShiftTMU(bmlBlockPeg, bmlId, id, mu.copy(aniPlayer), pb, this, aniPlayer);        
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, TimePeg startPeg, TimePeg endPeg, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
