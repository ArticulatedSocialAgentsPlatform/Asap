/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.pointing;

import hmi.animation.AnalyticalIKSolver;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.BiologicalSwivelCostsEvaluator;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectManager;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gesturebinding.GestureBinding;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.procanimation.IKBody;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.timemanipulator.SigmoidManipulator;
import asap.timemanipulator.TimeManipulator;

import com.google.common.collect.ImmutableSet;

/**
 * Timing: ready: gaze target reached relax: start to move back to rest pose
 * (for now 0 rotation of neck joints)
 * 
 * @author welberge TODO: Fitts' law based default timing
 *         TODO: Double-hand point(?)
 */
public class PointingMU implements AnimationUnit
{
    protected float qShoulder[];
    protected float qElbow[];
    protected float qShoulderStart[];
    protected float qElbowStart[];
    protected float qTemp[];
    protected float vecTemp[];
    protected float vecTemp2[];

    protected KeyPosition ready;
    protected KeyPosition relax;

    @Getter
    protected AnimationPlayer player;

    protected WorldObjectManager woManager;
    protected String target;
    protected TimeManipulator tmp;
    protected VJoint vjShoulder;
    protected VJoint vjElbow;
    protected VJoint vjWrist;
    protected VJoint vjFingerTip;
    protected String shoulderId;
    protected String elbowId;
    protected String wristId;
    protected String fingerTipId;
    protected AnalyticalIKSolver solver;
    protected String hand = "RIGHT_HAND";
    protected WorldObject woTarget;
    protected double preparationDuration;
    private BiologicalSwivelCostsEvaluator autoSwivel;
    protected IKBody ikBodyCurrent;

    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    public List<KeyPosition> getKeyPositions()
    {
        return keyPositionManager.getKeyPositions();
    }

    public void setKeyPositions(List<KeyPosition> p)
    {
        keyPositionManager.setKeyPositions(p);
    }

    public KeyPosition getKeyPosition(String id)
    {
        return keyPositionManager.getKeyPosition(id);
    }

    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

    public PointingMU()
    {
        super();
        shoulderId = Hanim.r_shoulder;
        elbowId = Hanim.r_elbow;
        wristId = Hanim.r_wrist;
        fingerTipId = Hanim.r_index3;

        qShoulder = new float[4];
        qElbow = new float[4];
        qShoulderStart = new float[4];
        qElbowStart = new float[4];
        qTemp = new float[4];
        vecTemp = new float[3];
        vecTemp2 = new float[3];
        // vecTemp3 = new float[3];
        ready = new KeyPosition("ready", 0.25, 1);
        relax = new KeyPosition("relax", 0.75, 1);
        addKeyPosition(ready);
        addKeyPosition(relax);
        target = "";

        // defaults from presenter
        tmp = new SigmoidManipulator(5, 1);

    }

    public PointingMU(VJoint shoulder, VJoint elbow, VJoint wrist, VJoint fingerTip)
    {
        this();
        vjShoulder = shoulder;
        vjElbow = elbow;
        vjWrist = wrist;
        vjFingerTip = fingerTip;
        if (shoulder != null) shoulderId = shoulder.getSid();
        if (elbow != null) elbowId = elbow.getSid();
        if (wrist != null) wristId = wrist.getSid();
        if (fingerTip != null) fingerTipId = fingerTip.getSid();
        setupSolver();
    }

    private void setupSolver()
    {
        float tv[] = new float[3];
        float sv[] = new float[3];
        vjElbow.getPathTranslation(vjShoulder, tv);
        vjWrist.getPathTranslation(vjElbow, sv);
        solver = new AnalyticalIKSolver(sv, tv, AnalyticalIKSolver.LimbPosition.ARM, (Vec3f.length(sv) + Vec3f.length(tv)) * 0.999f);
        if (shoulderId.equals(Hanim.r_shoulder))
        {
            autoSwivel = GestureBinding.constructAutoSwivel("right_arm");
        }
        else
        {
            autoSwivel = GestureBinding.constructAutoSwivel("left_arm");
        }
    }

    @Override
    public PointingMU copy(AnimationPlayer p)
    {
        PointingMU pmu = new PointingMU();
        pmu.shoulderId = shoulderId;
        pmu.elbowId = elbowId;
        pmu.vjShoulder = p.getVNextPartBySid(shoulderId);
        pmu.vjShoulder = p.getVNextPartBySid(elbowId);
        pmu.vjWrist = p.getVNextPartBySid(wristId);
        pmu.player = p;
        pmu.ikBodyCurrent = new IKBody(p.getVCurr());
        pmu.setHand(hand);
        pmu.woManager = p.getWoManager();
        pmu.target = target;
        return pmu;
    }

