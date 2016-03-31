/*******************************************************************************
 *******************************************************************************/
package asap.realizer.lipsync;

import hmi.tts.TTSTiming;
import saiba.bml.core.Behaviour;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;

/**
 * Adds a Visime to e.g. a plan
 * @author Herwin
 */
public interface LipSynchProvider
{
    /**
     * Adds the lipsync movement for a full (speech) behaviour 
     */
    void addLipSyncMovement(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit speechUnit, TTSTiming timing);
}
