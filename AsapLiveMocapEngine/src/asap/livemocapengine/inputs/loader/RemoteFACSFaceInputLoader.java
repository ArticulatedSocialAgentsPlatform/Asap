package asap.livemocapengine.inputs.loader;

import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.environment.AsapVirtualHuman;
import asap.environment.Loader;
import asap.environment.SensorLoader;
import asap.livemocapengine.inputs.RemoteFACSFaceInput;
import asap.utils.Environment;
import asap.utils.Sensor;

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
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader... requiredLoaders)
            throws IOException
    {
        this.id = newId;
        ServerInfo rh = null;
        if(tokenizer.atSTag(ServerInfo.xmlTag()))
        {
            rh = new ServerInfo();
            rh.readXML(tokenizer);
        }
        if(rh==null)
        {
            throw new XMLScanException("No inner serverinfo element in RemoteHeadInputLoader");
        } 
        facsFaceInput = new RemoteFACSFaceInput();
        facsFaceInput.connectToServer(rh.getHostName(), rh.getPort());
    }

    @Override
    public void unload()
    {
     // TODO stop facsFaceInput reading thread  
    }
}
