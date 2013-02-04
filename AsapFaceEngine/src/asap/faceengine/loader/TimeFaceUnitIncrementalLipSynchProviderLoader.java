package asap.faceengine.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.faceengine.lipsync.TimedFaceUnitIncrementalLipSynchProvider;
import asap.faceengine.viseme.VisemeBinding;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizerembodiments.IncrementalLipSynchProviderLoader;

/**
 * Loader for TimeFaceUnitIncrementalLipSynchProvider
 * @author hvanwelbergen
 */
public class TimeFaceUnitIncrementalLipSynchProviderLoader implements IncrementalLipSynchProviderLoader
{
    private IncrementalLipSynchProvider lipSyncProvider;
    private String id;
    
    
    @Override
    public IncrementalLipSynchProvider getLipSyncProvider()
    {
        return lipSyncProvider;
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
        this.id = loaderId;
        
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
            throw tokenizer.getXMLScanException("TimedFaceUnitIncrementalLipSynchProvider requires a visimebinding.");
        }

        lipSyncProvider = new TimedFaceUnitIncrementalLipSynchProvider(visBinding, fal.getFaceController(), fal.getPlanManager());
        
    }

    @Override
    public void unload()
    {
                
    }
}
