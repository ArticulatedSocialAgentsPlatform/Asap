package asap.animationengine.motionunit;

import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.KeyPositionManagerImpl;

import java.util.List;
import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.motionunit.MUPlayException;

import com.google.common.collect.ImmutableSet;
/**
 * Motion unit stub, typically used to test a TimedMotionUnit implementation.
 * @author welberge
 */
public class StubMotionUnit implements AnimationUnit
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
    public TimedAnimationUnit createTMU(FeedbackManager bfm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedAnimationUnit(bfm,bmlBlockPeg,bmlId,id,this, pb);
    }

    @Override
    public double getPreferedDuration()
    {
        return 0;
    }

    @Override
    public AnimationUnit copy(AnimationPlayer p)
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

    @Override
    public void startUnit(double t) throws MUPlayException
    {
                
    }
}