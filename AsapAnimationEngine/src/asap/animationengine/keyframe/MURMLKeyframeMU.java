package asap.animationengine.keyframe;

import hmi.animation.VJoint;

import java.util.List;
import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
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
    private VJoint vNext;
    private VJoint vCurr;
    private int nrOfDofs;
    private Interpolator interp;
    private boolean allowDynamicStart;
    private double preferedDuration = 1;
    private TimeManipulator manip;

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
        preferedDuration = unifyKeyFrames(keyFrames);
        interp.setKeyFrames(keyFrames, nrOfDofs);

        KeyPosition start = new KeyPosition("start", 0d, 1d);
        KeyPosition ready = new KeyPosition("ready", 0.1d, 1d);
        KeyPosition strokeStart = new KeyPosition("strokeStart", 0.1d, 1d);
        KeyPosition stroke = new KeyPosition("stroke", 0.5d, 1d);
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
        return preferedDuration;
    }

    @Override
    public void applyKeyFrame(KeyFrame kf)
    {
        int i = 0;
        for (String target : targets)
        {
            vNext.getPartBySid(target).setRotation(kf.getDofs(), i * 4);
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

    public AnimationUnit copy(VJoint vC, VJoint vN)
    {
        MURMLKeyframeMU copy = new MURMLKeyframeMU(targets, interp, manip, keyFrames, nrOfDofs, allowDynamicStart);
        copy.vNext = vN;
        copy.vCurr = vC;
        copy.preferedDuration = preferedDuration;
        for (KeyPosition keypos : getKeyPositions())
        {
            copy.addKeyPosition(keypos.deepCopy());
        }
        return copy;
    }

    @Override
    public AnimationUnit copy(AnimationPlayer p) throws MUSetupException
    {
        return copy(p.getVCurr(), p.getVNext());
    }

    @Override
    public TimedAnimationUnit createTMU(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedAnimationUnit(bbm, bmlBlockPeg, bmlId, id, this, pb);
    }

    @Override
    public KeyFrame getStartKeyFrame()
    {
        float q[] = new float[getKinematicJoints().size() * 4];
        int i = 0;
        for (String target : targets)
        {
            VJoint vj = vCurr.getPartBySid(target);
            vj.getRotation(q, i);
            i += 4;
        }
        return new KeyFrame(0, q);
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {
        super.setupDynamicStart(t, keyFrames);
        interp.setKeyFrames(keyFrames, nrOfDofs);
    }
}
