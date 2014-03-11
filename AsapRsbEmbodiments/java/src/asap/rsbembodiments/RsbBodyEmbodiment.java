package asap.rsbembodiments;

import hmi.animation.Hanim;
import hmi.animation.Skeleton;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.animationembodiments.SkeletonEmbodiment;
import hmi.math.Mat4f;
import hmi.math.Quat4f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import rsb.Factory;
import rsb.Informer;
import rsb.InitializeException;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.RemoteServer;
import asap.rsbembodiments.Rsbembodiments.AnimationData;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigRequest;
import asap.rsbembodiments.Rsbembodiments.AnimationSelection;
import asap.rsbembodiments.util.VJointRsbUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.primitives.Floats;

/**
 * Interfaces with an rsb graphical environment.
 * Currently rsb graphical environments are assumed to contain only one character.
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class RsbBodyEmbodiment implements SkeletonEmbodiment
{
    @Getter
    private String id;

    private final String characterId;
    private Informer<AnimationData> jointDataInformer;
    private Informer<AnimationSelection> animationSelectionInformer;
    private Object submitJointLock = new Object();
    private BiMap<String, String> renamingMap;

    @GuardedBy("submitJointLock")
    private VJoint submitJoint;

    private List<String> jointList = new ArrayList<String>();// same order as availableJoints
    @GuardedBy("submitJointLock")
    private Skeleton skel;
    private float[][] transformMatrices;

    public RsbBodyEmbodiment(String id, String characterId)
    {
        this.id = id;
        this.characterId = characterId;
    }

    private void initRsbConverters()
    {
        final ProtocolBufferConverter<AnimationData> jointDataConverter = new ProtocolBufferConverter<AnimationData>(
                AnimationData.getDefaultInstance());
        final ProtocolBufferConverter<AnimationDataConfigRequest> jointDataReqConverter = new ProtocolBufferConverter<AnimationDataConfigRequest>(
                AnimationDataConfigRequest.getDefaultInstance());
        final ProtocolBufferConverter<AnimationDataConfigReply> jointDataConfigReplyConverter = new ProtocolBufferConverter<AnimationDataConfigReply>(
                AnimationDataConfigReply.getDefaultInstance());
        final ProtocolBufferConverter<AnimationSelection> animationSelection = new ProtocolBufferConverter<AnimationSelection>(
                AnimationSelection.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataReqConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConfigReplyConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(animationSelection);
    }

    private void initInformers()
    {
        try
        {
            jointDataInformer = Factory.getInstance().createInformer(RSBEmbodimentConstants.ANIMATIONDATA_CATEGORY);
            animationSelectionInformer = Factory.getInstance().createInformer(RSBEmbodimentConstants.ANIMATIONSELECTION_CATEGORY);
            jointDataInformer.activate();
            animationSelectionInformer.activate();
        }
        catch (InitializeException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void selectJoints()
    {
        try
        {
            animationSelectionInformer.send(AnimationSelection.newBuilder().addAllSelectedJoints(jointList).build());
        }
        catch (RSBException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void updateJointLists(List<String> jointFilter)
    {
        for (String j : VJointUtils.transformToSidList(submitJoint.getParts()))
        {
            VJoint vj = submitJoint.getPart(j);
            if (vj == null)
            {
                vj = submitJoint.getPart(renamingMap.get(j));
            }

            if (vj != null && jointFilter.contains(vj.getSid()))
            {
                jointList.add(vj.getSid());
            }            
        }
        selectJoints();
    }

    private void initJoints(BiMap<String, String> renamingMap, List<String> jointFilter)
    {
        this.renamingMap = renamingMap;
        final RemoteServer server = Factory.getInstance().createRemoteServer(RSBEmbodimentConstants.ANIMATIONDATACONFIG_CATEGORY);
        try
        {
            server.activate();
            AnimationDataConfigReply reply = server.call(RSBEmbodimentConstants.ANIMATIONDATACONFIG_REQUEST_FUNCTION,
                    AnimationDataConfigRequest.newBuilder().setCharacterId(characterId).build());
            synchronized (submitJointLock)
            {
                submitJoint = VJointRsbUtils.toVJoint(reply.getSkeleton());
                // apply renaming
                for (VJoint vj : submitJoint.getParts())
                {
                    if (renamingMap.get(vj.getSid()) != null)
                    {
                        vj.setSid(renamingMap.get(vj.getSid()));
                    }
                }

                submitJoint = submitJoint.getPart(Hanim.HumanoidRoot);

                VJoint vjDummy = new VJoint("dummy");
                vjDummy.addChild(submitJoint);
                VJointUtils.setHAnimPose(vjDummy);

                skel = new Skeleton(submitJoint.getId() + "skel", submitJoint);
                updateJointLists(jointFilter);
                skel.setJointSids(jointList);

                skel.setNeutralPose();
                transformMatrices = skel.getTransformMatricesRef();
                skel.setUpdateOnWrite(true);
            }
        }
        catch (RSBException e)
        {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
        catch (TimeoutException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                server.deactivate();
            }
            catch (RSBException e)
            {
                throw new RuntimeException(e);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }

    }

    public void initialize(List<String> jointFilter)
    {
        initialize(HashBiMap.<String, String> create(), jointFilter);
    }

    public void initialize(BiMap<String, String> renamingMap, List<String> jointFilter)
    {
        initRsbConverters();
        initInformers();
        initJoints(renamingMap, jointFilter);        
    }

    private List<Float> getJointQuats()
    {
        List<Float> jointData = new ArrayList<>();
        synchronized (submitJointLock)
        {
            skel.putData();
            skel.getData();
            float q[] = Quat4f.getQuat4f();
            float m[] = Mat4f.getMat4f();

            for (int i = 0; i < jointList.size(); i++)
            {
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
                Quat4f.setFromMat4f(q, m);
                jointData.addAll(Floats.asList(q));
            }
        }
        return jointData;
    }

    @Override
    public void copy()
    {
        // construct float list for rotations, send with informer
        AnimationData jd = AnimationData.newBuilder().addAllJointQuats(getJointQuats()).build();
        try
        {
            jointDataInformer.send(jd);
        }
        catch (RSBException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VJoint getAnimationVJoint()
    {
        return submitJoint;
    }

    public void shutdown()
    {
        try
        {
            jointDataInformer.deactivate();
        }
        catch (RSBException e)
        {
            log.warn("RSB Exception", e);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
            log.warn("InterruptedException", e);
        }
        try
        {
            animationSelectionInformer.deactivate();
        }
        catch (RSBException e)
        {
            log.warn("RSB Exception", e);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
            log.warn("InterruptedException", e);
        }
    }
}
