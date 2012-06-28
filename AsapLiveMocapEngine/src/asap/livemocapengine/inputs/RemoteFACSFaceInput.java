package asap.livemocapengine.inputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import asap.utils.AUConfig;
import asap.utils.Side;

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
    private AUConfig[] aus = new AUConfig[84];
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
                            		for( int i=0; i<42; i++ ) {
                            			aus[i] = new AUConfig(Side.LEFT, i+1, Float.valueOf(recValues[i]));
                            		}
                            		for( int i=42; i<84; i++ ) {
                            			aus[i] = new AUConfig(Side.RIGHT, i-42+1, Float.valueOf(recValues[i]));
                            		}
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
        return aus;
    }

}
