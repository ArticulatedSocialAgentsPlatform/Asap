package asap.animationengine.ace.lmp;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.GuidingSequence;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterNotFoundException;

public class LMPWristPos extends LMPPos
{
    

    public LMPWristPos(GuidingSequence seq)
    {
        
    }
    
    @Override
    public void play(double t) throws MUPlayException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {
        // TODO Auto-generated method stub

    }

    @Override
    @Deprecated
    public String getReplacementGroup()
    {
        return null;
    }

    @Override
    public double getPreferedDuration()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterNotFoundException
    {
        throw new ParameterNotFoundException(name);
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterNotFoundException
    {
        throw new ParameterNotFoundException(name);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterNotFoundException
    {
        throw new ParameterNotFoundException(name);
    }

    @Override
    public String getParameterValue(String name) throws ParameterNotFoundException
    {
        throw new ParameterNotFoundException(name);
    }

    

    @Override
    public Set<String> getPhysicalJoints()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedAnimationUnit createTMU(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AnimationUnit copy(AnimationPlayer p) throws MUSetupException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
