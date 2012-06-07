package asap.livemocapengine.inputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Reads head input from Mark's sensor system
 * @author welberge
 */
public class RemoteHeadInput implements EulerInput
{
    private String id;
    private float pitch;
    private float roll;
    private float yaw;
    private BufferedReader in;
    private MyThread serverThread;

    //("localhost", 9123)
    public void connectToServer(String hostName, int port)
    {
        try
        {
            Socket socket = new Socket(hostName, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (UnknownHostException e)
        {
            System.out.println("Unknown host");
            in = null;
        }
        catch (IOException e)
        {
            System.out.println("Could not connect to localhost");
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
                        in = null;
                        e.printStackTrace();
                    }
                    if (line != null)
                    {
                        String[] recValues = line.split(" ");
                        if (recValues.length == 3)
                        {
                            synchronized (this)
                            {
                                roll = Float.parseFloat(recValues[0]);
                                pitch = Float.parseFloat(recValues[1]);
                                yaw = Float.parseFloat(recValues[2]);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public synchronized float getPitchDegrees()
    {
        return pitch;
    }

    @Override
    public synchronized float getYawDegrees()
    {
        return yaw;
    }

    @Override
    public synchronized float getRollDegrees()
    {
        return roll;
    }
}
