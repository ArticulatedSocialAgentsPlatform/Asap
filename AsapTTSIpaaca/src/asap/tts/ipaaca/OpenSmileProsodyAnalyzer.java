package asap.tts.ipaaca;

import hmi.tts.Prosody;

import java.io.IOException;

import asap.opensmile.OpenSmileWrapper;
import asap.opensmile.OpenSmileWrapper.AudioFeatures;

/**
 * Uses the OpenSmile wrapper to analyze the prosody of the speech audiofile
 * @author hvanwelbergen
 *
 */
public class OpenSmileProsodyAnalyzer implements VisualProsodyAnalyzer
{
    @Override
    public Prosody analyze(String fileName) throws IOException
    {
        AudioFeatures af = OpenSmileWrapper.analyzeProsody(fileName);
        return new Prosody(af.getF0(),af.getRmsEnergy(), af.getFrameDuration());
    }
}
