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

import asap.livemocapengine.inputs.RemoteHeadInput;

/**
 * Loads a RemoteHeadInput sensor
 * @author welberge
 */
public class RemoteHeadInputLoader implements SensorLoader
{
    private String id = "";
    private RemoteHeadInput headInput;

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
            throw new XMLScanException("No inner serverinfo element in RemoteHeadInputLoader");
        }
        headInput = new RemoteHeadInput(loaderId);
        headInput.connectToServer(rh.getHostName(), rh.getPort());
    }

    @Override
    public void unload()
    {
        headInput.shutdown();
    }

    @Override
    public Sensor getSensor()
    {
        return headInput;
    }

}
