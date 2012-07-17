package hmi.jnaoqiembodiment.loader;

import hmi.jnaoqiembodiment.NaoGazeEmbodiment;
import hmi.jnaoqiembodiment.NaoQiEmbodiment;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.environment.AsapVirtualHuman;
import asap.environment.EmbodimentLoader;
import asap.environment.Loader;
import asap.utils.Embodiment;
import asap.utils.Environment;

/**
 * Constructs a NaoGazeEmbodiment, requires a NaoQiEmbodimentLoader 
 * @author welberge
 */
public class NaoGazeEmbodimentLoader implements EmbodimentLoader
{
    private String id = "";
    private NaoGazeEmbodiment embodiment;
    
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
        
        NaoQiEmbodiment nqEmbodiment = null;
        for (Loader l:requiredLoaders)
        {
            if (l instanceof NaoQiEmbodimentLoader)
            {
                nqEmbodiment = ((NaoQiEmbodimentLoader) l).getEmbodiment();
            }
        }
        if(nqEmbodiment == null)
        {
            throw new XMLScanException("NaoHeadEmbodimentLoader requires an NaoQiEmbodimentLoader");
        }
        embodiment = new NaoGazeEmbodiment(id, nqEmbodiment.getDCMProxy());        
    }

    @Override
    public void unload()
    {
        embodiment.shutdown();        
    }

    @Override
    public Embodiment getEmbodiment()
    {
        return embodiment;
    }

}
