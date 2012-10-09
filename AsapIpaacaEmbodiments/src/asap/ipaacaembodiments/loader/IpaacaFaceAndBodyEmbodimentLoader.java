package asap.ipaacaembodiments.loader;

import java.io.IOException;
import java.util.HashMap;

import asap.ipaacaembodiments.IpaacaEmbodiment;
import asap.ipaacaembodiments.IpaacaFaceAndBodyEmbodiment;

import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

/**
 * Loader for the IpaacaFaceAndBodyEmbodiment
 * @author hvanwelbergen
 */
public class IpaacaFaceAndBodyEmbodimentLoader implements EmbodimentLoader
{
    private String id;
    private IpaacaFaceAndBodyEmbodiment embodiment;
    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private String renamingFile;
    
    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        
        ClockDrivenCopyEnvironment copyEnv = null;

        IpaacaEmbodiment ipEmb = null;
        for (Loader l : requiredLoaders)
        {
            if (l instanceof IpaacaEmbodimentLoader)
            {
                ipEmb = ((IpaacaEmbodimentLoader) l).getEmbodiment();
            }
        }
        for (Environment env : environments)
        {
            if (env instanceof ClockDrivenCopyEnvironment)
            {
                copyEnv = (ClockDrivenCopyEnvironment) env;
            }
        }

        if (copyEnv == null)
        {
            throw new XMLScanException("IpaacaFaceAndBodyEmbodimentLoader requires an ClockDrivenCopyEnvironment");
        }

        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }

        if (ipEmb == null)
        {
            throw new XMLScanException("IpaacaFaceAndBodyEmbodimentLoader requires an IpaacaEmbodimentLoader");
        }
        
        
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("renaming"))
        {
            attrMap = tokenizer.getAttributes();
            renamingFile = adapter.getRequiredAttribute("renamingFile",attrMap,tokenizer);
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }
        tokenizer.takeEmptyElement("renaming");
    }
    
    @Override
    public void unload()
    {
    }

    @Override
    public Embodiment getEmbodiment()
    {
        return embodiment;
    }

}
