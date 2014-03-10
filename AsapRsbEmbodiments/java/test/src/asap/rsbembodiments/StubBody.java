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
import asap.rsbembodiments.Rsbembodiments.JointDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.JointDataConfigRequest;
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
            return new Event(JointDataConfigReply.class, JointDataConfigReply.newBuilder()
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
        final ProtocolBufferConverter<JointDataConfigRequest> jointDataReqConverter = new ProtocolBufferConverter<JointDataConfigRequest>(
                JointDataConfigRequest.getDefaultInstance());
        final ProtocolBufferConverter<JointDataConfigReply> jointDataConfigReplyConverter = new ProtocolBufferConverter<JointDataConfigReply>(
                JointDataConfigReply.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataReqConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConfigReplyConverter);

        server = Factory.getInstance().createLocalServer(RSBEmbodimentConstants.JOINTDATACONFIG_CATEGORY);
        server.activate();

        server.addMethod(RSBEmbodimentConstants.JOINTDATACONFIG_REQUEST_FUNCTION, new JointDataConfigCallback());
    }
}
