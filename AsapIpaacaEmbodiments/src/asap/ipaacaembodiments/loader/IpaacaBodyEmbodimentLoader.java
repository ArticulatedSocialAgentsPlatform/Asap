package asap.ipaacaembodiments.loader;

import hmi.animation.RenamingMap;
import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.ipaacaembodiments.IpaacaBodyEmbodiment;

import com.google.common.collect.BiMap;

/**
 * Loads an IpaacaBodyEmbodiment, requires an IpaacaEmbodiment
 * @author hvanwelbergen
 * 
 */
public class IpaacaBodyEmbodimentLoader implements EmbodimentLoader
{
    private IpaacaBodyEmbodiment embodiment;
    private String id;
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
        
        ClockDrivenCopyEnvironment copyEnv = ArrayUtils.getFirstClassOfType(environments, ClockDrivenCopyEnvironment.class);
        IpaacaEmbodimentLoader ldr = ArrayUtils.getFirstClassOfType(requiredLoaders, IpaacaEmbodimentLoader.class);
        if (copyEnv == null)
        {
            throw new XMLScanException("IpaacaBodyEmbodimentLoader requires an ClockDrivenCopyEnvironment");
        }
        
        if (ldr == null)
        {
            throw new XMLScanException("IpaacaBodyEmbodimentLoader requires an IpaacaEmbodimentLoader");
        }
        
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }
        embodiment = new IpaacaBodyEmbodiment(id, ldr.getEmbodiment());
        
        if (renamingFile == null)
        {
            throw new XMLScanException("IpaacaBodyEmbodimentLoader requires inner renaming element");
        }
        BiMap<String, String> renamingMap = RenamingMap.renamingMapFromFileOnClasspath(renamingFile);
        embodiment.init(renamingMap, renamingMap.values());
        copyEnv.addCopyEmbodiment(embodiment);

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
