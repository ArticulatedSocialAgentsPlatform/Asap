/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.restpose;

import hmi.animation.SkeletonPose;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.math.Quat4f;
import hmi.physics.controller.BalanceController;
import hmi.physics.controller.ControllerParameterException;
import hmi.physics.controller.PhysicalController;
import hmi.util.Resources;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.MovementTimingUtils;
import asap.animationengine.controller.CompoundController;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.transitions.SlerpTransitionToPoseMU;
import asap.animationengine.transitions.TransitionMU;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnitState;

import com.google.common.collect.Sets;

/**
 * Manages the rest pose with a physical balance controller
 * @author welberge
 */
@Slf4j
public class PhysicalBalanceRestPose implements RestPose
{
    private PhysicalController balanceController = new BalanceController();
    private List<PhysicalController> optionalControllers = new ArrayList<PhysicalController>();

    private AnimationPlayer player;
    private Resources resource;
    private SkeletonPose restPose;
    private VJoint restPoseTree;
    private List<PhysicalController> prevControllers = new ArrayList<PhysicalController>();

    @Override
    public RestPose copy(AnimationPlayer player)
    {
        PhysicalBalanceRestPose rp = new PhysicalBalanceRestPose();
        if (restPose != null)
        {
            rp.restPose = restPose.untargettedCopy();
        }

        rp.balanceController = balanceController.copy(player.getPHuman());
        rp.setAnimationPlayer(player);
        rp.setResource(resource);
        rp.optionalControllers = new ArrayList<PhysicalController>();

        for (PhysicalController cc : optionalControllers)
        {
            rp.optionalControllers.add(cc.copy(player.getPHuman()));
        }
        return rp;
    }

    @Override
    public void setAnimationPlayer(AnimationPlayer player)
    {
        this.player = player;
        balanceController.setPhysicalHumanoid(player.getPHuman());
        restPoseTree = player.getVCurr().copyTree("rest-");

        for (VJoint vj : restPoseTree.getParts())
        {
            vj.setRotation(Quat4f.getIdentity());
        }
        if (restPose != null)
        {
            restPose.setTargets(restPoseTree.getParts().toArray(new VJoint[restPoseTree.getParts().size()]));
            restPose.setToTarget();
        }
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {
        if (Sets.intersection(balanceController.getRequiredJointIDs(), kinematicJoints).size() == 0)
        {
            player.addController(balanceController);
        }

        ArrayList<PhysicalController> currentControllers = new ArrayList<PhysicalController>();
        for (PhysicalController cc : optionalControllers)
        {
            if (Sets.intersection(cc.getRequiredJointIDs(), kinematicJoints).size() == 0)
            {
                if (!prevControllers.contains(cc))
                {
                    cc.reset();
                }
                currentControllers.add(cc);
                player.addController(cc);
            }
        }
        prevControllers = currentControllers;
    }

    private Set<String> getKinematicTransitionJoints(Set<String> transitionJoints)
    {
        Set<String> kinTransJoints = new HashSet<String>(transitionJoints);
        for (PhysicalController cc : optionalControllers)
        {
            if (transitionJoints.containsAll(cc.getRequiredJointIDs()))
            {
                kinTransJoints.removeAll(cc.getRequiredJointIDs());
                if (transitionJoints.containsAll(cc.getDesiredJointIDs()))
                {
                    kinTransJoints.removeAll(cc.getDesiredJointIDs());
                }
            }
        }
        return kinTransJoints;
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, String bmlId,
            String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        return createTransitionToRest(fbm, joints, startTime, 1, bmlId, id, bmlBlockPeg, pb);
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, double duration,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        TransitionMU mu = createTransitionToRest(joints);
        mu.addKeyPosition(new KeyPosition("start", 0));
        mu.addKeyPosition(new KeyPosition("end", 1));
        TimedAnimationMotionUnit tmu = new TimedAnimationMotionUnit(fbm, bmlBlockPeg, bmlId, id, mu, pb, player);
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
        Set<String> kinJoints = getKinematicTransitionJoints(joints);
        float rotations[] = new float[kinJoints.size() * 4];
        int i = 0;
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        for (String joint : kinJoints)
        {
            VJoint vj = restPoseTree.getPartBySid(joint);
            vj.getRotation(rotations, i);
            targetJoints.add(player.getVNextPartBySid(joint));
            startJoints.add(player.getVCurrPartBySid(joint));
            i += 4;
        }
        TransitionMU mu = new SlerpTransitionToPoseMU(targetJoints, startJoints, rotations);
        mu.setStartPose();
        return mu;
    }

    @Override
    public TransitionMU createTransitionToRestFromVJoints(Collection<VJoint> joints)
    {
        Set<String> kinJoints = getKinematicTransitionJoints(VJointUtils.transformToSidSet(joints));
        float rotations[] = new float[kinJoints.size() * 4];
        int i = 0;
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        for (String joint : kinJoints)
        {
            VJoint vj = restPoseTree.getPartBySid(joint);
            vj.getRotation(rotations, i);
            for (VJoint v : joints)
            {
                if (v.getSid().equals(joint))
                {
                    targetJoints.add(v);
                }
            }
            startJoints.add(player.getVCurrPartBySid(joint));
            i += 4;
        }
        TransitionMU mu = new SlerpTransitionToPoseMU(targetJoints, startJoints, rotations);
        mu.setStartPose();
        return mu;
    }

    @Override
    public double getTransitionToRestDuration(VJoint vCurrent, Set<String> joints)
    {
        double duration = MovementTimingUtils.getFittsMaximumLimbMovementDuration(vCurrent, restPoseTree, joints);
        if (duration > 0) return duration;
        return 1;
    }

    @Override
    public void initialRestPose(double time)
    {

    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("xmlcontrollers"))
        {
            String fileNames[] = value.split(",");
            for (String fileName : fileNames)
            {
                CompoundController cc = new CompoundController();
                try
                {
                    cc.readXML(resource.getReader(fileName.trim()));
                }
                catch (IOException e)
                {
                    throw new ParameterException("Cannot load compound controller " + value, e);
                }
                optionalControllers.add(cc);
            }
        }
        else if (name.equals("restpose"))
        {
            try
            {
                restPose = new SkeletonPose(new XMLTokenizer(resource.getReader(value)));
            }
            catch (IOException e)
            {
                throw new ParameterException("Cannot load SkeletonPose " + value, e);
            }
        }
        else
        {
            try
            {
                balanceController.setParameterValue(name, value);
            }
            catch (ControllerParameterException e)
            {
                log.warn("ControllerParameterException with parameter " + name + " = " + value, e);
            }
        }
    }

    @Override
    public PostureShiftTMU createPostureShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
            throws MUSetupException
    {
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        AnimationUnit mu = new SlerpTransitionToPoseMU(startJoints, targetJoints, new float[0]);
        return new PostureShiftTMU(bbf, bmlBlockPeg, bmlId, id, mu.copy(player), pb, this, player);
    }

    @Override
    public void setResource(Resources res)
    {
        resource = res;
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, TimePeg startPeg, TimePeg endPeg,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void start(double time)
    {

    }

}
