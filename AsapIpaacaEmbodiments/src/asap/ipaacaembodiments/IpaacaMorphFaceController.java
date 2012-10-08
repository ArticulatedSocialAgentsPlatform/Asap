package asap.ipaacaembodiments;

import hmi.faceanimation.MorphFaceController;
import hmi.faceanimation.MorphTargetHandler;

import java.util.Collection;

import lombok.Delegate;

import com.google.common.collect.ImmutableList;

/**
 * Face controller that handles face morphing through ipaaca
 * @author hvanwelbergen
 *
 */
public class IpaacaMorphFaceController implements MorphFaceController
{
    private IpaacaEmbodiment embodiment;
    
    public IpaacaMorphFaceController(IpaacaEmbodiment embodiment)
    {
        this.embodiment = embodiment;
    }
    
    @Delegate
    private MorphTargetHandler morphTargetHandler = new MorphTargetHandler();
    
    @Override
    public Collection<String> getPossibleFaceMorphTargetNames()
    {
        return embodiment.getAvailableMorphs();
    }

    @Override
    public void copy()
    {
        embodiment.setJointData(new ImmutableList.Builder<float[]>().build(), morphTargetHandler.getDesiredMorphTargets());        
    }

}
