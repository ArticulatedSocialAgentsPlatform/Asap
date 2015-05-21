/*******************************************************************************
 *******************************************************************************/
package hmi.jnaoqiembodiment.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jnaoqiembodiment.NaoFaceEmbodiment;
import hmi.jnaoqiembodiment.NaoQiEmbodiment;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

/**
 * Constructs a NaoFaceEmbodiment, requires a NaoQiEmbodimentLoader
 * @author welberge
 */
public class NaoFaceEmbodimentLoader implements EmbodimentLoader
{
    private String id = "";
    private NaoFaceEmbodiment embodiment;

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
        embodiment = new NaoFaceEmbodiment(id, nqEmbodiment.getLedsProxy());
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public NaoFaceEmbodiment getEmbodiment()
    {
        return embodiment;
    }

    @Override
    public void unload()
    {
        embodiment.shutdown();
    }
}
