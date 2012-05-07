package asap.speechengine;

import asap.speechengine.ttsbinding.TTSBinding;
import hmi.bml.core.Behaviour;
import hmi.elckerlyc.pegboard.BMLBlockPeg;

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
