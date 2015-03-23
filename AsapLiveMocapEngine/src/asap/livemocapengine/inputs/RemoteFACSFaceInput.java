/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.inputs;

import hmi.faceanimation.model.FACS;
import hmi.faceembodiments.AUConfig;
import hmi.faceembodiments.Side;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Charsets;

/**
 * Reads head input from Mark's sensor system
 * @author welberge
 * //XXX: should really make use of e.g. an ExecutorService rather than hard-coded thread management 
 * (see MultiThreadedPlanPlayer for an example); shutdown is probably a bit fragile now...
 */
@Slf4j
public class RemoteFACSFaceInput implements FACSFaceInput
{
    @Getter
    private final String id;
    
    private BufferedReader in;
    private MyThread serverThread;
    private int numAus = FACS.getActionUnits().size();
    private AUConfig[] aus = new AUConfig[numAus*2];

    private String hostName;
    private int port;
    private Socket socket = null;
    private volatile boolean shouldStop = false;
    
    public RemoteFACSFaceInput(String id)
    {
        this.id = id;
    }
    
    public void shutdown()
    {
        shouldStop = true;        
        if(socket!=null)
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                log.warn("Exception in shutdown of RemoteFACSFaceInput", e);
            }
        }
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
        if(shouldStop)return;
        try
        {
            socket = new Socket(hostName, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(),Charsets.UTF_8));
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
            while (!shouldStop)
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
                        log.warn("IOException face data from socket "+e);
                        in = null;
                    }

                    if (line != null)
                    {   	
                        String[] recValues = line.split(" ");
                        int length = recValues.length;
                        if(length == numAus * 2 ){
                            synchronized (this)
                            {
                            	for (int i = 0; i < length / 2; i++)
                            	{
                            		aus[i] = new AUConfig(Side.LEFT, i, Float.valueOf(recValues[i]));
                            	}
                            	for (int i = length / 2; i < length; i++)
                            	{
                            		aus[i] = new AUConfig(Side.RIGHT, i - length / 2, Float.valueOf(recValues[i]));
                            	}
                            }
                        }                        
                    }
                }
                else
                {
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
