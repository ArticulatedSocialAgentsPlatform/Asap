package asap.faceengine.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.faceengine.lipsync.TimedFaceUnitLipSynchProvider;
import asap.faceengine.viseme.VisemeBinding;
import asap.realizer.lipsync.LipSynchProvider;
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

        FaceEngineLoader fal = null;
        for (Loader e : requiredLoaders)
        {
            if (e instanceof FaceEngineLoader) fal = (FaceEngineLoader) e;
        }
        if (fal == null)
        {
            throw tokenizer.getXMLScanException("TimedFaceUnitLipSynchProviderLoader requires FaceEngineLoader.");
        }
        VisemeBinding visBinding = VisemeBindingLoader.load(tokenizer, fal.getFACSConverter());

        if (visBinding == null)
        {
            throw tokenizer.getXMLScanException("TimedFaceUnitLipSynchProvider requires a visimebinding.");
        }

        lipSyncProvider = new TimedFaceUnitLipSynchProvider(visBinding, fal.getFaceController(), fal.getPlanManager());
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
