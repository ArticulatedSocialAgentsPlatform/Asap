/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.keyframe;

import hmi.animation.VJoint;
import hmi.animation.VJointUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.MUPlayException;
import asap.motionunit.keyframe.Interpolator;
import asap.motionunit.keyframe.KeyFrame;
import asap.motionunit.keyframe.KeyFrameMotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPosition;
import asap.timemanipulator.TimeManipulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * MURML-style keyframe animation
 * @author hvanwelbergen
 * 
 */
public class MURMLKeyframeMU extends KeyFrameMotionUnit implements AnimationUnit
{
    private ImmutableList<String> targets;
    private List<KeyFrame> keyFrames;
    private int nrOfDofs;
    private Interpolator interp;
    private boolean allowDynamicStart;
    private double preferedDuration = 1;
    private TimeManipulator manip;
    private AnimationPlayer aniPlayer;
    private AnimationUnit relaxUnit;
    
    @Getter
    private double preparationDuration;
    
    public MURMLKeyframeMU(List<String> targets, Interpolator interp, TimeManipulator manip, List<KeyFrame> keyFrames, int nrOfDofs,
            boolean allowDynamicStart)
    {
        super(interp, manip, allowDynamicStart);
        this.allowDynamicStart = allowDynamicStart;
        this.manip = manip;
        this.targets = ImmutableList.copyOf(targets);
        this.nrOfDofs = nrOfDofs;
        this.keyFrames = Lists.newArrayList(keyFrames);
        this.interp = interp;
        if(allowDynamicStart)
        {
            preparationDuration = keyFrames.get(0).getFrameTime();
        }
        else if (keyFrames.size()>1)
        {
            preparationDuration = keyFrames.get(1).getFrameTime();
        }
        
        preferedDuration = unifyKeyFrames(keyFrames);
        interp.setKeyFrames(keyFrames, nrOfDofs);

        KeyPosition start = new KeyPosition("start", 0d, 1d);
        double relativeReady = preparationDuration/preferedDuration;
        KeyPosition ready = new KeyPosition("ready", relativeReady, 1d);        
        KeyPosition strokeStart = new KeyPosition("strokeStart", relativeReady, 1d);
        KeyPosition stroke = new KeyPosition("stroke", relativeReady, 1d);
        KeyPosition strokeEnd = new KeyPosition("strokeEnd", 0.9d, 1d);
        KeyPosition relax = new KeyPosition("relax", 0.9d, 1d);
        KeyPosition end = new KeyPosition("end", 1d, 1d);
        addKeyPosition(start);
        addKeyPosition(ready);
        addKeyPosition(strokeStart);
        addKeyPosition(stroke);
        addKeyPosition(strokeEnd);
        addKeyPosition(relax);
        addKeyPosition(end);
    }

    
    @Override
    public void play(double t) throws MUPlayException
    {
        double relaxTime = getKeyPosition("relax").time;
        if(t<=relaxTime)
        {
            super.play(t/relaxTime);
        }
        else
        {
            relaxUnit.play( (t-relaxTime)/(1-relaxTime));
        }
    }
    
    /**
     * Prefered duration of retraction + stroke
     */
    @Override
    public double getPreferedDuration()
    {
        return preferedDuration;
    }

    @Override
    public void applyKeyFrame(KeyFrame kf)
    {
        int i = 0;
        for (String target : targets)
        {
            aniPlayer.getVNextPartBySid(target).setRotation(kf.getDofs(), i * 4);
            i++;
        }
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return ImmutableSet.copyOf(targets);
    }

    private static final Set<String> PHJOINTS = ImmutableSet.of();

    @Override
    public Set<String> getPhysicalJoints()
    {
        return PHJOINTS;
    }

    public MURMLKeyframeMU copy(AnimationPlayer p) throws MUSetupException
    {
        MURMLKeyframeMU copy = new MURMLKeyframeMU(targets, interp, manip, keyFrames, nrOfDofs, allowDynamicStart);
        copy.aniPlayer = p;
        copy.preferedDuration = preferedDuration;
        copy.preparationDuration = preparationDuration;
        
        Set<String> targetCopy = new HashSet<String>(targets);
        targetCopy.removeAll(VJointUtils.transformToSidSet(p.getVCurr().getParts()));
        if(!targetCopy.isEmpty())
        {
            throw new MUSetupException("target joints "+targetCopy+ " not in animated skeleton ", this);
        }
        for (KeyPosition keypos : getKeyPositions())
        {
            copy.addKeyPosition(keypos.deepCopy());
        }
        return copy;
    }

    

    @Override
    public TimedAnimationMotionUnit createTMU(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
    {
        return new MURMLKeyframeTMU(bbm,bmlBlockPeg, bmlId, id, this, pb, aniPlayer);
    }

    @Override
    public KeyFrame getStartKeyFrame()
    {
        float q[] = new float[getKinematicJoints().size() * 4];
        int i = 0;
        for (String target : targets)
        {
            VJoint vj = aniPlayer.getVCurrPartBySid(target);
            vj.getRotation(q, i);
            i += 4;
        }
        return new KeyFrame(0, q);
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {
        super.setupDynamicStart(keyFrames);
        interp.setKeyFrames(keyFrames, nrOfDofs);
    }
    
    public void setupRelaxUnit()
    {
        relaxUnit = aniPlayer.createTransitionToRest(getKinematicJoints());
    }
    
    public double getRetractionDuration()
    {
        //XXX: would be more accurate to do this from relax pos..
        return aniPlayer.getTransitionToRestDuration(getKinematicJoints());
    }

    
    public double getRetractionDurationFromCurrent()
    {
        return aniPlayer.getTransitionToRestDuration(getKinematicJoints());
    }
    
    @Override
    public Set<String> getAdditiveJoints()
    {
        return ImmutableSet.of();
    }
}
