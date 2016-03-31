/*******************************************************************************
 *******************************************************************************/
package asap.textengine.loader;

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import asap.realizer.lipsync.LipSynchProvider;
import asap.realizerembodiments.LipSynchProviderLoader;
import asap.textengine.lipsync.TimedSpeechTextUnitLipSynchProvider;

/**
 * Loader for a TimedTextSpeechUnitLipSynchProvider.
 * 
 * @author Jordi Hendrix
 */
public class TimedSpeechTextUnitLipSynchProviderLoader implements LipSynchProviderLoader
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
        TextEngineLoader tel = null;

        for (Loader e : requiredLoaders)
        {
            if (e instanceof TextEngineLoader)
            {
                tel = (TextEngineLoader) e;
            }
        }

        if (tel == null)
        {
            throw new RuntimeException("TimedTextSpeechUnitLipSynchProviderLoader requires an Embodiment of type TextSpeechEngineLoader");
        }

        lipSyncProvider = new TimedSpeechTextUnitLipSynchProvider(tel.getPlanManager(), tel.getTextOutput());
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
