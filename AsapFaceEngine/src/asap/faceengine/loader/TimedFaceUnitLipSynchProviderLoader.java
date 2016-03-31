/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.faceengine.lipsync.TimedFaceUnitLipSynchProvider;
import asap.faceengine.viseme.MorphVisemeBinding;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizerembodiments.AsapRealizerEmbodiment;
import asap.realizerembodiments.LipSynchProviderLoader;

/**
 * Loads a TimedFaceUnitLipSynchProvider
 * @author hvanwelbergen
 */
public class TimedFaceUnitLipSynchProviderLoader implements LipSynchProviderLoader
{
    private String id;
    private LipSynchProvider lipSyncProvider;

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
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        setId(loaderId);

        FaceEngineLoader fal = ArrayUtils.getFirstClassOfType(requiredLoaders, FaceEngineLoader.class);
        if (fal == null)
        {
            throw tokenizer.getXMLScanException("TimedFaceUnitLipSynchProviderLoader requires FaceEngineLoader.");
        }

        AsapRealizerEmbodiment are = ArrayUtils.getFirstClassOfType(requiredLoaders, AsapRealizerEmbodiment.class);
        if (are == null)
        {
            throw new RuntimeException(
                    "TimedFaceUnitLipSynchProviderLoader requires an EmbodimentLoader containing a AsapRealizerEmbodiment");
        }

        MorphVisemeBinding visBinding = VisemeBindingLoader.loadMorphVisemeBinding(tokenizer);

        if (visBinding == null)
        {
            throw tokenizer.getXMLScanException("CoarticulationLipSyncProvider requires a visimebinding.");
        }

        lipSyncProvider = new TimedFaceUnitLipSynchProvider(visBinding, fal.getFaceController(), fal.getPlanManager(), are.getPegBoard());
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
