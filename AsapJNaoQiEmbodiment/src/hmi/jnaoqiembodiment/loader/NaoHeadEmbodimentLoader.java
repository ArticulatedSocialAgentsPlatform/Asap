package hmi.jnaoqiembodiment.loader;

import hmi.jnaoqiembodiment.NaoHeadEmbodiment;
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
 * Constructs a NaoHeadEmbodiment, requires a NaoQiEmbodimentLoader
 * @author welberge
 *
 */
public class NaoHeadEmbodimentLoader implements EmbodimentLoader
{
    private String id = "";
    private NaoHeadEmbodiment embodiment;

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
        embodiment = new NaoHeadEmbodiment(id, nqEmbodiment.getDCMProxy());
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
