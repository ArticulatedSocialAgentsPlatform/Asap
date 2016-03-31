/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.restpose;

import hmi.animation.SkeletonPose;
import hmi.animation.VJoint;
import hmi.animation.VObjectTransformCopier;
import hmi.math.Quat4f;
import hmi.util.Resources;

import java.util.ArrayList;
import java.util.Collection;
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
    private VJoint poseTree;        //Holds the pose on a VJoint structure. Joints not in the pose are set to have identity rotation.
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
                VJoint vjSet = player.getVNextPartBySid(vj.getSid());
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
        TimedAnimationMotionUnit tmu = new TimedAnimationMotionUnit(fbm, bmlBlockPeg, bmlId, id, mu, pb, player);
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
            targetJoints.add(player.getVNextPartBySid(joint));
            startJoints.add(player.getVCurrPartBySid(joint));
            i += 4;
        }
        TransitionMU mu = new SlerpTransitionToPoseMU(targetJoints, startJoints, rotations);
        mu.setStartPose();
        return mu;
    }
    
    public TransitionMU createTransitionToRestFromVJoints(Collection<VJoint> joints)
    {
        float rotations[] = new float[joints.size() * 4];
        int i = 0;
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        for (VJoint joint : joints)
        {
            VJoint vj = poseTree.getPartBySid(joint.getSid());
            vj.getRotation(rotations, i);
            targetJoints.add(joint);
            startJoints.add(player.getVCurrPartBySid(joint.getSid()));
            i += 4;
        }
        TransitionMU mu = new SlerpTransitionToPoseMU(targetJoints, startJoints, rotations);
        mu.setStartPose();
        return mu;
    }

    @Override
    public void initialRestPose(double time)
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

    private void setRotConfig(VJoint poseTree, int startIndex, float[] config)
    {
        int i = 0;
        for(VJoint vj:poseTree.getParts())
        {
            if(vj.getSid()!=null)
            {
                vj.getRotation(config, startIndex+i);
                i+=4;
            }
        }
    }
    
    public PostureShiftTMU createPostureShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
            throws MUSetupException
    {
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        
        for(VJoint vj:poseTree.getParts())
        {
            if(vj.getSid()!=null)
            {
                targetJoints.add(player.getVNextPartBySid(vj.getSid()));
                startJoints.add(player.getVCurrPartBySid(vj.getSid()));
            }
        }
        
        AnimationUnit mu;
        if (pose.getConfigType().equals("R"))
        {
            float config[]= new float[targetJoints.size()*4];
            setRotConfig(poseTree, 0, config);
            mu = new SlerpTransitionToPoseMU(startJoints, targetJoints, config);
        }
        else if (pose.getConfigType().equals("T1R"))
        {
            float config[]= new float[targetJoints.size()*4+3];
            poseTree.getTranslation(config);
            setRotConfig(poseTree, 3, config);
            mu = new T1RTransitionToPoseMU(startJoints, targetJoints, config);
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

    @Override
    public void start(double time)
    {
                
    }
}
