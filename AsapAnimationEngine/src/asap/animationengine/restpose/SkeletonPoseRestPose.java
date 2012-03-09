package asap.animationengine.restpose;

import hmi.animation.Hanim;
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
import hmi.math.Vec3f;
import hmi.neurophysics.FittsLaw;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.TimedMotionUnit;
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
            pose.setTargets(poseTree.getParts().toArray(new VJoint[0]));
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
    public TimedMotionUnit createTransitionToRest(Set<String> joints, double startTime, String bmlId, String id, BMLBlockPeg bmlBlockPeg)
    {
        return createTransitionToRest(joints, startTime, 1, bmlId, id, bmlBlockPeg);
    }

    @Override
    public TimedMotionUnit createTransitionToRest(Set<String> joints, double startTime, double duration, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg)
    {
        TransitionMU mu = createTransitionToRest(joints);
        mu.addKeyPosition(new KeyPosition("start", 0));
        mu.addKeyPosition(new KeyPosition("end", 1));
        TimedMotionUnit tmu = new TransitionTMU(feedbackManager, bmlBlockPeg, bmlId, id, mu, pegBoard);
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

    private double getFittsDuration(String id, String rootId, VJoint vCurrent)
    {
        VJoint currentJoint = vCurrent.getPartBySid(id);
        VJoint currentRootJoint = vCurrent.getPartBySid(rootId);
        VJoint poseJoint = poseTree.getPartBySid(id);
        VJoint poseRootJoint = poseTree.getPartBySid(rootId);
        if (currentJoint != null && poseJoint != null && currentRootJoint != null && poseRootJoint != null)
        {
            float[] relPos = Vec3f.getVec3f();
            float[] restPos = Vec3f.getVec3f();
            currentJoint.getPathTranslation(currentRootJoint, relPos);
            poseJoint.getPathTranslation(poseRootJoint, restPos);
            Vec3f.sub(relPos, restPos);
            return FittsLaw.getHandTrajectoryDuration(Vec3f.length(relPos));
        }
        return -1;
    }

    @Override
    public double getTransitionToRestDuration(VJoint vCurrent, Set<String> joints)
    {
        double duration = 1;
        double lastSetDur = -1;
        if (joints.contains(Hanim.r_wrist))
        {
            double d = getFittsDuration(Hanim.r_wrist, Hanim.r_shoulder, vCurrent);
            if (d > 0) lastSetDur = d;
        }
        if (joints.contains(Hanim.l_wrist))
        {
            double d = getFittsDuration(Hanim.l_wrist, Hanim.l_shoulder, vCurrent);
            if (d > 0 && d > lastSetDur) lastSetDur = d;
        }        
        if(lastSetDur>0)return lastSetDur;
        return duration;
    }

}
