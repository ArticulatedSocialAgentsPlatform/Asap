/*******************************************************************************
 *******************************************************************************/
package asap.faceengine;

import hmi.faceanimation.FaceControllerPose;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.PlanPlayer;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Specialized PlanPlayer that handles conflict resolution for TimedFaceUnit and manages the FaceController
 * @author hvanwelbergen
 */
public class FaceAnimationPlanPlayer implements PlanPlayer
{
    private final SingleThreadedPlanPlayer<TimedFaceUnit> defPlayer;
    private final FaceControllerPose facePose;
    
    public FaceAnimationPlanPlayer(FeedbackManager fbm, PlanManager<TimedFaceUnit> planManager, FaceControllerPose fcp)
    {
        defPlayer = new SingleThreadedPlanPlayer<>(fbm, planManager);
        facePose = fcp;
    }

    @Override
    public void play(double t)
    {
        facePose.clear();
        defPlayer.play(t);
        facePose.toTarget();
    }

    @Override
    public void stopPlanUnit(String bmlId, String id, double globalTime)
    {
        defPlayer.stopPlanUnit(bmlId, id, globalTime);
    }

    @Override
    public void stopBehaviourBlock(String bmlId, double time)
    {
        defPlayer.stopBehaviourBlock(bmlId, time);
    }

    @Override
    public void interruptPlanUnit(String bmlId, String id, double globalTime)
    {
        defPlayer.interruptPlanUnit(bmlId, id, globalTime);
    }

    @Override
    public void interruptBehaviourBlock(String bmlId, double time)
    {
        defPlayer.interruptBehaviourBlock(bmlId, time);
    }

    @Override
    public void reset(double time)
    {
        defPlayer.reset(time);
    }

    @Override
    public void setBMLBlockState(String bmlId, TimedPlanUnitState state)
    {
        defPlayer.setBMLBlockState(bmlId, state);

    }

    @Override
    public void shutdown()
    {
        defPlayer.shutdown();
    }

    @Override
    public void updateTiming(String bmlId)
    {
                
    }
}
