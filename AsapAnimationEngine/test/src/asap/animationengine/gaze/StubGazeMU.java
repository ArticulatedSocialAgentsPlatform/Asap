/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import java.util.Set;

import lombok.Delegate;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;

import com.google.common.collect.ImmutableSet;

/**
 * Testing stub for the GazeMU
 * @author hvanwelbergen
 */
public class StubGazeMU implements GazeMU
{
    @Delegate
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();    
    
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
    public TimedAnimationMotionUnit createTMU(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
    {
        return new GazeTMU(bbm,bmlBlockPeg,bmlId,id,this, pb, null);
    }

    @Override
    public AnimationUnit copy(AnimationPlayer p) throws MUSetupException
    {
        return null;
    }

    @Override
    public void play(double t) throws MUPlayException
    {
                
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {
                
    }

    @Override
    public double getPreferedDuration()
    {
        return 5;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
                
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
                
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        return null;
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        return 0;
    }

    

    @Override
    public double getPreferedStayDuration()
    {
        return 2;
    }

    @Override
    public double getPreferedRelaxDuration()
    {
        return 1;
    }

    @Override
    public double getPreferedReadyDuration()
    {
        return 1;
    }

    @Override
    public void setDurations(double prepDur, double relaxDur)
    {
                
    }

    @Override
    public void setEndRotation(float[] gazeDir)
    {
                
    }

    @Override
    public void setStartPose() throws MUPlayException
    {
                
    }

    @Override
    public void setTarget()
    {
                
    }
    @Override
    public Set<String> getAdditiveJoints()
    {
        return ImmutableSet.of();
    }
}
