/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaembodiments;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.NullMPEG4FaceController;

import java.util.Collection;

import lombok.Delegate;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;

/**
 * Implements morph based face animation through ipaaca (MPEG4 animation is ignored)
 * @author hvanwelbergen
 * 
 */
public class IpaacaFaceController implements FaceController
{
    public IpaacaFaceController(IpaacaEmbodiment env)
    {
        this(env, HashBiMap.<String,String>create());
    }
    
    public IpaacaFaceController(IpaacaEmbodiment env, BiMap<String, String> renamingMap)
    {
        mfc = new IpaacaMorphFaceController(env, renamingMap);
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

    @Override
    public float getCurrentWeight(String targetName)
    {
        return mfc.getCurrentWeight(targetName);
    }
}
