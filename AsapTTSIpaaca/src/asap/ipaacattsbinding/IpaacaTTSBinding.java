package asap.ipaacattsbinding;

import saiba.bml.core.SpeechBehaviour;
import hmi.tts.BMLTTSBridge;
import hmi.tts.util.PhonemeToVisemeMapping;
import asap.speechengine.ttsbinding.TTSBinding;
import asap.tts.ipaaca.IpaacaTTSGenerator;

/**
 * Binds SpeechBehaviour to Ipaaca TTS generation.
 * @author hvanwelbergen
 *
 */
public class IpaacaTTSBinding extends TTSBinding
{
    private IpaacaTTSGenerator ipaacaTtsGenerator;
    
    public IpaacaTTSBinding(PhonemeToVisemeMapping ptv)
    {
        ipaacaTtsGenerator = new IpaacaTTSGenerator(ptv);
        ttsGenerator = ipaacaTtsGenerator;
        
        ttsBridgeMap.put(SpeechBehaviour.class, new BMLTTSBridge(ipaacaTtsGenerator));        
    }
    
    @Override
    public void cleanup()
    {
        ipaacaTtsGenerator.close();
    }
}
