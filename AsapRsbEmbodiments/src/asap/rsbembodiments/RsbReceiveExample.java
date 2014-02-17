package asap.rsbembodiments;

import rsb.AbstractEventHandler;
import rsb.Event;
import rsb.Factory;
import rsb.Listener;

public class RsbReceiveExample extends AbstractEventHandler
{
    @Override
    public void handleEvent(final Event event)
    {
        System.out.println("Received event " + event.toString());
        System.out.println("Data: "+ event.getData());
    }

    public static void main(final String[] args) throws Throwable
    {
        // Get a factory instance to create new RSB objects.
        final Factory factory = Factory.getInstance();

        // Create a Listener instance on the specified scope that will
        // receive events and dispatch them asynchronously to all
        // registered handlers; activate the listener.
        final Listener listener = factory.createListener("/example/informer");
        listener.activate();

        try
        {
            // Add an EventHandler that will print events when they
            // are received.
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
