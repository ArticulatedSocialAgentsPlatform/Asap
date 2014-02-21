package asap.rsbembodiments;

import java.util.Arrays;

import rsb.Factory;
import rsb.Informer;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import asap.rsbembodiments.Rsbembodiments.JointData;

public class RsbSendExample
{
    public static void main(String args[]) throws RSBException, InterruptedException
    {
        final ProtocolBufferConverter<JointData> converter = new ProtocolBufferConverter<JointData>(JointData.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(converter);
        
        final Factory factory = Factory.getInstance();
        final Informer<JointData> informer = factory.createInformer("/example/informer");
        informer.activate();

        JointData jd = JointData.newBuilder().addAllData(Arrays.asList(1f,2f,3f)).build();
        informer.send(jd);
        informer.deactivate();
    }
}
