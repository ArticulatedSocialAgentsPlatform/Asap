/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.inputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.AtomicDouble;

/**
 * Reads gaze input from Mark's sensor system
 * @author vangennep
 */
@Slf4j
public class RemoteGazeInput implements EulerInput {
    private final String id;
    private BufferedReader in;
    private MyThread serverThread;
    
    private AtomicDouble pitch = new AtomicDouble(0);
    private AtomicDouble roll = new AtomicDouble(0);
    private AtomicDouble yaw = new AtomicDouble(0);

    private String hostName;
    private int port;
    private Socket socket = null;
    private volatile boolean shouldStop = false;
    
    public RemoteGazeInput(String id)
    {
        this.id = id;
    }

    // ("localhost", 9123)
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
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charsets.UTF_8));
        }
        catch (UnknownHostException e)
        {
            log.warn("Unknown host");
            in = null;
        }
        catch (IOException e)
        {
            in = null;
        }
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
                log.warn("Exception in shutdown of RemoteGazeInput", e);
            }
        }
    }

    class MyThread extends Thread
    {
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
                        in = null;
                        e.printStackTrace();
                    }
                    if (line != null)
                    {
                    	String[] recValues = line.split(" ");
                        if(recValues.length == 2){
                        	yaw.set(Float.parseFloat(recValues[0]));	//x
                            pitch.set(Float.parseFloat(recValues[1]));	//y                    
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
    public String getId()
    {
        return id;
    }


	@Override
	public float getPitchDegrees() {
		return pitch.floatValue();
	}

	@Override
	public float getYawDegrees() {
		return yaw.floatValue();
	}

	@Override
	public float getRollDegrees() {
		return roll.floatValue();
	}

}
