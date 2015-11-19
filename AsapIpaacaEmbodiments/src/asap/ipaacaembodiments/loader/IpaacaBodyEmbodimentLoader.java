/*******************************************************************************
 *******************************************************************************/
package asap.ipaacaembodiments.loader;

import hmi.animation.RenamingXMLMap;
import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.util.Resources;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;
import ipaaca.Initializer;

import java.io.IOException;
import java.util.HashMap;

import asap.ipaacaembodiments.IpaacaBodyEmbodiment;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;

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
    private BiMap<String,String> skeletonRenaming = null;    

    static
    {
        Initializer.initializeIpaacaRsb();
    }

    
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
        
        if (skeletonRenaming == null)
        {
            throw new XMLScanException("IpaacaBodyEmbodimentLoader requires inner renaming element");
        }
        embodiment.init(skeletonRenaming, ImmutableList.copyOf(skeletonRenaming.values()));
        copyEnv.addCopyEmbodiment(embodiment);

    }

    private BiMap<String,String> getRenamingMap(String mappingFile)throws IOException
    {
        RenamingXMLMap map = new RenamingXMLMap();
        map.readXML(new XMLTokenizer(new Resources("").getInputStream(mappingFile)));
        return map.getRenamingMap();
    }
    
    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("renaming"))
        {
            attrMap = tokenizer.getAttributes();
            String skelRenamingFile = adapter.getRequiredAttribute("skeletonRenamingFile",attrMap,tokenizer);
            skeletonRenaming = getRenamingMap(skelRenamingFile);
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
    public IpaacaBodyEmbodiment getEmbodiment()
    {
        return embodiment;
    }

}
