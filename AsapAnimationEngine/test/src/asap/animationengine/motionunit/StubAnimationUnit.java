/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.motionunit;

import java.util.Set;

import lombok.Delegate;
import asap.animationengine.AnimationPlayer;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;

import com.google.common.collect.ImmutableSet;

/**
 * Motion unit stub, typically used to test a TimedMotionUnit implementation.
 * @author welberge
 */
public class StubAnimationUnit implements AnimationUnit
{
    @Delegate
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private AnimationPlayer aniPlayer;

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
    public TimedAnimationMotionUnit createTMU(FeedbackManager bfm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedAnimationMotionUnit(bfm, bmlBlockPeg, bmlId, id, this, pb, aniPlayer);
    }

    @Override
    public double getPreferedDuration()
    {
        return 0;
    }

    @Override
    public AnimationUnit copy(AnimationPlayer p)
    {
        this.aniPlayer = p;
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
    
    @Override
    public Set<String> getAdditiveJoints()
    {
        return ImmutableSet.of();
    }
}
