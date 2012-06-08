package asap.livemocapengine.inputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import asap.utils.AUConfig;

/**
 * Reads head input from Mark's sensor system
 * @author welberge
 *
 */
public class RemoteFACSFaceInput implements FACSFaceInput
{
    private String id;
    private BufferedReader in;
    private MyThread serverThread;
    //private final static String HOST = "130.89.228.90";
    //private final static int PORT = 9123;
    
    @Override
    public String getId()
    {
        return id;
    }

    public void connectToServer(String hostName, int port)
    {
        try
        {
            Socket socket = new Socket(hostName, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (UnknownHostException e)
        {
            System.out.println("Unknown host " + hostName);
            in = null;
        }
        catch (IOException e)
        {
            System.out.println("Could not connect to " + hostName);
            in = null;
        }
        serverThread = new MyThread();
        if (in != null)
        {
            serverThread.start();
        }
    }
    
    class MyThread extends Thread
    {
        @Override
        public void run()
        {
            while (true)
            {
                if (in != null)
                {
                    String line = null;
                    try
                    {
                        line = in.readLine();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        in = null;
                    }
                    
                    //TODO: new parsing stuff here!
                    if (line != null)
                    {
                        String[] recValues = line.split(" ");
                        if (recValues.length == 84)
                        {
                            synchronized (this)
                            {
                                /*
                                faceValues = new Float[84];
                                for (int i = 0; i < 84; i++)
                                {
                                    faceValues[i] = Float.valueOf(recValues[i]);
                                }
                                */
                            }
                        }
                    }
                }
            }
        }
    }
    @Override
    public AUConfig[] getAUConfigs()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
