package asap.animationengine.visualprosody;

import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.visualprosody.VisualProsodyProvider;
import asap.visualprosody.VisualProsody;

/**
 * Generates head movement on the basis of speech prosody
 * @author hvanwelbergen
 */
public class HeadVisualProsodyProvider implements VisualProsodyProvider
{
    private final VisualProsody visualProsody;
    private final AnimationPlayer animationPlayer;
    private final PlanManager<TimedAnimationUnit> animationPlanManager;
    
    public HeadVisualProsodyProvider(VisualProsody v, AnimationPlayer ap, PlanManager<TimedAnimationUnit> animationPlanManager)
    {
        this.visualProsody = v;
        this.animationPlayer = ap;
        this.animationPlanManager = animationPlanManager;        
    }

    @Override
    public void visualProsody(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit speechUnit, double[] f0, double[] rmsEnergy,
            double frameDuration, float amplitude, float k)
    {
        System.out.println("scheduling visual prosody");
        long time = System.currentTimeMillis();

        VisualProsodyUnit tmu = new VisualProsodyUnit(NullFeedbackManager.getInstance(), bbPeg, beh.getBmlId(), beh.id, 
                speechUnit, visualProsody, animationPlayer, f0, rmsEnergy, frameDuration,
                speechUnit.getTimePeg("start"), speechUnit.getTimePeg("end"));
        tmu.setAmplitude(amplitude);
        tmu.setK(k);
        tmu.setSubUnit(true);
        tmu.setTimePeg("start", speechUnit.getTimePeg("start"));
        animationPlanManager.addPlanUnit(tmu);
        System.out.println("Scheduling visual prosody took " + (System.currentTimeMillis() - time) + " ms.");
        System.out.println("Speech duration: " + ((int) (speechUnit.getPreferedDuration() * 1000)) + " ms.");
    }
}
