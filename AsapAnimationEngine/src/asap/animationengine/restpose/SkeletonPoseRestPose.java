package asap.animationengine.restpose;

import hmi.animation.SkeletonPose;
import hmi.animation.VJoint;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.OffsetPeg;
import hmi.elckerlyc.TimePeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.TimedPlanUnitState;
import hmi.math.Quat4f;

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
    
    public SkeletonPoseRestPose(FeedbackManager bbf)
    {
        feedbackManager = bbf;
    }
    
    public SkeletonPoseRestPose(SkeletonPose pose,FeedbackManager bbf)
    {
        this.pose = pose;
        feedbackManager = bbf;
    }
    
    public SkeletonPoseRestPose(SkeletonPose pose, AnimationPlayer player,FeedbackManager bbf)
    {
        this(pose,bbf);
        setAnimationPlayer(player);        
    }
    
    public void setAnimationPlayer(AnimationPlayer player)
    {
        this.player = player;        
        poseTree = player.getVCurr().copyTree("rest-");
        if(pose!=null)
        {
            pose.setTargets(poseTree.getParts().toArray(new VJoint[0]));
            pose.setToTarget();
        }
        else
        {
            for(VJoint vj: poseTree.getParts())
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
