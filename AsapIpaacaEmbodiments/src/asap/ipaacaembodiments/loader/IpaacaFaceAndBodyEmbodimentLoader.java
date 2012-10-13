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
import asap.ipaacaembodiments.IpaacaEmbodiment;
import asap.ipaacaembodiments.IpaacaFaceAndBodyEmbodiment;
import asap.ipaacaembodiments.IpaacaFaceController;
import asap.ipaacaembodiments.IpaacaFaceEmbodiment;

import com.google.common.collect.BiMap;

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
        BiMap<String, String> renamingMap = RenamingMap.renamingMapFromFileOnClasspath(renamingFile);
        IpaacaFaceController fc = new IpaacaFaceController(ldr.getEmbodiment());
        IpaacaFaceEmbodiment faceEmbodiment = new IpaacaFaceEmbodiment(fc);        
        IpaacaBodyEmbodiment bodyEmbodiment = new IpaacaBodyEmbodiment(id, ipEmb);
        bodyEmbodiment.init(renamingMap, renamingMap.values());
        embodiment = new IpaacaFaceAndBodyEmbodiment(id, ipEmb, faceEmbodiment,bodyEmbodiment);
           
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
