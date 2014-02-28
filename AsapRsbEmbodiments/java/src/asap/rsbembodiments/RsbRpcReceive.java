package asap.rsbembodiments;

import rsb.Event;
import rsb.Factory;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.EventCallback;
import rsb.patterns.LocalServer;
import asap.rsbembodiments.Rsbembodiments.Joint;
import asap.rsbembodiments.Rsbembodiments.JointDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.JointDataConfigRequest;

public class RsbRpcReceive
{
    public static class EchoCallback extends EventCallback
    {
        @Override
        public Event invoke(final Event request) throws Throwable
        {
            System.out.println("invoke");
            JointDataConfigRequest jdcr = (JointDataConfigRequest)request.getData();
            Joint j = Joint.newBuilder().setId(jdcr.getId()+"-HumanoidRood").setParentId("parent").build();
            return new Event(JointDataConfigReply.class, JointDataConfigReply.newBuilder().addJoint(j).build());            
        }

    }

    public static void main(String args[]) throws RSBException
    {
        final ProtocolBufferConverter<JointDataConfigRequest> converter = new ProtocolBufferConverter<JointDataConfigRequest>(
                JointDataConfigRequest.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(converter);
        final ProtocolBufferConverter<JointDataConfigReply> converter2 = new ProtocolBufferConverter<JointDataConfigReply>(
                JointDataConfigReply.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(converter2);
        
        final LocalServer server = Factory.getInstance().createLocalServer("/example/server");
        server.activate();

        // Add method an "echo" method, implemented by EchoCallback.
        server.addMethod("jointDataConfigRequest", new EchoCallback());
        
        // Block until server.deactivate or process shutdown
        server.waitForShutdown();
    }
}
