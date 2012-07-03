package asap.animationengine.restpose;

import hmi.animation.VJoint;
import hmi.physics.controller.BalanceController;
import hmi.physics.controller.ControllerParameterException;
import hmi.physics.controller.PhysicalController;
import hmi.util.Resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.controller.CompoundController;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.transitions.SlerpTransitionToPoseMU;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;

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

    @Override
    public RestPose copy(AnimationPlayer player)
    {
        PhysicalBalanceRestPose rp = new PhysicalBalanceRestPose();
        rp.balanceController = balanceController.copy(player.getPHuman());
        rp.setAnimationPlayer(player);
        rp.setResource(resource);
        rp.optionalControllers = new ArrayList<PhysicalController>();
        
        for(PhysicalController cc:optionalControllers)
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
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints)
    {
        if (Sets.intersection(balanceController.getRequiredJointIDs(), kinematicJoints).size() == 0)
        {
            player.addController(balanceController);
        }
        
        for(PhysicalController cc:optionalControllers)
        {
            if(Sets.intersection(cc.getRequiredJointIDs(),kinematicJoints).size()==0)
            {
                player.addController(cc);
            }
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
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("xmlcontrollers"))
        {
            String fileNames[] = value.split(",");
            for(String fileName:fileNames)
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

}
