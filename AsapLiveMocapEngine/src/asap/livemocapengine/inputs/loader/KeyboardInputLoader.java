package asap.livemocapengine.inputs.loader;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.environment.AsapVirtualHuman;
import asap.environment.Loader;
import asap.environment.SensorLoader;
import asap.livemocapengine.inputs.KeyboardInput;
import asap.utils.Environment;

/**
 * Loads the KeyboardInput
 * @author Herwin
 *
 */
public class KeyboardInputLoader implements SensorLoader
{
    private String id="";
    private KeyboardInput sensor;
    
    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader... requiredLoaders)
            throws IOException
    {
        this.id = newId;
        /*
        JFrameEmbodiment jframeEmbodiment = null;
        for(Loader l:requiredLoaders)
        {
            if(l instanceof JFrameEmbodiment)
            {
                jframeEmbodiment = (JFrameEmbodiment)l;
            }
        }
        if(jframeEmbodiment==null)
        {
            throw new XMLScanException("KeyboardEulerInput requires a JFrameEmbodiment");
        }     
        */   
        sensor = new KeyboardInput(id);        
    }

    @Override
    public void unload()
    {
                
    }

    @Override
    public KeyboardInput getSensor()
    {
        return sensor;
    }
}
