package asap.ipaacaembodiments.loader;

import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.ipaacaembodiments.IpaacaEmbodiment;
import asap.ipaacaembodiments.IpaacaFaceController;
import asap.ipaacaembodiments.IpaacaFaceEmbodiment;

/**
 * Loads an IpaacaFaceEmbodiment, requires an IpaacaEmbodiment
 * @author hvanwelbergen
 * 
 */
public class IpaacaFaceEmbodimentLoader implements EmbodimentLoader
{
    private IpaacaFaceEmbodiment embodiment;
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
        ClockDrivenCopyEnvironment copyEnv=null;
        
        IpaacaEmbodiment ipEmb = null;
        for (Loader l : requiredLoaders)
        {
            if (l instanceof IpaacaEmbodimentLoader)
            {
                ipEmb = ((IpaacaEmbodimentLoader) l).getEmbodiment();
            }
        }
        for (Environment env: environments)
        {
            if(env instanceof ClockDrivenCopyEnvironment)
            {
                copyEnv = (ClockDrivenCopyEnvironment)env;                
            }
        }
        
        if(copyEnv == null)
        {
            throw new XMLScanException("IpaacaFaceEmbodimentLoader requires an ClockDrivenCopyEnvironment");
        }
        
        if(ipEmb == null)
        {
            throw new XMLScanException("IpaacaFaceEmbodimentLoader requires an IpaacaEmbodimentLoader");
        }
        IpaacaFaceController fc = new IpaacaFaceController(ipEmb);
        embodiment = new IpaacaFaceEmbodiment(fc);
        copyEnv.addCopyEmbodiment(embodiment);
    }

    @Override
    public void unload()
    {

    }

    @Override
    public IpaacaFaceEmbodiment getEmbodiment()
    {
        return embodiment;
    }

}
