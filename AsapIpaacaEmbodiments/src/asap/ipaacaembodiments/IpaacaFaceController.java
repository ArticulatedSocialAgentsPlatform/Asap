package asap.ipaacaembodiments;

import java.util.Collection;

import com.google.common.collect.ImmutableMap;

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
    private NullMPEG4FaceController mpegfc = new NullMPEG4FaceController();

    // recursive @Delegate broken:
    // see http://code.google.com/p/projectlombok/issues/detail?id=305
    // manual delegation below
    private IpaacaMorphFaceController mfc;

    public void setMorphTargets(String[] targetNames, float[] weights)
    {
        mfc.setMorphTargets(targetNames, weights);
    }

    public void addMorphTargets(String[] targetNames, float[] weights)
    {
        mfc.addMorphTargets(targetNames, weights);
    }

    public void removeMorphTargets(String[] targetNames, float[] weights)
    {
        mfc.removeMorphTargets(targetNames, weights);
    }

    public Collection<String> getPossibleFaceMorphTargetNames()
    {
        return mfc.getPossibleFaceMorphTargetNames();
    }

    public ImmutableMap<String, Float> getDesiredMorphTargets()
    {
        return mfc.getDesiredMorphTargets();
    }
    
    public void copy()
    {
        mfc.copy();
    }
}
