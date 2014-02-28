package asap.rsbembodiments;

import rsb.Event;
import rsb.Factory;
import rsb.RSBException;
import rsb.patterns.EventCallback;
import rsb.patterns.LocalServer;

public class RsbRpcReceive
{
    public static class EchoCallback extends EventCallback
    {
        @Override
        public Event invoke(final Event request) throws Throwable
        {
            return new Event(String.class, request.getData());
        }

    }

    public static void main(String args[]) throws RSBException
    {
        final LocalServer server = Factory.getInstance().createLocalServer("/example/server");
        server.activate();

        // Add method an "echo" method, implemented by EchoCallback.
        server.addMethod("echo", new EchoCallback());

        // Block until server.deactivate or process shutdown
        server.waitForShutdown();
    }
}
