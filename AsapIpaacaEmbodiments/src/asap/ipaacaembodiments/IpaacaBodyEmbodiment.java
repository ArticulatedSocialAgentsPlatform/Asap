package asap.ipaacaembodiments;

import hmi.animation.VJoint;
import hmi.environmentbase.CopyEmbodiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Sends joint rotations from its animation joint to a renderer through Ipaaca.
 * Assumes that the animation joint is not changed during the copy(). That is: assumes that there is only one thread accessing animationJoint.
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class IpaacaBodyEmbodiment implements CopyEmbodiment
{
    private final String id;
    private final VJoint animationJoint;
    private IpaacaEmbodiment ipaacaEmbodiment;
    private List<String> availableJoints;
    private List<String> unusedJoints;
    private List<String> usedJoints;
    private List<VJoint> jointList;// same order as availableJoints
    private Set<String> jointFilter;
    private BiMap<String, String> renamingMap;

    public IpaacaBodyEmbodiment(String id, VJoint animationJoint, IpaacaEmbodiment ipaacaEmbodiment)
    {
        this.id = id;
        this.animationJoint = animationJoint;
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
            VJoint vj = animationJoint.getPart(renamingMap.get(j));
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
        this.jointFilter = jointFilter;
        this.renamingMap = renamingMap;
        updateJointLists();
        ipaacaEmbodiment.addTargetUpdateListeners(new AvailableTargetsUpdateListener()
        {

            @Override
            public void update()
            {
                updateJointLists();
            }
        });
    }

    @Override
    public void copy()
    {
        animationJoint.calculateMatrices();
        ipaacaEmbodiment.setJointData(jointList, new ImmutableMap.Builder<String, Float>().build());
    }

    @Override
    public String getId()
    {
        return id;
    }
}
