/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaembodiments;

import hmi.faceanimation.MorphFaceController;
import hmi.faceanimation.MorphTargetHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Face controller that handles face morphing through ipaaca
 * @author hvanwelbergen
 * 
 */
public class IpaacaMorphFaceController implements MorphFaceController
{
    private IpaacaEmbodiment embodiment;
    private final BiMap<String, String> renamingMap;

    public IpaacaMorphFaceController(IpaacaEmbodiment embodiment, BiMap<String, String> renamingMap)
    {
        this.embodiment = embodiment;
        this.renamingMap = renamingMap;
    }

    public IpaacaMorphFaceController(IpaacaEmbodiment embodiment)
    {
        this(embodiment, HashBiMap.<String, String> create());
    }

    private MorphTargetHandler morphTargetHandler = new MorphTargetHandler();

    @Override
    public Collection<String> getPossibleFaceMorphTargetNames()
    {
        List<String> targets = new ArrayList<>();
        for (String str : embodiment.getAvailableMorphs())
        {
            if (renamingMap.get(str) != null)
            {
                targets.add(renamingMap.get(str));
            }
            else
            {
                targets.add(str);
            }
        }
        return ImmutableList.copyOf(targets);
    }

    public ImmutableMap<String, Float> getDesiredMorphTargets()
    {
        Map<String, Float> desired = new HashMap<String, Float>();
        {
            for (Entry<String, Float> entry : morphTargetHandler.getDesiredMorphTargets().entrySet())
            {
                String id = entry.getKey();
                if (renamingMap.inverse().get(id) != null)
                {
                    id = renamingMap.inverse().get(id);
                }
                desired.put(id, entry.getValue());
            }
        }
        return ImmutableMap.copyOf(desired);
    }

    @Override
    public void copy()
    {
        Map<String, Float> desired = new HashMap<String, Float>();
        {
            for (Entry<String, Float> entry : getDesiredMorphTargets().entrySet())
            {
                String id = entry.getKey();
                if (renamingMap.inverse().get(id) != null)
                {
                    id = renamingMap.inverse().get(id);
                }
                desired.put(id, entry.getValue());
            }
        }
        embodiment.setJointData(new ImmutableList.Builder<float[]>().build(), ImmutableMap.copyOf(desired));
    }

    @Override
    public void addMorphTargets(String[] arg0, float[] arg1)
    {
        morphTargetHandler.addMorphTargets(arg0, arg1);

    }

    @Override
    public void removeMorphTargets(String[] arg0, float[] arg1)
    {
        morphTargetHandler.removeMorphTargets(arg0, arg1);

    }

    @Override
    public void setMorphTargets(String[] arg0, float[] arg1)
    {
        morphTargetHandler.setMorphTargets(arg0, arg1);
    }

    public float getCurrentWeight(String morph)
    {
        return morphTargetHandler.getCurrentWeight(morph);
    }
}
