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
 * Simply looks forward (all influenced joints are 0)
 * @author hvanwelbergen
 *
 */
public class ForwardRestGaze implements RestGaze
{
    private AnimationPlayer aPlayer;
    private final GazeInfluence influence;
    
    public ForwardRestGaze(GazeInfluence influence)
    {
        this.influence = influence;
    }
    
    @Override
    public RestGaze copy(AnimationPlayer player)
    {
        ForwardRestGaze restGaze = new ForwardRestGaze(influence);
        restGaze.setAnimationPlayer(player);
        return restGaze;
    }

    @Override
    public void setAnimationPlayer(AnimationPlayer player)
    {
        aPlayer = player;        
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {
        // TODO Auto-generated method stub        
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, String bmlId,
            String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, double duration,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getTransitionToRestDuration(VJoint vCurrent, Set<String> joints)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public AnimationUnit createTransitionToRest(Set<String> joints)
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
        // TODO Auto-generated method stub
        return null;
    }

}
