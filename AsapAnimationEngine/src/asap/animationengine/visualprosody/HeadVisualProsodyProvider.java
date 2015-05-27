package asap.animationengine.visualprosody;

import saiba.bml.core.Behaviour;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.visualprosody.VisualProsodyProvider;

/**
 * Generates head movement on the basis of speech prosody
 * @author hvanwelbergen
 */
public class HeadVisualProsodyProvider implements VisualProsodyProvider
{
    @Override
    public void visualProsody(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit speechUnit, double[] f0, double[] rmsEnergy,
            double frameDuration)
    {
        System.out.println("VisualProsody!");
    }    
}
