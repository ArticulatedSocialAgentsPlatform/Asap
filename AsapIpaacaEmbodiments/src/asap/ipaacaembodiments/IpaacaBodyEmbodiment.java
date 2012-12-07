package asap.ipaacaembodiments;

import hmi.animation.Hanim;
import hmi.animation.Skeleton;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.animationembodiments.SkeletonEmbodiment;
import hmi.math.Mat4f;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

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
 * XXX alternatively: have getAnimationJoint create a
 * copyJoint and copy the transformations from this copyJoint back to the animationJoint at each copy?
 * @author hvanwelbergen
 * 
 */
public class IpaacaBodyEmbodiment implements SkeletonEmbodiment
{
    private final String id;
    private IpaacaEmbodiment ipaacaEmbodiment;
    private List<String> availableJoints;
    private List<String> unusedJoints;
    private List<String> usedJoints;
    private float[][] transformMatrices;

    private BiMap<String, String> renamingMap;

    @GuardedBy("submitJointLock")
    private VJoint submitJoint;

    private List<String> jointList = new ArrayList<String>();// same order as availableJoints

    @GuardedBy("submitJointLock")
    // private AdditiveT1RBlend blend;
    private Skeleton skel;

    private Object submitJointLock = new Object();

    public IpaacaBodyEmbodiment(String id, IpaacaEmbodiment ipaacaEmbodiment)
    {
        this.id = id;
        this.ipaacaEmbodiment = ipaacaEmbodiment;
    }

    private void updateJointLists(List<String> jointFilter)
    {
        ImmutableList<String> ipaacaJoints = ImmutableList.copyOf(ipaacaEmbodiment.getAvailableJoints());
        availableJoints = new ArrayList<>(ipaacaJoints);
        
        unusedJoints = new ArrayList<>();

        int i = 0;
        for (String j : availableJoints)
        {
            VJoint vj = submitJoint.getPart(j);
            if(vj==null)
            {
                vj = submitJoint.getPart(renamingMap.get(j));
            }
            
            if (vj == null || !jointFilter.contains(vj.getSid()))
            {
                unusedJoints.add(ipaacaJoints.get(i));
            }
            else
            {
                jointList.add(vj.getSid());
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
    public void init(BiMap<String, String> renamingMap, List<String> jointFilter)
    {
        ipaacaEmbodiment.waitForAvailableJoints();
        this.renamingMap = renamingMap;

        synchronized (submitJointLock)
        {
            submitJoint = ipaacaEmbodiment.getRootJointCopy("copy");

            // apply renaming
            for (VJoint vj : submitJoint.getParts())
            {
                if (renamingMap.get(vj.getSid()) != null)
                {
                    vj.setSid(renamingMap.get(vj.getSid()));
                }
            }
            submitJoint = submitJoint.getPart(Hanim.HumanoidRoot);

            VJoint vjDummy = new VJoint();
            vjDummy.addChild(submitJoint);
            // VJointUtils.setHAnimPose(submitJoint);
            VJointUtils.setHAnimPose(vjDummy);

            skel = new Skeleton(submitJoint.getId() + "skel", submitJoint);
            updateJointLists(jointFilter);
            skel.setJointSids(jointList);

            skel.setNeutralPose();
            transformMatrices = skel.getTransformMatricesRef();
            skel.setUpdateOnWrite(true);
        }
    }

    protected List<float[]> getJointMatrices()
    {
        List<float[]> jointMatrices = new ArrayList<>();
        synchronized (submitJointLock)
        {
            skel.putData();
            skel.getData();

            for (int i = 0; i < jointList.size(); i++)
            {
                float m[] = Mat4f.getMat4f();

                VJoint vj = submitJoint.getPartBySid(jointList.get(i));
                VJoint vjParent = vj.getParent();

                if (vjParent == null || vj.getSid().equals(Hanim.HumanoidRoot))
                {
                    Mat4f.set(m, transformMatrices[i]);
                }
                else
                {
                    float[] pInverse = Mat4f.getMat4f();
                    if (jointList.contains(vjParent.getSid()))
                    {
                        Mat4f.invertRigid(pInverse, transformMatrices[jointList.indexOf(vjParent.getSid())]);
                    }
                    else
                    {
                        // FIXME: does not take into account inverse binds between parent and root
                        Mat4f.invertRigid(pInverse, vjParent.getGlobalMatrix());
                    }
                    Mat4f.mul(m, pInverse, transformMatrices[i]);
                }
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
        return submitJoint;
    }
}
