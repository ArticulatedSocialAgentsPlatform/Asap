/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.inputs.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.environmentbase.SensorLoader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.livemocapengine.inputs.KeyboardInput;

/**
 * Loads the KeyboardInput
 * @author Herwin
 * 
 */
public class KeyboardInputLoader implements SensorLoader
{
    private String id = "";
    private KeyboardInput sensor;

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
