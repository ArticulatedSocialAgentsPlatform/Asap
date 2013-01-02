package asap.animationengine.restpose;

import hmi.animation.SkeletonPose;
import hmi.animation.VJoint;
import hmi.animation.VObjectTransformCopier;
import hmi.math.Quat4f;
import hmi.util.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.MovementTimingUtils;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.transitions.SlerpTransitionToPoseMU;
import asap.animationengine.transitions.T1RTransitionToPoseMU;
import asap.animationengine.transitions.TransitionMU;
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

    public void setAnimationPlayer(AnimationPlayer player)
    {
        this.player = player;
        poseTree = player.getVCurr().copyTree("rest-");
        for (VJoint vj : poseTree.getParts())
        {
            if (vj.getSid() != null)
            {
                vj.setRotation(Quat4f.getIdentity());
            }
        }
        if (pose != null)
        {
            pose.setTargets(poseTree.getParts().toArray(new VJoint[poseTree.getParts().size()]));
            pose.setToTarget();
        }
    }

    public RestPose copy(AnimationPlayer player)
    {
        SkeletonPoseRestPose copy = new SkeletonPoseRestPose();
        if (pose != null)
        {
            copy.pose = pose.untargettedCopy();
        }
        copy.setAnimationPlayer(player);
        return copy;
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {
        if (poseTree == null) return;
        float q[] = new float[4];
        for (VJoint vj : poseTree.getParts())
        {
            if (!kinematicJoints.contains(vj.getSid()) && !physicalJoints.contains(vj.getSid()))
            {                
                vj.getRotation(q);
                VJoint vjSet = player.getVNext().getPartBySid(vj.getSid());
                if (vjSet != null)
                {
                    vjSet.setRotation(q);
                }                
            }
        }        
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, String bmlId,
            String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        return createTransitionToRest(fbm, joints, startTime, 1, bmlId, id, bmlBlockPeg, pb);
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, TimePeg startPeg, TimePeg endPeg,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        TransitionMU mu = createTransitionToRest(joints);
        mu.addKeyPosition(new KeyPosition("start", 0));
        mu.addKeyPosition(new KeyPosition("end", 1));
        TimedAnimationMotionUnit tmu = new TimedAnimationMotionUnit(fbm, bmlBlockPeg, bmlId, id, mu, pb);
        tmu.setTimePeg("start", startPeg);
        tmu.setTimePeg("end", endPeg);
        tmu.setState(TimedPlanUnitState.LURKING);
        return tmu;
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, double duration,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        TimePeg startPeg = new TimePeg(bmlBlockPeg);
        startPeg.setGlobalValue(startTime);
        TimePeg endPeg = new OffsetPeg(startPeg, duration);
        return createTransitionToRest(fbm, joints, startPeg, endPeg, bmlId, id, bmlBlockPeg, pb);
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

    public PostureShiftTMU createPostureShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
            throws MUSetupException
    {
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        for (String joint : pose.getPartIds())
        {
            targetJoints.add(player.getVNext().getPartBySid(joint));
            startJoints.add(player.getVCurr().getPartBySid(joint));
        }

        AnimationUnit mu;
        if (pose.getConfigType().equals("R"))
        {
            mu = new SlerpTransitionToPoseMU(startJoints, targetJoints, pose.getConfig());
        }
        else if (pose.getConfigType().equals("T1R"))
        {
            mu = new T1RTransitionToPoseMU(startJoints, targetJoints, pose.getConfig());
        }
        else
        {
            return null;
        }
        return new PostureShiftTMU(bbf, bmlBlockPeg, bmlId, id, mu.copy(player), pb, this, player);
    }

    @Override
    public void setResource(Resources res)
    {

    }

}