    public void setTarget(String target) throws MUPlayException
    {
        if (woManager == null)
        {
            throw new MUPlayException("Pointing target not found, no WorldObjectManager set up.", this);
        }
        woTarget = woManager.getWorldObject(target);
        if (woTarget == null)
        {
            throw new MUPlayException("Pointing target not found", this);
        }
        vjFingerTip.getPathTranslation(null, vecTemp2);
        vjWrist.getPathTranslation(null, vecTemp);
        Vec3f.sub(vecTemp2, vecTemp);
        woTarget.getTranslation(vecTemp, null);
        AnalyticalIKSolver.translateToLocalSystem(null, vjShoulder, vecTemp, vecTemp2);
        setEndRotation(vecTemp2);
    }

    public void setStartPose(double prep) throws MUPlayException
    {
        preparationDuration = prep;
        player.getVCurrPartBySid(shoulderId).getRotation(qShoulderStart);
        player.getVCurrPartBySid(elbowId).getRotation(qElbowStart);
        setTarget(target);
    }

    /**
     * @param gazeDir
     *            gaze direction
     */
    public void setEndRotation(float[] vecPos)
    {
        double prevSwivel;
        if (shoulderId.equals(Hanim.l_shoulder))
        {
            prevSwivel = ikBodyCurrent.getSwivelLeftArm();
        }
        else
        {
            prevSwivel = ikBodyCurrent.getSwivelRightArm();
        }
        solver.setSwivel(autoSwivel.getSwivelAngleWithMinCost(prevSwivel));
        solver.solve(vecPos, qShoulder, qElbow);
    }

    @Override
    public double getPreferedDuration()
    {
        return 0;
    }

    public void setHand(String hand)
    {
        this.hand = hand;
        if (hand.equals("LEFT_HAND"))
        {
            shoulderId = Hanim.l_shoulder;
            elbowId = Hanim.l_elbow;
            wristId = Hanim.l_wrist;// Hanim.l_index2;//Hanim.l_index3;//
            fingerTipId = Hanim.l_index3;
        }
        else if (hand.equals("RIGHT_HAND"))
        {
            shoulderId = Hanim.r_shoulder;
            elbowId = Hanim.r_elbow;
            wristId = Hanim.r_wrist;// Hanim.r_index2; // //Hanim.r_index3;
            fingerTipId = Hanim.r_index3;
        }
        else if (hand.equals("UNSPECIFIED"))
        {
            // TODO: better selection strategy: for example from presenter: use
            // left hand to point to the left, right to point to the right
            // could also make use of available modalities (at run time?)
            shoulderId = Hanim.r_shoulder;
            elbowId = Hanim.r_elbow;
            wristId = Hanim.r_index3;// Hanim.r_wrist;
            fingerTipId = Hanim.r_index3;
        }
        else
        {
            throw new RuntimeException("Invalid hand spec " + hand);
        }
        vjShoulder = player.getVNextPartBySid(shoulderId);
        vjElbow = player.getVNextPartBySid(elbowId);
        vjWrist = player.getVNextPartBySid(wristId);
        vjFingerTip = player.getVNextPartBySid(fingerTipId);
        setupSolver();
    }

    @Override
    public void play(double t) throws MUPlayException
    {
        if (t < 0.25)
        {
            float tManip = (float) tmp.manip(t / 0.25);
            Quat4f.interpolate(qTemp, qShoulderStart, qShoulder, tManip);
            vjShoulder.setRotation(qTemp);
            Quat4f.interpolate(qTemp, qElbowStart, qElbow, tManip);
            vjElbow.setRotation(qTemp);
        }
        else
        {
            vjShoulder.setRotation(qShoulder);
            vjElbow.setRotation(qElbow);
        }
    }

    @Override
    public TimedAnimationMotionUnit createTMU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new PointingTMU(bfm, bbPeg, bmlId, id, this, pb, player);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterNotFoundException
    {
        if (name.equals("target"))
        {
            target = value;
        }
        else if (name.equals("hand"))
        {
            setHand(value);
        }
        else throw new ParameterNotFoundException(name);
    }

    @Override
    public String getParameterValue(String name) throws ParameterNotFoundException
    {
        if (name.equals("target")) return target;
        else if (name.equals("hand")) return hand;
        throw new ParameterNotFoundException(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterNotFoundException
    {
        throw new ParameterNotFoundException(name);
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterNotFoundException
    {
        throw new ParameterNotFoundException(name);
    }

    /**
     * Set the time manimpulator that describes the velocity profile for attack and decay. Default is SigmoidManipulator(3,4)
     */
    public void setTimeManipulator(TimeManipulator tmp)
    {
        this.tmp = tmp;
    }

    private static final Set<String> PHJOINTS = ImmutableSet.of();

    @Override
    public Set<String> getPhysicalJoints()
    {
        return PHJOINTS;
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return ImmutableSet.of(vjShoulder.getSid(), vjElbow.getSid());
    }

    public double getRelaxDuration()
    {
        return player.getRestPose().getTransitionToRestDuration(player.getVCurr(), getKinematicJoints());
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {

    }
    
    @Override
    public Set<String> getAdditiveJoints()
    {
        return ImmutableSet.of();
    }
}
