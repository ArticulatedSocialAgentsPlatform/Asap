package asap.livemocapengine.inputs.loader;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.environment.AsapVirtualHuman;
import asap.environment.Loader;
import asap.environment.SensorLoader;
import asap.livemocapengine.inputs.KeyboardEulerInput;
import asap.utils.Environment;

/**
 * Loads the KeyboardEulerInput and couples it to a JFrameEmbodiment
 * @author Herwin
 *
 */
public class KeyboardEulerInputLoader implements SensorLoader
{
    private String id="";
    private KeyboardEulerInput sensor;
    
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
        sensor = new KeyboardEulerInput(id);        
    }

    @Override
    public void unload()
    {
                
    }

    @Override
    public KeyboardEulerInput getSensor()
    {
        return sensor;
    }
}
