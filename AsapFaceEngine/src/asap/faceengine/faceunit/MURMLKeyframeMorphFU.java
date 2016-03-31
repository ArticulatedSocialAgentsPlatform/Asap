/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.faceunit;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;

import java.util.List;

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
import com.google.common.collect.Lists;

/**
 * A group of morph targets, controlled by key frames
 * @author hvanwelbergen
 * 
 */
public class MURMLKeyframeMorphFU extends KeyFrameMotionUnit implements FaceUnit
{
    private FaceController faceController;
    private String[] targets;
    private Interpolator interp;
    private boolean allowDynamicStart;
    private int nrOfDofs;
    private List<KeyFrame> keyFrames;
    private KeyFrame kfPrev;
    private double preferedDuration = 1;
    private TimeManipulator manip;
    private volatile boolean interrupted = false;

    public MURMLKeyframeMorphFU(List<String> targets, Interpolator interp, TimeManipulator manip, List<KeyFrame> keyFrames, int nrOfDofs,
            boolean allowDynamicStart)
    {
        super(interp, manip, allowDynamicStart);
        this.manip = manip;
        this.interp = interp;
        this.nrOfDofs = nrOfDofs;
        this.allowDynamicStart = allowDynamicStart;
        this.keyFrames = Lists.newArrayList(keyFrames);
        preferedDuration = unifyKeyFrames(keyFrames);
        float dofs[] = new float[nrOfDofs];
        kfPrev = new KeyFrame(0, dofs);

        interp.setKeyFrames(keyFrames, nrOfDofs);

        this.targets = targets.toArray(new String[targets.size()]);
        KeyPosition attackPeak = new KeyPosition("attackPeak", 0.1d, 1d);
        KeyPosition relax = new KeyPosition("relax", 0.9d, 1d);
        KeyPosition start = new KeyPosition("start", 0d, 1d);
        KeyPosition end = new KeyPosition("end", 1d, 1d);
        addKeyPosition(start);
        addKeyPosition(attackPeak);
        addKeyPosition(relax);
        addKeyPosition(end);
    }

    @Override
    public double getPreferedDuration()
    {
        return preferedDuration;
    }

    @Override
    public boolean hasValidParameters()
    {
        return true;
    }

    public void setFaceController(FaceController fc)
    {
        faceController = fc;
    }

    @Override
    public TimedFaceUnit createTFU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedFaceUnit(bfm, bbPeg, bmlId, id, this, pb);
    }

    @Override
    public FaceUnit copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        MURMLKeyframeMorphFU copy = new MURMLKeyframeMorphFU(ImmutableList.copyOf(targets), interp.copy(), manip, ImmutableList.copyOf(keyFrames),
                nrOfDofs, allowDynamicStart);
        copy.preferedDuration = preferedDuration;
        copy.setFaceController(fc);
        for (KeyPosition keypos : getKeyPositions())
        {
            copy.addKeyPosition(keypos.deepCopy());
        }
        return copy;
    }

    @Override
    public void applyKeyFrame(KeyFrame kf)
    {
        kfPrev = kf;
        faceController.addMorphTargets(targets, kf.getDofs());
    }

    @Override
    public KeyFrame getStartKeyFrame()
    {
        // frames are additive, this just sets a 0 start frame..
        float dofs[] = new float[targets.length];
        return new KeyFrame(0, dofs);
    }

    @Override
    public void play(double t) throws MUPlayException
    {
        if(interrupted)
        {
            KeyFrame kfEnd = keyFrames.get(keyFrames.size()-1);
            KeyFrame kfStart = new KeyFrame(0, kfPrev.getDofs());
            kfPrev.setFrameTime(manip.manip(t));
            keyFrames.clear();
            keyFrames.add(kfStart);
            keyFrames.add(kfPrev);
            keyFrames.add(kfEnd);
            interp.setKeyFrames(keyFrames, nrOfDofs);
            interrupted = false;
        }
        super.play(t);
    }
    
    @Override
    public void startUnit(double t) throws MUPlayException
    {
        super.setupDynamicStart(keyFrames);
        interp.setKeyFrames(keyFrames, nrOfDofs);
    }

    @Override
    public void interruptFromHere()
    {
        interrupted = true;
    }
}
