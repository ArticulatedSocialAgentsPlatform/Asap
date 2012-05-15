package asap.speechengine;

import asap.realizer.pegboard.BMLBlockPeg;
import asap.speechengine.ttsbinding.TTSBinding;
import saiba.bml.core.Behaviour;

/**
 * Factory to create TimedTTSUnits
 * 
 * @author Herwin van Welbergen
 */
public interface TimedTTSUnitFactory
{
    TimedTTSUnit createTimedTTSUnit(BMLBlockPeg bbPeg, String text, String voiceId, String bmlId, String id, TTSBinding ttsBin,
            Class<? extends Behaviour> behClass);
}
