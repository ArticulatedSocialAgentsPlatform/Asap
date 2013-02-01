package asap.realizer.lipsync;

import saiba.bml.core.Behaviour;
import hmi.tts.Visime;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;

/**
 * Can be used to incrementally construct lipsync units, whose timing might change at a later stage.
 * @author hvanwelbergen
 *
 */
public interface IncrementalLipsyncProvider
{
    /**
     * Sets a single viseme, that is identified by identifier. Later calls to set with the same identifier should override the old viseme information.
     */
    void setLipSyncUnit(BMLBlockPeg bbPeg, Behaviour beh, double start, Visime viseme, Object identifier);
}
