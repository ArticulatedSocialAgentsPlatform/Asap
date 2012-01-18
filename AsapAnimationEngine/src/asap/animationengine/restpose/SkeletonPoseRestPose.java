package asap.animationengine.restpose;

import hmi.animation.SkeletonPose;
import hmi.animation.VJoint;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.OffsetPeg;
import hmi.elckerlyc.TimePeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.TimedPlanUnitState;

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
    private final AnimationPlayer player;
    private final VJoint poseTree;
    private final FeedbackManager feedbackManager;
    
    public SkeletonPoseRestPose(SkeletonPose pose, AnimationPlayer player,FeedbackManager bbf)
    {
        this.player = player;
        feedbackManager = bbf;
        poseTree = player.getVCurr().copyTree("rest-");
        pose.setTargets(poseTree.getParts().toArray(new VJoint[0]));
        pose.setToTarget();
    }
    
    @Override
    public void play(double time, Set<String> joints)
    {
                
    }

    @Override
    public TimedMotionUnit createTransitionToRest(Set<String> joints, double startTime, String bmlId, String id, BMLBlockPeg bmlBlockPeg)
    {
        return createTransitionToRest(joints,startTime, 1, bmlId,id,bmlBlockPeg);
    }

    @Override
    public TimedMotionUnit createTransitionToRest(Set<String> joints, double startTime, double duration, String bmlId, String id, BMLBlockPeg bmlBlockPeg)
    {
        float rotations[]=new float[joints.size()*4];
        int i=0;
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        for(String joint:joints)
        {
            VJoint vj = poseTree.getPartBySid(joint);
            vj.getRotation(rotations, i);
            targetJoints.add(player.getVNext().getPartBySid(joint));
            startJoints.add(player.getVCurr().getPartBySid(joint));
            i+=4;
        }
        TransitionMU mu = new SlerpTransitionToPoseMU(targetJoints, startJoints, rotations);
        mu.addKeyPosition(new KeyPosition("start",0));
        mu.addKeyPosition(new KeyPosition("end",1));
        TimedMotionUnit tmu = new TransitionTMU(feedbackManager, bmlBlockPeg, bmlId, id, mu);
        TimePeg startPeg = new TimePeg(bmlBlockPeg);
        startPeg.setGlobalValue(startTime);
        tmu.setTimePeg("start", startPeg);
        TimePeg endPeg = new OffsetPeg(startPeg,duration);
        tmu.setTimePeg("end", endPeg);
        tmu.setState(TimedPlanUnitState.LURKING);
        return tmu;
    }

}
