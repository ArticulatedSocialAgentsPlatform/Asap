/*******************************************************************************
 *******************************************************************************/
package asap.ipaacattsbinding;

import hmi.tts.BMLTTSBridge;
import hmi.tts.util.PhonemeToVisemeMapping;
import saiba.bml.core.SpeechBehaviour;
import asap.bml.ext.ssml.SSMLBehaviour;
import asap.speechengine.ttsbinding.TTSBinding;
import asap.tts.ipaaca.IpaacaSSMLTTSBridge;
import asap.tts.ipaaca.IpaacaTTSGenerator;
import asap.tts.ipaaca.VisualProsodyAnalyzer;

/**
 * Binds SpeechBehaviour to Ipaaca TTS generation.
 * @author hvanwelbergen
 *
 */
public class IpaacaTTSBinding extends TTSBinding
{
    private IpaacaTTSGenerator ipaacaTtsGenerator;
    
    public IpaacaTTSBinding(PhonemeToVisemeMapping ptv, VisualProsodyAnalyzer vpa)
    {
        ipaacaTtsGenerator = new IpaacaTTSGenerator(ptv,vpa);
        ttsGenerator = ipaacaTtsGenerator;
        
        ttsBridgeMap.put(SpeechBehaviour.class, new BMLTTSBridge(ipaacaTtsGenerator));
        ttsBridgeMap.put(SSMLBehaviour.class, new IpaacaSSMLTTSBridge(ipaacaTtsGenerator));
        
        supportedBehaviours.add(SSMLBehaviour.class);
    }
    
    @Override
    public void cleanup()
    {
        ipaacaTtsGenerator.close();
    }
}
