package asap.livemocapengine.inputs;

import hmi.faceembodiments.AUConfig;
import hmi.faceembodiments.Side;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;


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

    private String hostName;
    private int port;
    
    @Override
    public String getId()
    {
        return id;
    }

    public void connectToServer(String hostName, int port)
    {
        this.hostName = hostName;
    		this.port = port;
        connectToServer();
        
        serverThread = new MyThread();
        serverThread.start();
    }
    
    public void connectToServer()
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
            in = null;
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
                            			aus[i] = new AUConfig(Side.LEFT, i, Float.valueOf(recValues[i]));
                            		}
                            		for( int i=42; i<84; i++ ) {
                            			aus[i] = new AUConfig(Side.RIGHT, i-42, Float.valueOf(recValues[i]));
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
                } else {
                		connectToServer();
                }
            }
        }
    }
    @Override
    public synchronized AUConfig[] getAUConfigs()
    {
        return aus;
    }

}
