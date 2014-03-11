package asap.rsbembodiments;

import rsb.Factory;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.RemoteServer;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigRequest;

public class RsbRpcSend
{
    public static void main(final String[] args) throws Throwable
    {
        final ProtocolBufferConverter<AnimationDataConfigRequest> converter = new ProtocolBufferConverter<AnimationDataConfigRequest>(
                AnimationDataConfigRequest.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(converter);
        final ProtocolBufferConverter<AnimationDataConfigRequest> converter2 = new ProtocolBufferConverter<AnimationDataConfigRequest>(
                AnimationDataConfigRequest.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(converter2);
        
        // Get remote server object to call exposed request methods of
        // participants
        final RemoteServer server = Factory.getInstance().createRemoteServer("/example/server");
        server.activate();

        // Call remote method and deactivate the server.
        try
        {
            AnimationDataConfigReply reply = server.call("jointDataConfigRequest", Rsbembodiments.AnimationDataConfigRequest
                    .newBuilder().setCharacterId("billie").build());

            System.out.println("Server replied: " + reply.getSkeleton().getJoints(0));
        }
        finally
        {
            server.deactivate();
        }
    }
}
