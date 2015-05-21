/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.faceunit;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.FaceInterpolator;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import hmi.faceanimation.model.MPEG4Configuration;

import java.util.ArrayList;
import java.util.List;

import asap.motionunit.MUPlayException;

import com.google.common.collect.ImmutableList;

/**
 * MotionUnit for MPEG-4 keyframe animation
 * @author herwinvw
 *
 */
public class KeyframeFapsMU extends KeyframeFaceUnit
{
    private final List<Integer> parts;

    public KeyframeFapsMU(FaceInterpolator mi)
    {
        super(mi);
        List<Integer> ps = new ArrayList<Integer>();
        for (String part : mi.getParts())
        {
            ps.add(Integer.parseInt(part));
        }
        parts = ImmutableList.copyOf(ps);
    }

    @Override
    public KeyframeFapsMU copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        KeyframeFapsMU copy = new KeyframeFapsMU(mi);
        setupCopy(copy, fc);
        return copy;
    }

    @Override
    public void play(double t) throws MUPlayException
    {
        float current[] = getInterpolatedValue(t);
        MPEG4Configuration config = new MPEG4Configuration();
        int i = 0;
        for (Integer part : parts)
        {
            config.setValue(part-1, Math.round(current[i]));
            i++;
        }
        faceController.addMPEG4Configuration(config);
    }

}
