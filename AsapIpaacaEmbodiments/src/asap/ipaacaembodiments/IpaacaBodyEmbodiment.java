package asap.ipaacaembodiments;

import hmi.animation.AdditiveRotationBlend;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.animationembodiments.SkeletonEmbodiment;
import hmi.math.Mat4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Sends joint rotations from its animation joint to a renderer through Ipaaca.
 * Assumes that the animation joint is not changed during the copy(). That is: assumes that there is only one thread accessing animationJoint, 
 * and that this same thread calls copy() upon this embodiment. Init constructs the animationjoint and should also be called by the same thread.
 * 
 * Also assumes no other animation (e.g. on the face) is used. Use the IpaacaFaceAndBodyEmbodiment if the face is also to be animated.
 * 
 * XXX alternatively: have getAnimationJoint create a copyJoint and copy the transformations from this copyJoint back to the animationJoint at each copy?
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class IpaacaBodyEmbodiment implements SkeletonEmbodiment
{
    private final String id;
    private VJoint animationJoint;
    private IpaacaEmbodiment ipaacaEmbodiment;
    private List<String> availableJoints;
    private List<String> unusedJoints;
    private List<String> usedJoints;
    
    
    private Set<String> jointFilter;
    private BiMap<String, String> renamingMap;
    
    @GuardedBy("submitJointLock")
    private VJoint submitJoint;
    
    @GuardedBy("submitJointLock")
    private List<VJoint> jointList;// same order as availableJoints
    
    @GuardedBy("submitJointLock")
    private AdditiveRotationBlend blend;
    
    private Object submitJointLock = new Object();

    public IpaacaBodyEmbodiment(String id, IpaacaEmbodiment ipaacaEmbodiment)
    {
        this.id = id;
        this.ipaacaEmbodiment = ipaacaEmbodiment;
    }

    private void updateJointLists()
    {
        ImmutableList<String> ipaacaJoints = ImmutableList.copyOf(ipaacaEmbodiment.getAvailableJoints());
        availableJoints = Lists.transform(ipaacaJoints, new Function<String, String>()
        {
            @Override
            public String apply(@Nullable String str)
            {
                return str.replaceAll(" ", "_");
            }
        });
        jointList = new ArrayList<>();

        unusedJoints = new ArrayList<>();

        int i = 0;
        for (String j : availableJoints)
        {
            VJoint vj = submitJoint.getPart(renamingMap.get(j));
            /*
             * List<String> hanimAll= ImmutableList.of(Hanim.HumanoidRoot, Hanim.r_shoulder,Hanim.l_shoulder, Hanim.r_hip,Hanim.l_hip);
             * if(vj!=null && hanimAll.contains(vj.getSid()))
             */
            if (vj != null && jointFilter.contains(vj.getSid()))
            {
                jointList.add(vj);
            }
            else
            {
                unusedJoints.add(ipaacaJoints.get(i));
                log.warn("Cannot map renderjoint {} to any animation joint.", j);
            }
            i++;
        }
        usedJoints = new ArrayList<>(ipaacaJoints);
        usedJoints.removeAll(unusedJoints);
        ipaacaEmbodiment.setUsedJoints(usedJoints);
    }

    /**
     * @param renamingMap animation joint name -> render joint name map
     */
    public void init(BiMap<String, String> renamingMap, Set<String> jointFilter)
    {
        ipaacaEmbodiment.waitForAvailableJoints();
        this.jointFilter = jointFilter;
        this.renamingMap = renamingMap;
        
        synchronized (submitJointLock)
        {
            submitJoint = ipaacaEmbodiment.getRootJointCopy("copy");

            // apply renaming
            for (VJoint vj : submitJoint.getParts())
            {
                if (renamingMap.get(vj.getSid().replace(" ", "_")) != null)
                {
                    vj.setSid(renamingMap.get(vj.getSid().replace(" ", "_")));
                }
            }

            VJoint hanimJoint = submitJoint.copyTree("hanim");            
            VJointUtils.setHAnimPose(hanimJoint);
            animationJoint = VJointUtils.createNullRotationCopyTree(hanimJoint,"control");            
            blend = new AdditiveRotationBlend(hanimJoint, animationJoint, submitJoint);
            
            updateJointLists();
        }
    }

    protected List<float[]> getJointMatrices()
    {
        List<float[]> jointMatrices = new ArrayList<>();
        synchronized(submitJointLock)
        {
            blend.blend();
            submitJoint.calculateMatrices();
            for(VJoint vj:jointList)
            {
                float m[]=Mat4f.getMat4f();
                Mat4f.set(m,vj.getLocalMatrix());
                jointMatrices.add(m);
            }
        }
        return jointMatrices;
    }
    
    @Override
    public void copy()
    {
        ipaacaEmbodiment.setJointData(getJointMatrices(), new ImmutableMap.Builder<String, Float>().build());
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public VJoint getAnimationVJoint()
    {
        return animationJoint;
    }
}
