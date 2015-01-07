/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaembodiments.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.ipaacaembodiments.IpaacaEmbodiment;

/**
 * Loads an IpaacaEmbodiment
 * @author hvanwelbergen
 * 
 */
public class IpaacaEmbodimentLoader implements EmbodimentLoader
{
    private IpaacaEmbodiment embodiment;
    private String id;

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
        embodiment = new IpaacaEmbodiment();
        embodiment.initialize();
    }

    @Override
    public void unload()
    {
        embodiment.shutdown();

    }

    @Override
    public IpaacaEmbodiment getEmbodiment()
    {
        return embodiment;
    }

}
