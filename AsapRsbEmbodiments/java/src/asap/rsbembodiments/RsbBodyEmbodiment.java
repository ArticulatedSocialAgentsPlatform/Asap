package asap.rsbembodiments;

import hmi.animation.VJoint;
import hmi.animationembodiments.SkeletonEmbodiment;

import java.util.Arrays;
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
import asap.rsbembodiments.Rsbembodiments.JointData;
import asap.rsbembodiments.Rsbembodiments.JointDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.JointDataConfigRequest;
import asap.rsbembodiments.util.VJointRsbUtils;

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
    private Informer<JointData> jointDataInformer;
    private Object submitJointLock = new Object();
    @GuardedBy("submitJointLock")
    private VJoint submitJoint;

    public RsbBodyEmbodiment(String id, String characterId)
    {
        this.id = id;
        this.characterId = characterId;
    }

    private void initRsbConverters()
    {
        final ProtocolBufferConverter<JointData> jointDataConverter = new ProtocolBufferConverter<JointData>(JointData.getDefaultInstance());
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

    private void initJoints()
    {
        final RemoteServer server = Factory.getInstance().createRemoteServer(RSBEmbodimentConstants.JOINTDATACONFIG_CATEGORY);
        try
        {
            server.activate();
            Rsbembodiments.JointDataConfigReply reply = server.call(RSBEmbodimentConstants.JOINTDATACONFIG_REQUEST_FUNCTION,
                    Rsbembodiments.JointDataConfigRequest.newBuilder().setId(characterId).build());
            synchronized(submitJointLock)
            {
                submitJoint = VJointRsbUtils.toVJoint(reply.getJointList());
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
        initRsbConverters();
        initJoints();
        initInformer();
    }

    @Override
    public void copy()
    {
        // construct float list for rotations, send with informer
        JointData jd = JointData.newBuilder().addAllData(Arrays.asList(1f, 2f, 3f)).build();
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
