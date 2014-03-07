package asap.rsbembodiments;

import rsb.AbstractDataHandler;
import rsb.Factory;
import rsb.Listener;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import asap.rsbembodiments.Rsbembodiments.AnimationData;

public class RsbReceiveExample extends AbstractDataHandler<AnimationData>
{
    @Override
    public void handleEvent(final AnimationData data)
    {
        System.out.println("jointData: "+data.getJointDataList());
    }

    public static void main(final String[] args) throws Throwable
    {
        final ProtocolBufferConverter<AnimationData> converter = new ProtocolBufferConverter<AnimationData>(AnimationData.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(converter);
        final Factory factory = Factory.getInstance();
        final Listener listener = factory.createListener("/example/informer");
        listener.activate();

        try
        {
            listener.addHandler(new RsbReceiveExample(), true);

            // Wait for events.
            while (true)
            {
                Thread.sleep(1);
            }
        }
        finally
        {
            // Deactivate the listener after use.
            listener.deactivate();
        }
    }
}
