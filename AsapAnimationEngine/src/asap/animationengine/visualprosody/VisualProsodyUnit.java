package asap.animationengine.visualprosody;

import hmi.animation.ConfigList;
import hmi.animation.Hanim;
import hmi.animation.SkeletonInterpolator;
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
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.visualprosody.AudioFeatures;
import asap.visualprosody.VisualProsody;

import com.google.common.collect.ImmutableSet;

/**
 * Manages the incremental construction of visual prosody elements
 * @author hvanwelbergen
 *
 */
public class VisualProsodyUnit extends TimedAbstractPlanUnit implements TimedAnimationUnit
{
    private static final double LOOKAHEAD = 0.1d;
    private static final double RELAX_DURATION = 0.3;
    private final double f0[];
    private final double rmsEnergy[];
    private final double frameDuration;
    private int currentFrame = 0;
    private int lastFrame;
    private SkeletonInterpolator headSki;
    private VisualProsody visualProsody;
    private final AnimationPlayer animationPlayer;
    private final PlanManager<TimedAnimationUnit> animationPlanManager;
    private final TimePeg speechStart, speechEnd;
    private boolean relaxSetup = false;
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

    private int getNextFrameBoundary()
    {
        int frameLength = (int) (LOOKAHEAD / frameDuration);
        int nextFrame = currentFrame + frameLength;
        if (nextFrame >= f0.length) nextFrame = f0.length - 1;
        return nextFrame;
    }

    private SkeletonInterpolator getFirstHeadMotion()
    {
        lastFrame = getNextFrameBoundary();
        return visualProsody.headMotion(new double[] { 0, 0, 0 },
                new AudioFeatures(Arrays.copyOfRange(f0, currentFrame, lastFrame), Arrays.copyOfRange(rmsEnergy, currentFrame, lastFrame),
                        frameDuration));
    }

    private SkeletonInterpolator getNextHeadMotion()
    {
        lastFrame = getNextFrameBoundary();
        if (lastFrame > currentFrame)
        {
            return visualProsody.nextHeadMotion(new AudioFeatures(Arrays.copyOfRange(f0, currentFrame, lastFrame), Arrays.copyOfRange(
                    rmsEnergy, currentFrame, lastFrame), frameDuration));
        }
        return null;
    }

    @Override
    public void startUnit(double time)
    {
        headSki = getFirstHeadMotion();
        currentFrame = lastFrame;
        KeyframeMU mu = new KeyframeMU(headSki);
        mu.setAdditive(true);

        mu = mu.copy(animationPlayer);
        TimedAnimationMotionUnit tmu = mu.createTMU(NullFeedbackManager.getInstance(), getBMLBlockPeg(), getBMLId(), getId(), pegBoard);
        tmu.resolveStartAndEndKeyPositions();
        tmu.setSubUnit(true);
        tmu.setTimePeg("start", speechStart);
        animationPlanManager.addPlanUnit(tmu);
    }

    @Override
    public void playUnit(double time)
    {
        SkeletonInterpolator ski = getNextHeadMotion();
        if (ski != null)
        {
            headSki.appendInterpolator(lastFrame * frameDuration, ski);
            lastFrame = currentFrame;
        }
        else if (!relaxSetup)
        {
            ConfigList cl = new ConfigList(4);
            cl.addConfig(0, headSki.getConfig(headSki.size() - 1));
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
            relaxSetup = true;
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

    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return ImmutableSet.of(Hanim.skullbase);
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
