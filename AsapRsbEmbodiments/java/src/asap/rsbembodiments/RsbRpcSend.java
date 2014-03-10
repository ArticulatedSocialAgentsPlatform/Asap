package asap.rsbembodiments;

import rsb.Factory;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.RemoteServer;
import asap.rsbembodiments.Rsbembodiments.JointDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.JointDataConfigRequest;

public class RsbRpcSend
{
    public static void main(final String[] args) throws Throwable
    {
        final ProtocolBufferConverter<JointDataConfigRequest> converter = new ProtocolBufferConverter<JointDataConfigRequest>(
                JointDataConfigRequest.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(converter);
        final ProtocolBufferConverter<JointDataConfigReply> converter2 = new ProtocolBufferConverter<JointDataConfigReply>(
                JointDataConfigReply.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(converter2);
        
        // Get remote server object to call exposed request methods of
        // participants
        final RemoteServer server = Factory.getInstance().createRemoteServer("/example/server");
        server.activate();

        // Call remote method and deactivate the server.
        try
        {
            Rsbembodiments.JointDataConfigReply reply = server.call("jointDataConfigRequest", Rsbembodiments.JointDataConfigRequest
                    .newBuilder().setId("billie").build());

            System.out.println("Server replied: " + reply.getSkeletonList().get(0).getId());
        }
        finally
        {
            server.deactivate();
        }
    }
}
