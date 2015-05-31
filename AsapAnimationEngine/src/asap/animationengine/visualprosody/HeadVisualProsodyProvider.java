package asap.animationengine.visualprosody;

import hmi.animation.ConfigList;
import hmi.animation.Hanim;
import hmi.animation.SkeletonInterpolator;
import hmi.math.Quat4f;
import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.keyframe.KeyframeMU;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.visualprosody.VisualProsodyProvider;
import asap.visualprosody.AudioFeatures;
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
    private final PegBoard pegBoard;

    public HeadVisualProsodyProvider(VisualProsody v, AnimationPlayer ap, PlanManager<TimedAnimationUnit> animationPlanManager, PegBoard pb)
    {
        this.visualProsody = v;
        this.animationPlayer = ap;
        this.animationPlanManager = animationPlanManager;
        this.pegBoard = pb;
    }

    @Override
    public void visualProsody(BMLBlockPeg bbPeg, Behaviour beh, TimedPlanUnit speechUnit, double[] f0, double[] rmsEnergy,
            double frameDuration)
    {
        double rpyStart[] = new double[] { 0, 0, 0 };
        SkeletonInterpolator headSki = visualProsody.headMotion(rpyStart, new AudioFeatures(f0, rmsEnergy, frameDuration));
        KeyframeMU mu = new KeyframeMU(headSki);
        mu.setAdditive(true);
        mu = mu.copy(animationPlayer);
        TimedAnimationMotionUnit tmu = mu.createTMU(NullFeedbackManager.getInstance(), bbPeg, beh.getBmlId(), beh.id, pegBoard);
        tmu.resolveStartAndEndKeyPositions();
        tmu.setSubUnit(true);
        animationPlanManager.addPlanUnit(tmu);
        tmu.setTimePeg("start", speechUnit.getTimePeg("start"));

        ConfigList cl = new ConfigList(4);
        cl.addConfig(0, headSki.getConfig(headSki.size() - 1));
        cl.addConfig(0.3, Quat4f.getIdentity());
        SkeletonInterpolator relaxSki = new SkeletonInterpolator(new String[] { Hanim.skullbase }, cl, "R");
        KeyframeMU relaxMu = new KeyframeMU(relaxSki);
        relaxMu.setAdditive(true);
        relaxMu = relaxMu.copy(animationPlayer);
        TimedAnimationMotionUnit tmuRelax = relaxMu.createTMU(NullFeedbackManager.getInstance(), bbPeg, beh.getBmlId(), beh.id, pegBoard);
        tmuRelax.resolveStartAndEndKeyPositions();
        tmuRelax.setSubUnit(true);
        animationPlanManager.addPlanUnit(tmuRelax);
        tmuRelax.setTimePeg("start", speechUnit.getTimePeg("end"));
    }
}
