package asap.animationengine.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import asap.animationengine.gesturebinding.SpeechBinding;
import asap.animationengine.lipsync.TimedAnimationUnitLipSynchProvider;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.LipSynchProviderLoader;

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

    private AsapRealizerEmbodiment are = null;
    
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
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments, Loader ... requiredLoaders) throws IOException
    {
        setId(loaderId);
        
        MixedAnimationEngineLoader ael = null;
        for (Loader e : requiredLoaders)
        {
            if (e instanceof MixedAnimationEngineLoader) ael = (MixedAnimationEngineLoader) e;
            if (e instanceof AsapRealizerEmbodiment) are = (AsapRealizerEmbodiment) e;
        }
        if (ael == null)
        {
            throw tokenizer.getXMLScanException("TimedAnimationUnitLipSynchProviderLoader requires mixedanimationenvironment.");
        }
        if (are == null)
        {
            throw new RuntimeException("TimedAnimationUnitLipSynchProviderLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
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
        lipSyncProvider = new TimedAnimationUnitLipSynchProvider(sb, ael.getAnimationPlayer(), ael.getPlanManager(),are.getPegBoard());
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
