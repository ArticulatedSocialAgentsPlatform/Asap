package asap.animationengine.visualprosody;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.keyframe.KeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
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
@Slf4j
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
        double rpyStart[] = new double[]{0,0,0};
        AnimationUnit mu = new KeyframeMU(visualProsody.headMotion(rpyStart,new AudioFeatures(f0,rmsEnergy,frameDuration)));
        try
        {
            mu = mu.copy(animationPlayer);
        }
        catch (MUSetupException e)
        {
            log.warn("Exception planning visual prosody timedmotionunit for speechbehavior {}", e, beh);
        }
        TimedAnimationMotionUnit tmu = mu.createTMU(NullFeedbackManager.getInstance(),bbPeg,beh.getBmlId(),beh.id,pegBoard);
        tmu.resolveStartAndEndKeyPositions();
        tmu.setSubUnit(true);
        animationPlanManager.addPlanUnit(tmu);
        tmu.setTimePeg("start", speechUnit.getTimePeg("start"));        
    }    
}
