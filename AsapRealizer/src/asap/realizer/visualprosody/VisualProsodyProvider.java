package asap.realizer.visualprosody;

import saiba.bml.core.Behaviour;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;

/**
 * Provides visual prosody movement for the provided f0, rmsEnergy arrays, in which each lasts frameDuration seconds
 * @author hvanwelbergen
 */
public interface VisualProsodyProvider
{

    void visualProsody(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit speechUnit, double f0[], double rmsEnergy[], double frameDuration,
            float amount, float k);

    default void visualProsody(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit speechUnit, double f0[], double rmsEnergy[],
            double frameDuration, float amount)
    {
        visualProsody(bbPeg, beh, speechUnit, f0, rmsEnergy, frameDuration, amount, 1f);
    }
    
    default void visualProsody(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit speechUnit, double f0[], double rmsEnergy[],
            double frameDuration)
    {
        visualProsody(bbPeg, beh, speechUnit, f0, rmsEnergy, frameDuration, 1f, 1f);
    }
}
