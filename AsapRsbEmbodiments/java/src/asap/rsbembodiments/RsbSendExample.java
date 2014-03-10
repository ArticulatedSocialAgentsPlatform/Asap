package asap.rsbembodiments;

import java.util.Arrays;

import rsb.Factory;
import rsb.Informer;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import asap.rsbembodiments.Rsbembodiments.AnimationData;

public class RsbSendExample
{
    public static void main(String args[]) throws RSBException, InterruptedException
    {
        final ProtocolBufferConverter<AnimationData> converter = new ProtocolBufferConverter<AnimationData>(AnimationData.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(converter);
        
        final Factory factory = Factory.getInstance();
        final Informer<AnimationData> informer = factory.createInformer("/example/informer");
        informer.activate();

        AnimationData jd = AnimationData.newBuilder().addAllJointQuats(Arrays.asList(1f,2f,3f,4f)).build();
        informer.send(jd);
        informer.deactivate();
    }
}
