/*******************************************************************************
 *******************************************************************************/
package asap.realizer.lipsync;

import hmi.tts.Visime;
import saiba.bml.core.Behaviour;
import asap.realizer.pegboard.BMLBlockPeg;

/**
 * Can be used to incrementally construct lipsync units, whose timing might change at a later stage.
 * @author hvanwelbergen
 *
 */
public interface IncrementalLipSynchProvider
{
    /**
     * Sets a single viseme, that is identified by identifier. Later calls to set with the same identifier should override the old viseme information.
     */
    void setLipSyncUnit(BMLBlockPeg bbPeg, Behaviour beh, double start, Visime viseme, Object identifier);    
}
