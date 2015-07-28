/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace.lmp;

import hmi.animation.Hanim;
import hmi.animation.SkeletonPose;
import hmi.animation.VJoint;
import hmi.math.Quat4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.PostureConstraint;
import asap.math.LinearQuatInterpolator;
import asap.motionunit.MUPlayException;
import asap.motionunit.keyframe.Interpolator;
import asap.motionunit.keyframe.KeyFrame;
import asap.motionunit.keyframe.KeyFrameMotionUnit;
import asap.motionunit.keyframe.LinearQuatFloatInterpolator;
import asap.motionunit.keyframe.QuatFloatInterpolator;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;

import com.google.common.collect.ImmutableSet;

/**
 * Hand movement
 * @author hvanwelbergen
 * 
 */
// TODO: lots of duplicate code with LMPWristRot, factor this out
public class LMPHandMove extends LMP
{
    private final AnimationPlayer aniPlayer;
    private final ImmutableSet<String> kinematicJoints;
    private List<PostureConstraint> pcVec;
    private Map<PostureConstraint, TimePeg> constraintMap = new HashMap<>();
    private static final double TRANSITION_TIME = 0.5;
    private static final double DEFAULT_STROKEPHASE_DURATION = 0;
    private HandKeyFrameUnit mu;
    private QuatFloatInterpolator<LinearQuatInterpolator> interp;
    private final boolean isStatic;

    private class HandKeyFrameUnit extends KeyFrameMotionUnit
    {
        public HandKeyFrameUnit(Interpolator interp)
        {
            super(interp);
        }

        @Override
        public void startUnit(double t) throws MUPlayException
        {

        }

        public void setupDynamicStart(List<KeyFrame> keyFrames)
        {
            unifyKeyFrames(keyFrames);
            super.setupDynamicStart(keyFrames);
        }

        @Override
        public double getPreferedDuration()
        {
            return getStrokeDuration() + TRANSITION_TIME * 2;
        }

        @Override
        public void applyKeyFrame(KeyFrame kf)
        {
            int i = 0;
            for (String target : getKinematicJoints())
            {
                aniPlayer.getVNextPartBySid(target).setRotation(kf.getDofs(), i * 4);
                i++;
            }
        }

        @Override
        public KeyFrame getStartKeyFrame()
        {
            float q[] = new float[getKinematicJoints().size() * 4];
            int i = 0;
            for (String target : getKinematicJoints())
            {
                VJoint vj = aniPlayer.getVCurrPartBySid(target);
                vj.getRotation(q, i);
                i += 4;
            }
            return new KeyFrame(0, q);
        }

    }

    public LMPHandMove(String scope, List<PostureConstraint> pcVec, FeedbackManager fbm, BMLBlockPeg bmlPeg, String bmlId, String behId,
            PegBoard pegBoard, AnimationPlayer aniPlayer, boolean isStatic)
    {
        super(fbm, bmlPeg, bmlId, behId, pegBoard);
        this.aniPlayer = aniPlayer;
        this.pcVec = pcVec;
        this.isStatic = isStatic;
        interp = new LinearQuatFloatInterpolator();
        mu = new HandKeyFrameUnit(interp);

        if (scope == null)
        {
            scope = "right_arm";
        }

        Set<String> jointIds = new HashSet<>();
        if (scope.equals("left_arm"))
        {
            jointIds.addAll(ImmutableSet.copyOf(Hanim.LEFTHAND_JOINTS));
            for (PostureConstraint pc : pcVec)
            {
                pc.getPosture().mirror(null);
            }
        }
        else
        {
            jointIds.addAll(ImmutableSet.copyOf(Hanim.RIGHTHAND_JOINTS));
        }

        Set<String> removeIds = new HashSet<>();
        for (String id : jointIds)
        {
            if (aniPlayer.getVCurrPartBySid(id) == null)
            {
                removeIds.add(id);
            }
        }
        jointIds.removeAll(removeIds);
        kinematicJoints = ImmutableSet.copyOf(jointIds);
        createPCTimePegs();
    }

    private void constructKeyFrames(double startTime)
    {
        List<KeyFrame> frames = new ArrayList<>();
        for (PostureConstraint oc : pcVec)
        {
            double time = constraintMap.get(oc).getGlobalValue() - startTime;
            SkeletonPose sp = oc.getPosture();
            float config[] = new float[getKinematicJoints().size() * 4];
            int j = 0;
            for (String jointId : getKinematicJoints())
            {
                Quat4f.setIdentity(config, j);
                for (int i = 0; i < sp.getPartIds().length; i++)
                {
                    if (sp.getPartIds()[i].equals(jointId))
                    {
                        Quat4f.set(config, j, sp.getConfig(), i * 4);
                    }
                }
                j += 4;
            }
            KeyFrame kf = new KeyFrame(time, config);
            frames.add(kf);
        }
        mu.setupDynamicStart(frames);
        interp.setKeyFrames(frames, 4 * getKinematicJoints().size());
    }

    @Override
    public List<String> getAvailableSyncs()
    {
        List<String> syncs = super.getAvailableSyncs();
        for (PostureConstraint oc : pcVec)
        {
            if (!syncs.contains(oc.getId()))
            {
                syncs.add(oc.getId());
            }
        }
        return syncs;
    }

    private void createPCTimePegs()
    {
        for (PostureConstraint oc : pcVec)
        {
            if (constraintMap.get(oc) == null)
            {
                TimePeg tp = new TimePeg(getBMLBlockPeg());
                constraintMap.put(oc, tp);
                pegBoard.addTimePeg(getBMLId(), getId(), oc.getId(), tp);
            }
        }
    }

