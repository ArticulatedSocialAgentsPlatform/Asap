package asap.animationengine.visualprosody;

import hmi.animation.ConfigList;
import hmi.animation.Hanim;
import hmi.animation.SkeletonInterpolator;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.Spine;
import hmi.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import lombok.Setter;
import saiba.bml.core.Behaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.keyframe.KeyframeMU;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedAbstractPlanUnit;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
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
    private final TimePeg speechStart, speechEnd;
    private boolean relaxSetup = false;
    private double[] rpy, rpyPrev, rpyPrevPrev;
    private VJoint additiveBody;
    private KeyframeMU relaxMu;
    private List<String> joints;
    private volatile boolean interrupted = false;
    private double relaxStart;

    @Setter
    private double k = 1;

    @Setter
    private double amplitude = 1;

    public VisualProsodyUnit(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, TimedPlanUnit speechUnit,
            VisualProsody vp, AnimationPlayer animationPlayer, double[] f0, double[] rmsEnergy, double frameDuration, TimePeg speechStart,
            TimePeg speechEnd)
    {
        super(bbf, bmlBlockPeg, bmlId, id, true);
        this.f0 = f0;
        this.rmsEnergy = rmsEnergy;
        this.frameDuration = frameDuration;
        this.visualProsody = vp;
        this.animationPlayer = animationPlayer;
        this.speechStart = speechStart;
        this.speechEnd = speechEnd;
        joints = new ArrayList<String>();
        for (String cJoint : Hanim.CERVICAL_JOINTS)
        {
            if (animationPlayer.getVNextPartBySid(cJoint) != null)
            {
                joints.add(cJoint);
            }
        }
    }

    @Override
    public void startUnit(double time) throws TimedPlanUnitPlayException
    {
        if (f0.length > 0)
        {
            rpy = visualProsody.firstHeadMotion(new double[] { 0, 0, 0 }, f0[0], rmsEnergy[0], frameDuration, frameDuration);
        }
        else
        {
            rpy = new double[3];
        }
        rpyPrev = Arrays.copyOf(rpy, 3);
        rpyPrevPrev = Arrays.copyOf(rpy, 3);
        additiveBody = animationPlayer.constructAdditiveBody(ImmutableSet.copyOf(joints));
        feedback("start", time);
        super.startUnit(time);
    }

    @Override
    public void playUnit(double time) throws TimedPlanUnitPlayException
    {
        if (f0.length == 0) return;

        float rpyAnimation[] = Vec3f.getVec3f();
        for (int i = 0; i < 3; i++)
        {
            rpyAnimation[i] = (float) ((rpy[i] - visualProsody.getOffset()[i]) * amplitude);
        }
        if (time > speechEnd.getGlobalValue() || interrupted)
        {
            if (!relaxSetup)
            {
                relaxStart = time;
                float q[] = new float[joints.size() * 4];
                Spine.setCervicalRotationRollPitchYawDegrees(q, rpyAnimation[0], rpyAnimation[1], rpyAnimation[2], joints.size());
                ConfigList cl = new ConfigList(4 * joints.size());
                cl.addConfig(0, q);
                float qIdentity[] = new float[joints.size() * 4];
                for (int i = 0; i < joints.size(); i++)
                {
                    Quat4f.setIdentity(qIdentity, i * 4);
                }
                cl.addConfig(RELAX_DURATION, qIdentity);
                SkeletonInterpolator relaxSki = new SkeletonInterpolator(joints.toArray(new String[joints.size()]), cl, "R");
                relaxMu = new KeyframeMU(relaxSki);
                relaxMu.setAdditive(true);
                try
                {
                    relaxMu = relaxMu.copy(animationPlayer);
                }
                catch (MUSetupException e)
                {
                    throw new TimedPlanUnitPlayException("", this, e);
                }
                relaxSetup = true;
            }
            relaxMu.play((time - relaxStart) / RELAX_DURATION);
        }
        else
        {
            int index = (int) (((time - getStartTime()) / (f0.length * frameDuration)) * (f0.length - 1));
            if (index >= f0.length) index = f0.length - 1;
            rpy = visualProsody.nextHeadMotion(rpyPrev, rpyPrevPrev, f0[index], rmsEnergy[index], animationPlayer.getStepTime(),
                    frameDuration);

            for (int i = 0; i < 3; i++)
            {
                rpy[i] = rpyPrev[i] + k * (rpy[i] - rpyPrev[i]);
            }

            for (int i = 0; i < 3; i++)
            {
                rpyAnimation[i] = (float) ((rpy[i] - visualProsody.getOffset()[i]) * amplitude);
            }
            rpyPrevPrev = rpyPrev;
            rpyPrev = rpy;
            float q[] = new float[joints.size() * 4];
            Spine.setCervicalRotationRollPitchYawDegrees(q, rpyAnimation[0], rpyAnimation[1], rpyAnimation[2], joints.size());
            int i = 0;
            for (String joint : joints)
            {
                additiveBody.getPart(joint).setRotation(q, i * 4);
                i++;
            }
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
    public void setParameterValue(String paramId, String value) throws ParameterException
    {
        if (StringUtil.isNumeric(value))
        {
            setFloatParameterValue(paramId, Float.parseFloat(value));
        }
    }

    @Override
    public void setFloatParameterValue(String paramId, float value) throws ParameterException
    {
        if (paramId.equals("visualprosodyAmplitude"))
        {
            amplitude = value;
        }
        else if (paramId.equals("visualprosodyK"))
        {
            k = value;
        }
    }

    @Override
    public float getFloatParameterValue(String paramId) throws ParameterException
    {
        if (paramId.equals("visualprosodyAmplitude"))
        {
            return (float) amplitude;
        }
        else if (paramId.equals("visualprosodyK"))
        {
            return (float) k;
        }
        return super.getFloatParameterValue(paramId);
    }

    @Override
    public String getParameterValue(String paramId) throws ParameterException
    {
        if (paramId.equals("visualprosodyAmplitude"))
        {
            return "" + amplitude;
        }
        else if (paramId.equals("visualprosodyK"))
        {
            return "" + k;
        }
        return super.getParameterValue(paramId);
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
        if (relaxMu != null)
        {
            relaxMu.cleanup();
        }
        feedback("end", time);
    }

    @Override
    public void interrupt(double time)
    {
        if (time <= speechEnd.getGlobalValue())
        {
            interrupted = true;
        }
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
