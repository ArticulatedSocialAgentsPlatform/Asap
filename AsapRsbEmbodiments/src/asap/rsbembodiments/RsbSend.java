package asap.rsbembodiments;

import rsb.Factory;
import rsb.Informer;
import rsb.RSBException;

public class RsbSend
{
    public static void main(String args[]) throws RSBException, InterruptedException
    {
     // Get a factory instance to create RSB objects.
        final Factory factory = Factory.getInstance();

        // Create an informer on scope "/exmaple/informer".
        final Informer<String> informer = factory
                .createInformer("/example/informer");

        // Activate the informer to be ready for work
        informer.activate();

        // Send and event using a method that accepts the data and
        // automatically creates an appropriate event internally.
        informer.send("test 1 2 3");

        // As there is no explicit removal model in java, always manually
        // deactivate the informer if it is not needed anymore
        informer.deactivate();

    }
}