    @Override
    protected void setInternalStrokeTiming(double earliestStart)
    {
        List<PostureConstraint> tpSet = new ArrayList<>();
        for (PostureConstraint oc : pcVec)
        {
            TimePeg tp = constraintMap.get(oc);
            if (tp != null)
            {
                if (tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                {
                    tpSet.add(oc);
                }
            }
        }

        // set inner
        for (int i = 0; i < tpSet.size() - 1; i++)
        {
            PostureConstraint ocLeft = tpSet.get(i);
            PostureConstraint ocRight = tpSet.get(i + 1);
            TimePeg tpLeft = constraintMap.get(ocLeft);
            TimePeg tpRight = constraintMap.get(ocRight);
            double avgDur = (tpRight.getGlobalValue() - tpLeft.getGlobalValue()) / (pcVec.indexOf(ocRight) - pcVec.indexOf(ocLeft));
            double time = tpLeft.getGlobalValue();
            for (int j = pcVec.indexOf(ocLeft) + 1; j < pcVec.indexOf(ocRight); j++)
            {
                time += avgDur;
                constraintMap.get(pcVec.get(j)).setGlobalValue(time);
            }
        }

        // find average duration to use for outer
        int i = 0;
        double totalDur = 0;
        int segments = 0;

        TimePeg tpPrev = null;
        for (PostureConstraint oc : pcVec)
        {
            i++;
            TimePeg tp = constraintMap.get(oc);
            if (tp != null)
            {
                if (tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                {
                    if (tpPrev != null)
                    {
                        segments++;
                        totalDur += (tp.getGlobalValue() - tpPrev.getGlobalValue()) / (double) i;
                        i = 0;
                    }
                    tpPrev = tp;
                }
            }
        }
        double avgDur = DEFAULT_STROKEPHASE_DURATION / (pcVec.size() - 1);
        if (segments > 0)
        {
            avgDur = totalDur / segments;
        }

        // set from right to end
        double time = constraintMap.get(tpSet.get(tpSet.size() - 1)).getGlobalValue();
        for (int j = pcVec.indexOf(tpSet.get(tpSet.size() - 1)) + 1; j < pcVec.size(); j++)
        {
            time += avgDur;
            constraintMap.get(pcVec.get(j)).setGlobalValue(time);
        }

        // set from left to start
        time = constraintMap.get(tpSet.get(0)).getGlobalValue();
        int nrOfSegments = pcVec.indexOf(tpSet.get(0));
        if (time - (nrOfSegments * avgDur) < TRANSITION_TIME + earliestStart)
        {
            avgDur = (time - TRANSITION_TIME) / nrOfSegments;
        }

        for (int j = pcVec.indexOf(tpSet.get(0)) - 1; j >= 0; j--)
        {
            time -= avgDur;
            constraintMap.get(pcVec.get(j)).setGlobalValue(time);
        }
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return kinematicJoints;
    }

    @Override
    public Set<String> getPhysicalJoints()
    {
        return ImmutableSet.of();
    }

    private PostureConstraint findOrientConstraint(String syncId)
    {
        for (PostureConstraint oc : pcVec)
        {
            if (oc.getId().equals(syncId))
            {
                return oc;
            }
        }
        return null;
    }

    @Override
    public void setTimePeg(String syncId, TimePeg peg)
    {
        if (findOrientConstraint(syncId) != null)
        {
            constraintMap.put(findOrientConstraint(syncId), peg);
        }
        super.setTimePeg(syncId, peg);
    }

    @Override
    public boolean hasValidTiming()
    {
        TimePeg tpPrev = null;
        for (PostureConstraint pc : pcVec)
        {
            TimePeg tp = constraintMap.get(pc);
            if (tp != null)
            {
                if (tp.getGlobalValue() != TimePeg.VALUE_UNKNOWN)
                {
                    if (tpPrev != null)
                    {
                        if (tpPrev.getGlobalValue() > tp.getGlobalValue()) return false;
                    }
                    tpPrev = tp;
                }
            }
        }
        return true;
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        double strokeEndTime = this.getTime("strokeEnd");
        if (time < strokeEndTime)
        {
            double t = (time - getStartTime()) / (strokeEndTime - getStartTime());
            try
            {
                mu.play(t);
            }
            catch (MUPlayException e)
            {
                throw new TimedPlanUnitPlayException("Keyframe playback failure ", this, e);
            }
        }
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        resolveTimePegs(time);

        constructKeyFrames(time);

        feedback("start", time);

        super.startUnit(time);
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {

    }

    @Override
    public double getPreparationDuration()
    {
        SkeletonPose sp = pcVec.get(0).getPosture();        
        float qStart[] = Quat4f.getQuat4f();
        int i= 0;
        for (String jointId : sp.getPartIds())
        {
            VJoint vj = aniPlayer.getVCurrPartBySid(jointId);
            vj.getRotation(qStart);
            if(!Quat4f.epsilonRotationEquivalent(sp.getConfig(),i,qStart,0, 0.01f))
            {
                return TRANSITION_TIME;
            }
            i+=4;
        }
        return 0;
    }

    @Override
    public double getRetractionDuration()
    {
        return TRANSITION_TIME;
    }

    @Override
    public double getStrokeDuration()
    {
        if (isStatic) return 0;
        double strokeDuration = 0;
        PostureConstraint ocPrev = null;
        for (PostureConstraint oc : pcVec)
        {
            if (ocPrev != null)
            {
                strokeDuration += TRANSITION_TIME;
            }
            ocPrev = oc;
        }
        return strokeDuration;
    }

}
