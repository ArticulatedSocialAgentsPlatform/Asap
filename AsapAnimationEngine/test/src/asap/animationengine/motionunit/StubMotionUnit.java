package asap.animationengine.motionunit;

import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.KeyPositionManagerImpl;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.feedback.FeedbackManager;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUPlayException;
import asap.animationengine.motionunit.MotionUnit;
import asap.animationengine.motionunit.TimedMotionUnit;
/**
 * Motion unit stub, typically used to test a TimedMotionUnit implementation.
 * @author welberge
 */
public class StubMotionUnit implements MotionUnit
{
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();    
    
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    public List<KeyPosition> getKeyPositions()
    {
        return keyPositionManager.getKeyPositions();
    }

    public void setKeyPositions(List<KeyPosition> p)
    {
        keyPositionManager.setKeyPositions(p);
    }

    public KeyPosition getKeyPosition(String id)
    {
        return keyPositionManager.getKeyPosition(id);
    }

    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

    @Override
    public void setFloatParameterValue(String name, float value)
    {
    }

    @Override
    public void setParameterValue(String name, String value)
    {
    }

    @Override
    public String getParameterValue(String name)
    {
        return null;
    }

    @Override
    public void play(double t) throws MUPlayException
    {

    }

    @Override
    public TimedMotionUnit createTMU(FeedbackManager bfm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedMotionUnit(bfm,bmlBlockPeg,bmlId,id,this, pb);
    }

    @Override
    public double getPreferedDuration()
    {
        return 0;
    }

    @Override
    public MotionUnit copy(AnimationPlayer p)
    {
        return null;
    }

    @Override
    public String getReplacementGroup()
    {
        return null;
    }

    @Override
    public float getFloatParameterValue(String name)
    {
        return 0;
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        return ImmutableSet.of();
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return ImmutableSet.of();
    }
}