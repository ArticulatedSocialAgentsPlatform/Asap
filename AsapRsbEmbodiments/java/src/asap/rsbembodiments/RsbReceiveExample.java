package asap.rsbembodiments;

import rsb.AbstractDataHandler;
import rsb.Factory;
import rsb.Listener;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import asap.rsbembodiments.Rsbembodiments.JointData;

public class RsbReceiveExample extends AbstractDataHandler<JointData>
{
    @Override
    public void handleEvent(final JointData data)
    {
        System.out.println("jointData: "+data.getDataList());
    }

    public static void main(final String[] args) throws Throwable
    {
        final ProtocolBufferConverter<JointData> converter = new ProtocolBufferConverter<JointData>(JointData.getDefaultInstance());
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
