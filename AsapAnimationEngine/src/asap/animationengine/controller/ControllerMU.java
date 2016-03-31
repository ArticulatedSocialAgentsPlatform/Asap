/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.controller;

import hmi.physics.controller.ControllerParameterException;
import hmi.physics.controller.ControllerParameterNotFoundException;
import hmi.physics.controller.PhysicalController;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.PhysicalTMU;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;

import com.google.common.collect.ImmutableSet;

/**
 * MotionUnit for a physical controller
 * @author Herwin van Welbergen
 * 
 */
public class ControllerMU implements AnimationUnit
{
    private PhysicalController controller;
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private AnimationPlayer aPlayer;
    @Getter private int priority = 0;
    
    /**
     * Constructor
     * @param pc physical controller linked to the motion unit
     */
    public ControllerMU(PhysicalController pc, AnimationPlayer player)
    {
        controller = pc;
        aPlayer = player;
        addKeyPosition(new KeyPosition("start", 0, 1));
        addKeyPosition(new KeyPosition("end", 1, 1));
    }

    @Override
    public double getPreferedDuration()
    {
        return 0;
    }

    @Override
    public void play(double t)
    {
        aPlayer.addController(controller);

    }

    /**
     * Create a copy for use in animation player p (that is, link up to its
     * VNext, PhysicalHumanoid, physically steered joint list
     * 
     * @param p
     *            animation player to use the MotionUnit in
     */
    @Override
    public AnimationUnit copy(AnimationPlayer p)
    {
        PhysicalController pc = controller.copy(p.getPHuman());
        ControllerMU c = new ControllerMU(pc, p);
        c.priority = priority;
        return c;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        if(name.equals("priority"))
        {
            priority = (int)value;
        }
        else
        {
            try
            {
                controller.setParameterValue(name, value);
            }
            catch (ControllerParameterNotFoundException e)
            {
                throw new ParameterNotFoundException(e.getParamId(), e);
            }
            catch (ControllerParameterException e)
            {
                throw new InvalidParameterException(name, "" + value, e);
            }
        }
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if(name.equals("priority"))
        {
            priority = Integer.parseInt(value);
        }
        else
        {
            try
            {
                controller.setParameterValue(name, value);
            }
            catch (ControllerParameterNotFoundException e)
            {
                throw new ParameterNotFoundException(e.getParamId(), e);
            }
            catch (ControllerParameterException e)
            {
                throw new InvalidParameterException(name, value, e);
            }
        }
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        if(name.equals("priority"))
        {
            return ""+priority;
        }        
        try
        {
            return controller.getParameterValue(name);
        }
        catch (ControllerParameterNotFoundException e)
        {
            throw new ParameterNotFoundException(e.getParamId(), e);
        }
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterNotFoundException
    {
        if(name.equals("priority"))
        {
            return priority;
        }
        try
        {
            return controller.getFloatParameterValue(name);
        }
        catch (ControllerParameterNotFoundException e)
        {
            throw new ParameterNotFoundException(e.getParamId(), e);
        }
    }

    @Override
    public TimedAnimationMotionUnit createTMU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new PhysicalTMU(bfm, bbPeg, bmlId, id, this, pb, aPlayer);
    }

    public void reset()
    {
        controller.reset();
    }

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    @Override
    public List<KeyPosition> getKeyPositions()
    {
        return keyPositionManager.getKeyPositions();
    }

    @Override
    public void setKeyPositions(List<KeyPosition> p)
    {
        keyPositionManager.setKeyPositions(p);
    }

    @Override
    public KeyPosition getKeyPosition(String name)
    {
        return keyPositionManager.getKeyPosition(name);
    }

    @Override
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

    private static final Set<String> KINJOINTS = ImmutableSet.of();

    @Override
    public Set<String> getKinematicJoints()
    {
        return KINJOINTS;
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        return controller.getJoints();
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {

    }

    @Override
    public Set<String> getAdditiveJoints()
    {
        return ImmutableSet.of();
    }
}
