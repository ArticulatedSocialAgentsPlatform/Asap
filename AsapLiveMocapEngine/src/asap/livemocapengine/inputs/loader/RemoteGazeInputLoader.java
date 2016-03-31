/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.inputs.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.environmentbase.Sensor;
import hmi.environmentbase.SensorLoader;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.livemocapengine.inputs.RemoteGazeInput;


/**
 * Loads a RemoteGazeInput sensor
 * @author vangennep
 */
public class RemoteGazeInputLoader implements SensorLoader {
    private String id = "";
    private RemoteGazeInput gazeinput;

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        this.id = loaderId;
        ServerInfo rh = null;
        if (tokenizer.atSTag(ServerInfo.xmlTag()))
        {
            rh = new ServerInfo();
            rh.readXML(tokenizer);
        }
        if (rh == null)
        {
            throw new XMLScanException("No inner serverinfo element in RemoteGazeInputLoader");
        }
        gazeinput = new RemoteGazeInput(loaderId);
        gazeinput.connectToServer(rh.getHostName(), rh.getPort());
    }

    @Override
    public void unload()
    {
    	gazeinput.shutdown();
    }

    @Override
    public Sensor getSensor()
    {
        return gazeinput;
    }	
}
