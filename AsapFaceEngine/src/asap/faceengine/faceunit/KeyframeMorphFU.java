package asap.faceengine.faceunit;

import java.util.List;

import com.google.common.collect.ImmutableList;

import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import asap.motionunit.keyframe.Interpolator;
import asap.motionunit.keyframe.KeyFrame;
import asap.motionunit.keyframe.KeyFrameMotionUnit;
import asap.utils.AnimationSync;

/**
 * A group of morph targets, controlled by key frames
 * @author hvanwelbergen
 *
 */
public class KeyframeMorphFU extends KeyFrameMotionUnit implements FaceUnit
{
    private FaceController faceController;
    private String[] targets;
    private float[] prevWeights;
    private Interpolator interp;

    public KeyframeMorphFU(List<String> targets, Interpolator interp)
    {
        super(interp);
        this.interp = interp;
        prevWeights = new float[targets.size()];
        this.targets = targets.toArray(new String[targets.size()]);
        KeyPosition ready = new KeyPosition("ready", 0.1d, 1d);
        KeyPosition relax = new KeyPosition("relax", 0.9d, 1d);
        KeyPosition start = new KeyPosition("start", 0d, 1d);
        KeyPosition end = new KeyPosition("end", 1d, 1d);
        addKeyPosition(start);
        addKeyPosition(ready);
        addKeyPosition(relax);
        addKeyPosition(end);
    }

    @Override
    public String getReplacementGroup()
    {
        return null;
    }

    @Override
    public double getPreferedDuration()
    {
        return 0;
    }

    @Override
    public boolean hasValidParameters()
    {
        return true;
    }

    @Override
    public void cleanup()
    {
        faceController.removeMorphTargets(targets, prevWeights);
        for (int i = 0; i < prevWeights.length; i++)
        {
            prevWeights[i] = 0;
        }
    }

    public void setFaceController(FaceController fc)
    {
        faceController = fc;
    }

    @Override
    public TimedFaceUnit createTFU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        return new TimedFaceUnit(bfm, bbPeg, bmlId, id, this);
    }

    @Override
    public FaceUnit copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        KeyframeMorphFU copy = new KeyframeMorphFU(ImmutableList.copyOf(targets), interp.copy());
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
        synchronized (AnimationSync.getSync())
        {
            faceController.removeMorphTargets(targets, prevWeights);
            faceController.addMorphTargets(targets, kf.getDofs());
            System.arraycopy(kf.getDofs(), 0, prevWeights, 0, prevWeights.length);
        }
    }
}
