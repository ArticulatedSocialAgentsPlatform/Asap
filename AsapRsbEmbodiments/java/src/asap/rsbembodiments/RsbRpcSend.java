package asap.rsbembodiments;

import rsb.Factory;
import rsb.patterns.RemoteServer;

public class RsbRpcSend
{
    public static void main(final String[] args) throws Throwable
    {
        // Get remote server object to call exposed request methods of
        // participants
        final RemoteServer server = Factory.getInstance().createRemoteServer("/example/server");
        server.activate();

        // Call remote method and deactivate the server.
        try
        {
            System.out.println("Server replied: " + server.call("echo", 1d));
        }
        finally
        {
            server.deactivate();
        }
    }
}
