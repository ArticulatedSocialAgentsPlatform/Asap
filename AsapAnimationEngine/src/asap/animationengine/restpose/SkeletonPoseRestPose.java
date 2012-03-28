package asap.animationengine.restpose;

import hmi.animation.SkeletonPose;
import hmi.animation.VJoint;
import hmi.animation.VObjectTransformCopier;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.math.Quat4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.MovementTimingUtils;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.transitions.SlerpTransitionToPoseMU;
import asap.animationengine.transitions.TransitionMU;
import asap.animationengine.transitions.TransitionTMU;

/**
 * A simple static rest-pose implementation; the restpose is specified by a SkeletonPose
 * @author hvanwelbergen
 */
public class SkeletonPoseRestPose implements RestPose
{
    private AnimationPlayer player;
    private VJoint poseTree;
    private final FeedbackManager feedbackManager;
    private SkeletonPose pose;
    private final PegBoard pegBoard;
    
    public SkeletonPoseRestPose(FeedbackManager bbf, PegBoard pb)
    {
        feedbackManager = bbf;
        pegBoard = pb;
    }

    public SkeletonPoseRestPose(SkeletonPose pose, FeedbackManager bbf,PegBoard pb)
    {
        this.pose = pose;
        feedbackManager = bbf;
        pegBoard = pb;
    }

    public SkeletonPoseRestPose(SkeletonPose pose, AnimationPlayer player, FeedbackManager bbf, PegBoard pb)
    {
        this(pose, bbf,pb);
        setAnimationPlayer(player);
    }

    public void setAnimationPlayer(AnimationPlayer player)
    {
        this.player = player;
        poseTree = player.getVCurr().copyTree("rest-");
        if (pose != null)
        {
            pose.setTargets(poseTree.getParts().toArray(new VJoint[poseTree.getParts().size()]));
            pose.setToTarget();
        }
        else
        {
            for (VJoint vj : poseTree.getParts())
            {
                vj.setRotation(Quat4f.getIdentity());
            }
        }
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {

    }

    @Override
    public TimedAnimationUnit createTransitionToRest(Set<String> joints, double startTime, String bmlId, String id, BMLBlockPeg bmlBlockPeg)
    {
        return createTransitionToRest(joints, startTime, 1, bmlId, id, bmlBlockPeg);
    }

    @Override
    public TimedAnimationUnit createTransitionToRest(Set<String> joints, double startTime, double duration, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg)
    {
        TransitionMU mu = createTransitionToRest(joints);
        mu.addKeyPosition(new KeyPosition("start", 0));
        mu.addKeyPosition(new KeyPosition("end", 1));
        TimedAnimationUnit tmu = new TransitionTMU(feedbackManager, bmlBlockPeg, bmlId, id, mu, pegBoard);
        TimePeg startPeg = new TimePeg(bmlBlockPeg);
        startPeg.setGlobalValue(startTime);
        tmu.setTimePeg("start", startPeg);
        TimePeg endPeg = new OffsetPeg(startPeg, duration);
        tmu.setTimePeg("end", endPeg);
        tmu.setState(TimedPlanUnitState.LURKING);
        return tmu;
    }

    @Override
    public TransitionMU createTransitionToRest(Set<String> joints)
    {
        float rotations[] = new float[joints.size() * 4];
        int i = 0;
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        for (String joint : joints)
        {
            VJoint vj = poseTree.getPartBySid(joint);
            vj.getRotation(rotations, i);
            targetJoints.add(player.getVNext().getPartBySid(joint));
            startJoints.add(player.getVCurr().getPartBySid(joint));
            i += 4;
        }
        TransitionMU mu = new SlerpTransitionToPoseMU(targetJoints, startJoints, rotations);
        mu.setStartPose();
        return mu;
    }

    @Override
    public void setRestPose()
    {
        VObjectTransformCopier.newInstanceFromVJointTree(poseTree, player.getVCurr(), "T1R").copyConfig();
        VObjectTransformCopier.newInstanceFromVJointTree(poseTree, player.getVNext(), "T1R").copyConfig();
        VObjectTransformCopier.newInstanceFromVJointTree(poseTree, player.getVPrev(), "T1R").copyConfig();
    }

    @Override
    public double getTransitionToRestDuration(VJoint vCurrent, Set<String> joints)
    {
        double duration = MovementTimingUtils.getFittsMaximumLimbMovementDuration(vCurrent,poseTree,joints);
        if(duration>0)return duration;
        return 1;        
    }

}
