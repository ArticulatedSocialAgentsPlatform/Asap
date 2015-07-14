package asap.tts.ipaaca;

import hmi.tts.Prosody;

/**
 * Dummy implementation, always returns null
 * @author hvanwelbergen
 *
 */
public class NullProsodyAnalyzer implements VisualProsodyAnalyzer
{
    @Override
    public Prosody analyze(String fileName)
    {
        return null;
    }
}
