package asap.ipaacaembodiments;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.NullMPEG4FaceController;
import lombok.Delegate;

/**
 * Implements morph based face animation through ipaaca (MPEG4 animation is ignored)
 * @author hvanwelbergen
 * 
 */
public class IpaacaFaceController implements FaceController
{
    public IpaacaFaceController(IpaacaEmbodiment env)
    {
        mfc = new IpaacaMorphFaceController(env);
    }

    private interface Excludes
    {
        void copy();
    }

    @Delegate(excludes = Excludes.class)
    private IpaacaMorphFaceController mfc;

    @Delegate(excludes = Excludes.class)
    private NullMPEG4FaceController mpegfc = new NullMPEG4FaceController();

    @Override
    public void copy()
    {
        mfc.copy();
    }
}
