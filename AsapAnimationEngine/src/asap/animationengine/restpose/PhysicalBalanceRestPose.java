package asap.animationengine.restpose;

import hmi.animation.VJoint;
import hmi.physics.controller.BalanceController;
import hmi.physics.controller.ControllerParameterException;
import hmi.physics.controller.PhysicalController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.transitions.SlerpTransitionToPoseMU;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

import com.google.common.collect.Sets;

/**
 * Manages the rest pose with a physical balance controller
 * @author welberge
 */
@Slf4j
public class PhysicalBalanceRestPose implements RestPose
{
    private PhysicalController balanceController = new BalanceController();
    private AnimationPlayer player;

    @Override
    public RestPose copy(AnimationPlayer player)
    {
        PhysicalBalanceRestPose rp = new PhysicalBalanceRestPose();
        rp.balanceController = balanceController.copy(player.getPHuman());
        rp.setAnimationPlayer(player);
        return rp;
    }

    @Override
    public void setAnimationPlayer(AnimationPlayer player)
    {
        this.player = player;
        balanceController.setPhysicalHumanoid(player.getPHuman());
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {
        if (Sets.intersection(balanceController.getRequiredJointIDs(), physicalJoints).size() == 0)
        {
            player.addController(balanceController);
        }
    }

    @Override
    public TimedAnimationUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedAnimationUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, double duration,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getTransitionToRestDuration(VJoint vCurrent, Set<String> joints)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public AnimationUnit createTransitionToRest(Set<String> joints)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setRestPose()
    {

    }

    @Override
    public void setParameterValue(String name, String value)
    {
        try
        {
            balanceController.setParameterValue(name, value);
        }
        catch (ControllerParameterException e)
        {
            log.warn("ControllerParameterException with parameter "+name+ " = "+value,e);
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

}
