package hmi.elckerlyc.lipsync;

import saiba.bml.core.Behaviour;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.tts.Visime;

import java.util.List;

/**
 * Adds a Visime to e.g. a plan
 * @author Herwin
 */
public interface LipSynchProvider
{
    void addLipSyncMovement(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit speechUnit, List<Visime> visemes);
}
