package asap.animationengine.gaze;

import hmi.math.Vec3f;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Simply looks forward (all influenced joints are 0)
 * @author hvanwelbergen
 *
 */
public class ForwardRestGaze implements RestGaze
{
    private AnimationPlayer aPlayer;
    private final GazeInfluence influence;
    //private ImmutableSet<String> influencedJoints;
    
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
        //influencedJoints = GazeUtils.getJoints(player.getVNext(), influence);
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {
                
    }

    
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, TimePeg startPeg, TimePeg endPeg,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        AnimationUnit mu = createTransitionToRest();
        TimedAnimationMotionUnit tmu = mu.createTMU(fbm, bmlBlockPeg, bmlId, id, pb);
        tmu.setTimePeg("start", startPeg);
        tmu.setTimePeg("ready", endPeg);
        tmu.setState(TimedPlanUnitState.LURKING);
        return tmu;
    }
    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, double startTime, String bmlId,
            String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        TimePeg startPeg = new TimePeg(bmlBlockPeg);
        startPeg.setGlobalValue(startTime);
        TimePeg endPeg = new OffsetPeg(startPeg, getTransitionToRestDuration());
        return createTransitionToRest(fbm, startPeg, endPeg, bmlId, id, bmlBlockPeg, pb);
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, double startTime, double duration,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        TimePeg startPeg = new TimePeg(bmlBlockPeg);
        startPeg.setGlobalValue(startTime);
        TimePeg endPeg = new OffsetPeg(startPeg, duration);
        return createTransitionToRest(fbm, startPeg, endPeg, bmlId, id, bmlBlockPeg, pb);
    }

    @Override
    public double getTransitionToRestDuration()
    {
        return 2;
    }

    @Override
    public AnimationUnit createTransitionToRest()
    {
        DynamicGazeMU mu = new DynamicGazeMU();
        mu.setPlayer(aPlayer);
        mu.setInfluence(influence);        
        mu.setGazeDirection(Vec3f.getVec3f(0,0,1));        
        return mu;
    }

    @Override
    public GazeShiftTMU createGazeShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
            throws MUSetupException
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
        throw new ParameterException("ForwardRestGaze doesn't support any parameters");
    }

    
}
