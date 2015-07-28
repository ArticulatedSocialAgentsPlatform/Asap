package asap.animationengine.visualprosody;

import hmi.animation.ConfigList;
import hmi.animation.Hanim;
import hmi.animation.SkeletonInterpolator;
import hmi.animation.VJoint;
import hmi.math.Quat4f;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.keyframe.KeyframeMU;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.visualprosody.VisualProsody;

import com.google.common.collect.ImmutableSet;

/**
 * Manages the incremental construction of visual prosody elements
 * @author hvanwelbergen
 *
 */
public class VisualProsodyUnit extends TimedAbstractPlanUnit implements TimedAnimationUnit
{
    private static final double RELAX_DURATION = 0.3;
    private final double f0[];
    private final double rmsEnergy[];
    private final double frameDuration;
    private VisualProsody visualProsody;
    private final AnimationPlayer animationPlayer;
    private final PlanManager<TimedAnimationUnit> animationPlanManager;
    private final TimePeg speechStart, speechEnd;
    private boolean relaxSetup = false;
    private double[] rpy, rpyPrev, rpyPrevPrev;
    private VJoint additiveBody;
    private final PegBoard pegBoard;

    public VisualProsodyUnit(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb, TimedPlanUnit speechUnit,
            VisualProsody vp, AnimationPlayer animationPlayer, PlanManager<TimedAnimationUnit> animationPlanManager, double[] f0,
            double[] rmsEnergy, double frameDuration, TimePeg speechStart, TimePeg speechEnd)
    {
        super(bbf, bmlBlockPeg, bmlId, id, true);
        this.pegBoard = pb;
        this.f0 = f0;
        this.rmsEnergy = rmsEnergy;
        this.frameDuration = frameDuration;
        this.visualProsody = vp;
        this.animationPlayer = animationPlayer;
        this.animationPlanManager = animationPlanManager;
        this.speechStart = speechStart;
        this.speechEnd = speechEnd;
    }

    @Override
    public void startUnit(double time)
    {
        rpy = visualProsody.firstHeadMotion(new double[] { 0, 0, 0 }, f0[0], rmsEnergy[0], frameDuration, frameDuration);
        rpyPrev = Arrays.copyOf(rpy, 3);
        rpyPrevPrev = Arrays.copyOf(rpy, 3);
        additiveBody = animationPlayer.constructAdditiveBody(ImmutableSet.of(Hanim.skullbase));
    }

    @Override
    public void playUnit(double time)
    {
        if (time >= speechEnd.getGlobalValue() && !relaxSetup)
        {
            float q[] = Quat4f.getQuat4fFromRollPitchYawDegrees((float) rpy[0] - visualProsody.getOffset()[0], (float) rpy[1]
                    - visualProsody.getOffset()[1], (float) rpy[2] - visualProsody.getOffset()[2]);
            ConfigList cl = new ConfigList(4);
            cl.addConfig(0, q);
            cl.addConfig(RELAX_DURATION, Quat4f.getIdentity());
            SkeletonInterpolator relaxSki = new SkeletonInterpolator(new String[] { Hanim.skullbase }, cl, "R");
            KeyframeMU relaxMu = new KeyframeMU(relaxSki);
            relaxMu.setAdditive(true);
            relaxMu = relaxMu.copy(animationPlayer);
            TimedAnimationMotionUnit tmuRelax = relaxMu.createTMU(NullFeedbackManager.getInstance(), getBMLBlockPeg(), getBMLId(), getId(),
                    pegBoard);
            tmuRelax.resolveStartAndEndKeyPositions();
            tmuRelax.setSubUnit(true);
            tmuRelax.setTimePeg("start", speechEnd);
            animationPlanManager.addPlanUnit(tmuRelax);
            tmuRelax.setState(TimedPlanUnitState.LURKING);
            additiveBody.getPart(Hanim.skullbase).setRotation(q);
            relaxSetup = true;
        }
        else
        {
            int index = (int) (((time - getStartTime()) / (f0.length * frameDuration)) * (f0.length - 1));
            if (index >= f0.length) index = f0.length-1;            
            rpy = visualProsody.nextHeadMotion(rpyPrev, rpyPrevPrev, f0[index], rmsEnergy[index], animationPlayer.getStepTime(),
                    frameDuration);
            rpyPrevPrev = rpyPrev;
            rpyPrev = rpy;            
            float q[] = Quat4f.getQuat4fFromRollPitchYawDegrees((float) rpy[0] - visualProsody.getOffset()[0], (float) rpy[1]
                    - visualProsody.getOffset()[1], (float) rpy[2] - visualProsody.getOffset()[2]);
            additiveBody.getPart(Hanim.skullbase).setRotation(q);
        }
    }

    @Override
    public double getStartTime()
    {
        return speechStart.getGlobalValue();
    }

    @Override
    public double getEndTime()
    {
        return speechEnd.getGlobalValue() + RELAX_DURATION;
    }

    @Override
    public double getRelaxTime()
    {
        return speechEnd.getGlobalValue();
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        return null;
    }

    @Override
    public TimePeg getTimePeg(String syncId)
    {
        return null;
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {

    }

    @Override
    public boolean hasValidTiming()
    {
        return true;
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        animationPlayer.removeAdditiveBody(additiveBody);
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return ImmutableSet.of();
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        return ImmutableSet.of();
    }

    @Override
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
    {

    }

    @Override
    public double getPreparationDuration()
    {
        return 0;
    }

    @Override
    public double getRetractionDuration()
    {
        return RELAX_DURATION;
    }

    @Override
    public double getStrokeDuration()
    {
        return 0;
    }
}
