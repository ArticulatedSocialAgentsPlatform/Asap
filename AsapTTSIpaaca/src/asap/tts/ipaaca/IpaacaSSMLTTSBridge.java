/*******************************************************************************
 *******************************************************************************/
package asap.tts.ipaaca;

import hmi.tts.TTSBridge;
import hmi.tts.TTSException;
import hmi.tts.TimingInfo;

import java.io.IOException;

/**
 * Binds the SSMLBehavior to the ipaaca ttsgenerator
 * @author hvanwelbergen
 * 
 */
public class IpaacaSSMLTTSBridge implements TTSBridge
{
    private final IpaacaTTSGenerator ttsGen;

    public IpaacaSSMLTTSBridge(IpaacaTTSGenerator ttsGen)
    {
        this.ttsGen = ttsGen;
    }

    @Override
    public TimingInfo getTiming(String text) throws TTSException
    {
        return ttsGen.getTiming(text);
    }

    @Override
    public TimingInfo speak(String text) throws TTSException
    {
        return ttsGen.speak(text);
    }

    @Override
    public TimingInfo speakToFile(String text, String filename) throws IOException, TTSException
    {
        return ttsGen.speakToFile(text, filename);
    }
}
