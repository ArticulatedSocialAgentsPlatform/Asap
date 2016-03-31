/*******************************************************************************
 *******************************************************************************/
package hmi.jnaoqiembodiment.loader;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jnaoqiembodiment.NaoHeadEmbodiment;
import hmi.jnaoqiembodiment.NaoQiEmbodiment;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

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
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        this.id = loaderId;

        NaoQiEmbodiment nqEmbodiment = null;
        for (Loader l : requiredLoaders)
        {
            if (l instanceof NaoQiEmbodimentLoader)
            {
                nqEmbodiment = ((NaoQiEmbodimentLoader) l).getEmbodiment();
            }
        }
        if (nqEmbodiment == null)
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
