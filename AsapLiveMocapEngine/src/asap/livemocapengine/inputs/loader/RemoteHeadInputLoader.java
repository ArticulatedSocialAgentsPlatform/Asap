package asap.livemocapengine.inputs.loader;

import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;

import asap.environment.AsapVirtualHuman;
import asap.environment.Loader;
import asap.environment.SensorLoader;
import asap.livemocapengine.inputs.RemoteHeadInput;
import asap.utils.Environment;
import asap.utils.Sensor;

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

    private static class ServerInfo extends XMLStructureAdapter
    {
        @Getter private String hostName;
        @Getter private int port;
        
        private static final String XMLTAG = "serverinfo";

        public static String xmlTag()
        {
            return XMLTAG;
        }

        @Override
        public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {
            hostName = getRequiredAttribute("host", attrMap, tokenizer);
            port = getRequiredIntAttribute("port", attrMap, tokenizer);        
            super.decodeAttributes(attrMap, tokenizer);
        }
        
        @Override
        public String getXMLTag()
        {
            return XMLTAG;
        }
    }
    
    @Override
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader... requiredLoaders)
            throws IOException
    {
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
        headInput = new RemoteHeadInput();
        headInput.connectToServer(rh.getHostName(), rh.getPort());
    }

    @Override
    public void unload()
    {
        // TODO stop headInput reading thread        
    }

    @Override
    public Sensor getSensor()
    {
        return headInput;
    }

}
