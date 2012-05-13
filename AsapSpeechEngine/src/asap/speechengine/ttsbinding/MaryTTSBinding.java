package asap.speechengine.ttsbinding;

import saiba.bml.core.SpeechBehaviour;
import hmi.bml.ext.maryxml.MaryXMLBehaviour;
import hmi.bml.ext.maryxml.MaryWordsBehaviour;
import hmi.bml.ext.maryxml.MaryAllophonesBehaviour;
import hmi.bml.ext.ssml.SSMLBehaviour;
import hmi.tts.BMLTTSBridge;
import hmi.tts.mary.MarySSMLTTSBridge;
import hmi.tts.mary.MaryTTSGenerator;
import hmi.tts.mary.MaryXMLTTSBridge;
import hmi.tts.mary.MaryWordsTTSBridge;
import hmi.tts.mary.MaryAllophonesTTSBridge;
import hmi.tts.util.PhonemeToVisemeMapping;

/**
 * Binds SpeechBehaviour, SSMLBehaviour, MaryXMLBehaviour, MaryWordsBehaviour and MaryAllophonesBehaviour
 * to the MaryTTS speech generation
 * @author welberge
 *
 */
public class MaryTTSBinding extends TTSBinding
{
    private MaryTTSGenerator maryTTSGenerator;
    public MaryTTSBinding(String marydir, PhonemeToVisemeMapping ptv)
    {
        try
        {
            maryTTSGenerator = new MaryTTSGenerator(marydir, ptv);
            ttsGenerator = maryTTSGenerator;
        } 
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }        
        ttsBridgeMap.put(SpeechBehaviour.class, new BMLTTSBridge(maryTTSGenerator));
        ttsBridgeMap.put(SSMLBehaviour.class, new MarySSMLTTSBridge(maryTTSGenerator));
        ttsBridgeMap.put(MaryXMLBehaviour.class, new MaryXMLTTSBridge(maryTTSGenerator));
        ttsBridgeMap.put(MaryWordsBehaviour.class, new MaryWordsTTSBridge(maryTTSGenerator));
        ttsBridgeMap.put(MaryAllophonesBehaviour.class, new MaryAllophonesTTSBridge(maryTTSGenerator));
        
        supportedBehaviours.add(SSMLBehaviour.class);
        supportedBehaviours.add(MaryXMLBehaviour.class);
        supportedBehaviours.add(MaryWordsBehaviour.class);
        supportedBehaviours.add(MaryAllophonesBehaviour.class);
    }
    @Override
    public void cleanup()
    {
            
    }
}
