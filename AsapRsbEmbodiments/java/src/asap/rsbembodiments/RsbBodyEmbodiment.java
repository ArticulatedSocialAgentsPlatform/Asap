package asap.rsbembodiments;

import hmi.animation.Hanim;
import hmi.animation.Skeleton;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.animationembodiments.SkeletonEmbodiment;
import hmi.math.Mat4f;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.concurrent.GuardedBy;

import lombok.Getter;
import rsb.Factory;
import rsb.Informer;
import rsb.InitializeException;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.RemoteServer;
import asap.rsbembodiments.Rsbembodiments.AnimationData;
import asap.rsbembodiments.Rsbembodiments.JointDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.JointDataConfigRequest;
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
public class RsbBodyEmbodiment implements SkeletonEmbodiment
{
    @Getter
    private String id;

    private final String characterId;
    private Informer<AnimationData> jointDataInformer;
    private Object submitJointLock = new Object();
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
        final ProtocolBufferConverter<AnimationData> jointDataConverter = new ProtocolBufferConverter<AnimationData>(AnimationData.getDefaultInstance());
        final ProtocolBufferConverter<JointDataConfigRequest> jointDataReqConverter = new ProtocolBufferConverter<JointDataConfigRequest>(
                JointDataConfigRequest.getDefaultInstance());
        final ProtocolBufferConverter<JointDataConfigReply> jointDataConfigReplyConverter = new ProtocolBufferConverter<JointDataConfigReply>(
                JointDataConfigReply.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataReqConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConfigReplyConverter);

    }

    private void initInformer()
    {
        try
        {
            jointDataInformer = Factory.getInstance().createInformer(RSBEmbodimentConstants.JOINTDATA_CATEGORY);
            jointDataInformer.activate();
        }
        catch (InitializeException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void initJoints(BiMap<String, String> renamingMap)
    {
        final RemoteServer server = Factory.getInstance().createRemoteServer(RSBEmbodimentConstants.JOINTDATACONFIG_CATEGORY);
        try
        {
            server.activate();
            Rsbembodiments.JointDataConfigReply reply = server.call(RSBEmbodimentConstants.JOINTDATACONFIG_REQUEST_FUNCTION,
                    Rsbembodiments.JointDataConfigRequest.newBuilder().setId(characterId).build());
            synchronized (submitJointLock)
            {
                submitJoint = VJointRsbUtils.toVJoint(reply.getSkeletonList());
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
                VJointUtils.setHAnimPose(vjDummy);

                skel = new Skeleton(submitJoint.getId() + "skel", submitJoint);
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

    public void initialize()
    {
        initialize(HashBiMap.<String, String> create());
    }

    public void initialize(BiMap<String, String> renamingMap)
    {
        initRsbConverters();
        initJoints(renamingMap);
        initInformer();
    }

    private List<Float> getJointData()
    {
        List<Float> jointData = new ArrayList<>();
        synchronized (submitJointLock)
        {
            skel.putData();
            skel.getData();
            float t[] = Vec3f.getVec3f();
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
                if (i == 0)
                {
                    Mat4f.getTranslation(t, m);
                    jointData.addAll(Floats.asList(t));
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
        AnimationData jd = AnimationData.newBuilder().addAllJointData(getJointData()).build();
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
}
