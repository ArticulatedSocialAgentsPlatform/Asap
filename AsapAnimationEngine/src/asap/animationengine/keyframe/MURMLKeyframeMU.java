package asap.animationengine.keyframe;

import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.PegBoard;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.motionunit.keyframe.Interpolator;
import asap.motionunit.keyframe.KeyFrame;
import asap.motionunit.keyframe.KeyFrameMotionUnit;

/**
 * MURML-style keyframe animation
 * @author hvanwelbergen
 *
 */
public class MURMLKeyframeMU extends KeyFrameMotionUnit implements AnimationUnit
{
    private List<String> targets;
    public MURMLKeyframeMU(List<String> targets, Interpolator interp, List<KeyFrame> keyFrames, int nrOfDofs)
    {
        super(interp);
        targets = ImmutableList.copyOf(targets);        
    }

    /**
     * @deprecated no longer relevant in BML 1.0
     */
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
    public void applyKeyFrame(KeyFrame kf)
    {
        // TODO Auto-generated method stub
        
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
