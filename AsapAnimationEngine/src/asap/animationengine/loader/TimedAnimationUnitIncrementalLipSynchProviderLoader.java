package asap.animationengine.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.animationengine.gesturebinding.SpeechBinding;
import asap.animationengine.lipsync.TimedAnimationUnitIncrementalLipSynchProvider;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.IncrementalLipSynchProviderLoader;

/**
 * Loader for TimedAnimationUnitLipIncrementalLipSynchProvider
 * @author Herwin
 */
public class TimedAnimationUnitIncrementalLipSynchProviderLoader implements IncrementalLipSynchProviderLoader
{
    private String id;
    private IncrementalLipSynchProvider lipSyncProvider;

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
        MixedAnimationEngineLoader ael = null;
        AsapRealizerEmbodiment are = null;
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
            throw new RuntimeException(
                    "TimedAnimationUnitLipSynchProviderLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }
        SpeechBinding sb = SpeechBindingLoader.load(tokenizer);
        if (sb == null)
        {
            throw tokenizer.getXMLScanException("TimedAnimationUnitLipSynchProviderLoaderTest requires a speechbinding.");
        }
        lipSyncProvider = new TimedAnimationUnitIncrementalLipSynchProvider(sb, ael.getAnimationPlayer(), ael.getPlanManager(),
                are.getPegBoard());

    }

    @Override
    public void unload()
    {

    }

    @Override
    public IncrementalLipSynchProvider getLipSyncProvider()
    {
        return lipSyncProvider;
    }

}
