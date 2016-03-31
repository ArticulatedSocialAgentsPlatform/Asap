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

import asap.livemocapengine.inputs.RemoteFACSFaceInput;

/**
 * Loads a RemoteFACSFaceInput Sensor
 * @author welberge
 * 
 */
public class RemoteFACSFaceInputLoader implements SensorLoader
{
    private String id = "";
    private RemoteFACSFaceInput facsFaceInput;

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public Sensor getSensor()
    {

        return facsFaceInput;
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
        facsFaceInput = new RemoteFACSFaceInput(loaderId);
        facsFaceInput.connectToServer(rh.getHostName(), rh.getPort());
    }

    @Override
    public void unload()
    {
        facsFaceInput.shutdown();
    }
}
