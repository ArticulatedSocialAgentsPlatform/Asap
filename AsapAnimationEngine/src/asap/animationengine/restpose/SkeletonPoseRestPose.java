package asap.animationengine.restpose;

import hmi.animation.SkeletonPose;
import hmi.animation.VJoint;
import hmi.animation.VObjectTransformCopier;
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
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * A simple static rest-pose implementation; the restpose is specified by a SkeletonPose
 * @author hvanwelbergen
 */
public class SkeletonPoseRestPose implements RestPose
{
    private AnimationPlayer player;
    private VJoint poseTree;
    private SkeletonPose pose;

    public SkeletonPoseRestPose()
    {

    }

    public SkeletonPoseRestPose(SkeletonPose pose)
    {
        this.pose = pose;
    }

    private void setAnimationPlayer(AnimationPlayer player)
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

    public RestPose copy(AnimationPlayer player)
    {
        SkeletonPoseRestPose copy = new SkeletonPoseRestPose();
        if (pose!=null)
        {
            copy.pose = pose.untargettedCopy();
        }
        copy.setAnimationPlayer(player);
        return copy;
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {

    }

    @Override
    public TimedAnimationUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        return createTransitionToRest(fbm, joints, startTime, 1, bmlId, id, bmlBlockPeg, pb);
    }

    @Override
    public TimedAnimationUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, double duration,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        TransitionMU mu = createTransitionToRest(joints);
        mu.addKeyPosition(new KeyPosition("start", 0));
        mu.addKeyPosition(new KeyPosition("end", 1));
        TimedAnimationUnit tmu = new TransitionTMU(fbm, bmlBlockPeg, bmlId, id, mu, pb);
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
        double duration = MovementTimingUtils.getFittsMaximumLimbMovementDuration(vCurrent, poseTree, joints);
        if (duration > 0) return duration;
        return 1;
    }

    @Override
    public void setParameterValue(String name, String value)
    {

    }

}
