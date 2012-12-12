package asap.ipaacaembodiments.loader;

import hmi.animation.RenamingXMLMap;
import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.util.Resources;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.ipaacaembodiments.IpaacaBodyEmbodiment;
import asap.ipaacaembodiments.IpaacaEmbodiment;
import asap.ipaacaembodiments.IpaacaFaceAndBodyEmbodiment;
import asap.ipaacaembodiments.IpaacaFaceController;
import asap.ipaacaembodiments.IpaacaFaceEmbodiment;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;

/**
 * Loader for the IpaacaFaceAndBodyEmbodiment
 * @author hvanwelbergen
 */
public class IpaacaFaceAndBodyEmbodimentLoader implements EmbodimentLoader
{
    private String id;
    private IpaacaFaceAndBodyEmbodiment embodiment;
    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private BiMap<String,String> skeletonRenaming = null;
    private BiMap<String,String> morphRenaming = HashBiMap.create();

    
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
            throw new XMLScanException("IpaacaFaceAndBodyEmbodimentLoader requires an ClockDrivenCopyEnvironment");
        }
        
        if (ldr == null)
        {
            throw new XMLScanException("IpaacaFaceAndBodyEmbodimentLoader requires an IpaacaEmbodimentLoader");
        }
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }     
        
        IpaacaEmbodiment ipEmb = ldr.getEmbodiment();
        
        IpaacaFaceController fc = new IpaacaFaceController(ldr.getEmbodiment(), morphRenaming);
        IpaacaFaceEmbodiment faceEmbodiment = new IpaacaFaceEmbodiment(fc);        
        IpaacaBodyEmbodiment bodyEmbodiment = new IpaacaBodyEmbodiment(id, ipEmb);
        bodyEmbodiment.init(skeletonRenaming, ImmutableList.copyOf(skeletonRenaming.values()));
        embodiment = new IpaacaFaceAndBodyEmbodiment(id, ipEmb, faceEmbodiment,bodyEmbodiment);
           
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
            
            attrMap = tokenizer.getAttributes();
            String skelRenamingFile = adapter.getRequiredAttribute("skeletonRenamingFile",attrMap,tokenizer);
            skeletonRenaming = getRenamingMap(skelRenamingFile);
            
            String morphsRenamingFile = adapter.getOptionalAttribute("morphRenamingFile",attrMap);            
            if(morphsRenamingFile!=null)
            {
                morphRenaming = getRenamingMap(morphsRenamingFile);
            }            
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
