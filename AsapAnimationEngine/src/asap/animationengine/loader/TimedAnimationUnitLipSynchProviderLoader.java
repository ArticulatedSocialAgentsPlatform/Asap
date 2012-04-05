package asap.animationengine.loader;

import asap.animationengine.gesturebinding.SpeechBinding;
import asap.animationengine.lipsync.TimedAnimationUnitLipSynchProvider;
import hmi.elckerlyc.lipsync.LipSynchProvider;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.environment.AsapVirtualHuman;
import asap.environment.LipSynchProviderLoader;
import asap.environment.Loader;
import asap.utils.Environment;

/**
 * Loader for TimedAnimationUnitLipSynchProviderLoader
 * @author Herwin
 * 
 */
public class TimedAnimationUnitLipSynchProviderLoader implements LipSynchProviderLoader
{
    private String id;
    private LipSynchProvider lipSyncProvider;
    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    
    public void setId(String newId)
    {
        id = newId;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String newId, AsapVirtualHuman avh, Environment[] environments, Loader... requiredLoaders)
            throws IOException
    {
        setId(newId);
        
        MixedAnimationEngineLoader ael = null;
        for (Loader e : requiredLoaders)
        {
            if (e instanceof MixedAnimationEngineLoader) ael = (MixedAnimationEngineLoader) e;
        }
        if (ael == null)
        {
            throw tokenizer.getXMLScanException("TimedAnimationUnitLipSynchProviderLoaderTest requires mixedanimationenvironment.");
        }
        SpeechBinding sb = null;

        while (!tokenizer.atETag("Loader"))
        {
            if (tokenizer.atSTag("SpeechBinding"))
            {

                HashMap<String, String> attrMap = tokenizer.getAttributes();
                sb = new SpeechBinding(new Resources(adapter.getOptionalAttribute("basedir", attrMap, "")));
                try
                {
                    sb.readXML(new Resources(adapter.getOptionalAttribute("resources", attrMap, "")).getReader(adapter
                            .getRequiredAttribute("filename", attrMap, tokenizer)));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw new RuntimeException("Cannnot load SpeechBinding: " + e);
                }
                tokenizer.takeEmptyElement("SpeechBinding");
            }
        }
        
        if (sb == null)
        {
            throw tokenizer.getXMLScanException("TimedAnimationUnitLipSynchProviderLoaderTest requires a speechbinding.");
        }
        // read speechbinding
        lipSyncProvider = new TimedAnimationUnitLipSynchProvider(sb, ael.getAnimationPlayer(), ael.getPlanManager(),avh.getPegBoard());
    }

    @Override
    public void unload()
    {

    }

    @Override
    public LipSynchProvider getLipSyncProvider()
    {
        return lipSyncProvider;
    }

}
