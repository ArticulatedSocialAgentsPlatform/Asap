/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.faceunit;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.FaceInterpolator;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;

/**
 * Implements a keyframe animation on morphunits
 * e.g. for corpus reproduction.
 * @author herwinvw
 *
 */
public class KeyframeMorphFU extends KeyframeFaceUnit 
{
    public KeyframeMorphFU(FaceInterpolator mi)
    {
        super(mi);
    }
    
    @Override
    public void play(double t)
    {
        faceController.addMorphTargets(mi.getParts().toArray(new String[0]),getInterpolatedValue(t));
    }

    @Override
    public KeyframeMorphFU copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        KeyframeMorphFU copy = new KeyframeMorphFU(mi);
        setupCopy(copy,fc);       
        return copy;
    }
}
