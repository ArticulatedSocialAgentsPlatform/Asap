package asap.ipaacaembodiments.loader;

import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

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
        
        ClockDrivenCopyEnvironment copyEnv = ArrayUtils.getFirstClassOfType(environments, ClockDrivenCopyEnvironment.class);
        IpaacaEmbodimentLoader ldr = ArrayUtils.getFirstClassOfType(requiredLoaders, IpaacaEmbodimentLoader.class); 
        
        if(copyEnv == null)
        {
            throw new XMLScanException("IpaacaFaceEmbodimentLoader requires an ClockDrivenCopyEnvironment");
        }
        
        if(ldr == null)
        {
            throw new XMLScanException("IpaacaFaceEmbodimentLoader requires an IpaacaEmbodimentLoader");
        }
        IpaacaFaceController fc = new IpaacaFaceController(ldr.getEmbodiment());
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
