package asap.tts.ipaaca;

import java.io.IOException;

import hmi.tts.Prosody;

/**
 * Analyzes the prosody in wav file fileName
 * @author hvanwelbergen
 *
 */
public interface VisualProsodyAnalyzer
{
    Prosody analyze(String fileName) throws IOException;
}
