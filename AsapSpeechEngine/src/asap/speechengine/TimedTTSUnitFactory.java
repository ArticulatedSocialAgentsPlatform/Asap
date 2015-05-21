/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import saiba.bml.core.Behaviour;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.speechengine.ttsbinding.TTSBinding;

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
