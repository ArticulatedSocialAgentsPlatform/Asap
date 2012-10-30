package asap.animationengine.ace.lmp;

import hmi.animation.Hanim;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.PostureConstraint;
import asap.motionunit.TMUPlayException;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.TimePegAndConstraint;

/**
 * Hand movement
 * @author hvanwelbergen
 * 
 */
public class LMPHandMove extends LMP
{
    private final AnimationPlayer aniPlayer;
    private final ImmutableSet<String>kinematicJoints;

    public LMPHandMove(String scope, List<PostureConstraint> pcVec, FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId,
            PegBoard pegBoard, AnimationPlayer aniPlayer)
    {
        super(fbm, bmlPeg, bmlId, behId, pegBoard);
        this.aniPlayer = aniPlayer;
        
        if (scope == null)
        {
            scope = "right_arm";
        }

        if (scope.equals("left_arm"))
        {
            kinematicJoints = ImmutableSet.copyOf(Hanim.LEFTHAND_JOINTS);
            for(PostureConstraint pc:pcVec)
            {
                pc.getPosture().mirror(null);
            }
        }
        else
        {
            kinematicJoints = ImmutableSet.copyOf(Hanim.RIGHTHAND_JOINTS);
        }
        
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return kinematicJoints;
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        return ImmutableSet.of();
    }

    @Override
    public void updateTiming(double time) throws TMUPlayException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasValidTiming()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        // TODO Auto-generated method stub

    }

}
