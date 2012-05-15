package asap.realizer.lipsync;

import saiba.bml.core.Behaviour;
import hmi.tts.Visime;

import java.util.List;

import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;

/**
 * Adds a Visime to e.g. a plan
 * @author Herwin
 */
public interface LipSynchProvider
{
    void addLipSyncMovement(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit speechUnit, List<Visime> visemes);
}
