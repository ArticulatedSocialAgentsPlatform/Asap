package asap.faceengine.faceunit;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.FaceInterpolator;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import asap.motionunit.MUPlayException;

public class KeyframeFacsFU extends KeyframeFaceUnit 
{
    private FACSConverter facsConverter;
    
    public KeyframeFacsFU(FaceInterpolator mi)
    {
        super(mi);        
    }

    @Override
    public FaceUnit copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        KeyframeFacsFU copy = new KeyframeFacsFU(mi);
        copy.facsConverter = fconv;
        return copy;
    }

    @Override
    public void play(double t) throws MUPlayException
    {
        // TODO Auto-generated method stub
        
    }

}
