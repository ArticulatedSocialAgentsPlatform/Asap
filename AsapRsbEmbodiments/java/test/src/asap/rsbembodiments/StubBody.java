package asap.rsbembodiments;

import hmi.animation.VJoint;
import rsb.Event;
import rsb.Factory;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.EventCallback;
import rsb.patterns.LocalServer;
import asap.rsbembodiments.Rsbembodiments.AnimationData;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigRequest;
import asap.rsbembodiments.util.VJointRsbUtils;

public class StubBody
{
    private final LocalServer server;
    private final VJoint vjoint;

    private class JointDataConfigCallback extends EventCallback
    {
        @Override
        public Event invoke(final Event request) throws Throwable
        {
            return new Event(AnimationDataConfigReply.class, AnimationDataConfigReply.newBuilder()
                    .setSkeleton(VJointRsbUtils.toRsbSkeleton(vjoint)).build());
        }
    }

    public void deactivate() throws RSBException, InterruptedException
    {
        server.deactivate();
    }

    public void startServer() throws RSBException
    {
        new Thread()
        {
            @Override
            public void run()
            {
                // Block until server.deactivate or process shutdown
                server.waitForShutdown();
            }
        };
    }

    public StubBody(VJoint root) throws RSBException
    {
        this.vjoint = root;
        final ProtocolBufferConverter<AnimationData> jointDataConverter = new ProtocolBufferConverter<AnimationData>(
                AnimationData.getDefaultInstance());
        final ProtocolBufferConverter<AnimationDataConfigRequest> jointDataReqConverter = new ProtocolBufferConverter<AnimationDataConfigRequest>(
                AnimationDataConfigRequest.getDefaultInstance());
        final ProtocolBufferConverter<AnimationDataConfigReply> jointDataConfigReplyConverter = new ProtocolBufferConverter<AnimationDataConfigReply>(
                AnimationDataConfigReply.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataReqConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConfigReplyConverter);

        server = Factory.getInstance().createLocalServer(RSBEmbodimentConstants.ANIMATIONDATACONFIG_CATEGORY);
        server.activate();

        server.addMethod(RSBEmbodimentConstants.ANIMATIONDATACONFIG_REQUEST_FUNCTION, new JointDataConfigCallback());
    }
}
