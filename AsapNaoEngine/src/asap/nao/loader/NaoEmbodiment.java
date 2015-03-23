/*******************************************************************************
 *******************************************************************************/
package asap.nao.loader;

import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.nao.Nao;

/** Take care of its own loading from XML. */
public class NaoEmbodiment implements EmbodimentLoader, Embodiment
{
    private Nao nao = null;

    private String id = "";

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return id;
    }

    /**
     * Creates a Nao class to get a connection with the Nao
     */
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        setId(loaderId);
        nao = new Nao();
        return;
    }

    @Override
    public void unload()
    {
        throw new RuntimeException("Missing functionality to unload Nao");
    }

    /** Return this embodiment */
    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }

    /** provide access to the abstract Nao interface of Nao, which independent of whether it is a robot nao or a simulation nao. */

    public Nao getNao()
    {
        return nao;
    }

}
