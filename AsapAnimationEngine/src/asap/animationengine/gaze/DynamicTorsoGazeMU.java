package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.Saccade;
import hmi.neurophysics.Torso;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUSetupException;
import asap.motionunit.MUPlayException;
import asap.timemanipulator.ErfManipulator;
import asap.timemanipulator.TimeManipulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Provides (up to) full torso gaze to moving targets.
 * 
 * Implementation inspired by:<br>
 * Helena Grillon and Daniel Thalmann, Simulating gaze attention behaviors for crowds (2009), in: Computer Animation and Virtual Worlds, 20 2-3(111-- 119)<br>
 * 
 * Additions to their work:<br>
 * Eyes reach the target first and then lock on to it, that is, they overshoot their end rotation and then move back while remaining locked on the target (as in
 * P. Radua, D. Tweed, and T. Vilis. Three-dimensional eye, head, and chest orientations after large gaze shifts and the
 * underlying neural strategies. Journal of Neurophysiology, 72(6):2840–2852, 1994.).
 * The eye max speed and speed profile is biologically motivated (using R. H. S. Carpenter. Movements of the Eyes. Pion Ltd, London, UK, second edition, 1988).
 * Eyes adhere to biologically motivated rotation limits; eye rotation is calculated using Listing's law
 * (using D. Tweed. Three-dimensional model of the human eye-head saccadic system.
 * Journal of Neurophysiology, 77(2):654–666, February 1997).
 * @author Herwin
 */
public class DynamicTorsoGazeMU extends GazeMU
{
    // SMAX/Smax(t) in Grillion

    private static final double TORSO_TIME_SCALE = 2; // 2x slower than neck
    private static final int FPS_THORACIC = 3; // used as multiplier for the tmp setup
    private static final int FPS_CERVICAL = 3; // used as multiplier for the tmp setup
    private TimeManipulator tmpThoracic;
    private TimeManipulator tmpCervical;
    
    private ImmutableList<VJoint> joints;
    private ImmutableList<VJoint> cervicalJoints;
    private ImmutableList<VJoint> thoracicJoints;

    private ImmutableSet<String> kinematicJoints;

    float qStart[];
    float qStartCombined[] = Quat4f.getQuat4f();

    public DynamicTorsoGazeMU()
    {
        setupKeyPositions();
    }

    private float[] getSpine(float q[])
    {
        float[] spineRots = new float[joints.size() * 4];
        float[] rpy = Vec3f.getVec3f();
        Quat4f.getRollPitchYaw(q, rpy);

        int n = joints.size();
        for (int i = 1; i <= joints.size(); i++)
        {
            float s = (float) Torso.getLinearIncrease(i, n);
            Quat4f.setFromRollPitchYaw(spineRots, (i - 1) * 4, s * rpy[0], s * rpy[1], s * rpy[2]);
        }
        return spineRots;
    }

    private void setSpine(float q[])
    {
        int i = 0;
        for (VJoint vj : joints)
        {
            vj.setRotation(q, i);
            i += 4;
        }
    }

    
    

    public void playEye(double t) throws MUPlayException
    {
        float qCurrRight[] = Quat4f.getQuat4f();
        float qCurrLeft[] = Quat4f.getQuat4f();
        lEyeCurr.getRotation(qCurrLeft);
        rEyeCurr.getRotation(qCurrRight);
        float qDesRight[] = Quat4f.getQuat4f();
        float qDesLeft[] = Quat4f.getQuat4f();
        setEndEyeRotation(lEye, qDesLeft);
        setEndEyeRotation(rEye, qDesRight);

        double dur = Math.max(Saccade.getSaccadeDuration(getAngle(qDesRight, qCurrRight)),
                Saccade.getSaccadeDuration(getAngle(qDesLeft, qCurrLeft)));

        if (t < RELATIVE_READY_TIME)
        {
            if (dur < player.getStepTime())
            {
                lEye.setRotation(qDesLeft);
                rEye.setRotation(qDesRight);
            }
            else
            {
                double delta = player.getStepTime() / dur;
                float q[] = Quat4f.getQuat4f();
                Quat4f.interpolate(q, qCurrLeft, qDesLeft, (float) delta);
                lEye.setRotation(q);
                Quat4f.interpolate(q, qCurrRight, qDesRight, (float) delta);
                rEye.setRotation(q);
            }
        }
        else if (t < RELATIVE_RELAX_TIME)
        {
            lEye.setRotation(qDesLeft);
            rEye.setRotation(qDesRight);
        }
    }

    private void setThoracic(float qStart[], float qSpine[], double tRel)
    {
        int i = 0;
        float q[] = Quat4f.getQuat4f();
        for (VJoint vj : thoracicJoints)
        {
            Quat4f.interpolate(q, 0, qStart, i, qSpine, i, (float) tmpThoracic.manip(tRel));
            vj.setRotation(q);
            i += 4;
        }
    }

    private void setCervical(float qStart[], float qSpine[], double tRel)
    {
        int i = thoracicJoints.size() * 4;
        float q[] = Quat4f.getQuat4f();
        for (VJoint vj : cervicalJoints)
        {
            Quat4f.interpolate(q, 0, qStart, i, qSpine, i, (float) tmpCervical.manip(tRel));
            vj.setRotation(q);
            i += 4;
        }
    }

    @Override
    public void play(double t) throws MUPlayException
    {
        setTarget();
        float qSpine[] = getSpine(qGaze);

        if (t < RELATIVE_READY_TIME)
        {
            double tRel = t / RELATIVE_READY_TIME;
            setThoracic(qStart, qSpine, tRel);
            if (t < RELATIVE_READY_TIME / TORSO_TIME_SCALE)
            {
                setCervical(qStart, qSpine, tRel / (RELATIVE_READY_TIME / TORSO_TIME_SCALE));
            }
            else
            {
                setCervical(qStart, qSpine, 1);
            }
        }
        else if (t > RELATIVE_RELAX_TIME)
        {
            relaxUnit.play((t - RELATIVE_RELAX_TIME) / (1 - RELATIVE_RELAX_TIME));
        }
        else
        {
            setSpine(qSpine);
        }
        playEye(t);
    }

    @Override
    public void setStartPose() throws MUPlayException
    {
        VJoint vjTop = joints.get(joints.size() - 1);
        VJoint vjRoot = joints.get(0);
        vjTop.getPathRotation(vjRoot, qStartCombined);

        qStart = new float[joints.size() * 4];
        int i = 0;
        for (VJoint vj : joints)
        {
            VJoint vjCur = player.getVCurr().getPart(vj.getSid());
            {
                vjCur.getRotation(qStart, i);
            }
            i += 4;
        }
        setTarget(target);
    }

    @Override
    protected void setTarget() throws MUPlayException
    {
        VJoint neck = joints.get(joints.size() - 1);
        woTarget.getTranslation2(localGaze, neck);

        // lgazeneck = gazepos - neck
        // lgazeeyes = gazepos - eye = gazepos - (neck+localeye) = gazepos-neck-localeye = lgazeneck - localeye
        float rOffset[] = Vec3f.getVec3f();
        float lOffset[] = Vec3f.getVec3f();
        rEye.getPathTranslation(neck, rOffset);
        lEye.getPathTranslation(neck, lOffset);
        Vec3f.scale(-0.5f, rOffset);
        Vec3f.scale(-0.5f, lOffset);
        Vec3f.add(localGaze, rOffset);
        Vec3f.add(localGaze, lOffset);
        Quat4f.transformVec3f(getOffsetRotation(), localGaze);
        setEndRotation(localGaze);
    }

    /**
     * Determine the angle to rotate from qS to qG
     */
    private double getAngle(float qG[], float qS[])
    {
        if (Quat4f.epsilonEquals(qG, 0, 0, 0, 0, 0.01f))
        {
            return Math.PI * 0.25;
        }
        float q[] = Quat4f.getQuat4f();
        Quat4f.set(q, qGaze);
        Quat4f.mulConjugateRight(q, qS);
        double angle = Quat4f.getAngle(q);
        if (angle < 0) angle = -angle;
        if (angle > Math.PI) angle -= Math.PI;
        return angle;
    }

    @Override
    public double getPreferedReadyDuration()
    {
        return TORSO_TIME_SCALE * TARGET_IMPORTANCE * getAngle(qGaze, qStartCombined) / NECK_VELOCITY;
    }

    @Override
    public void setDurations(double prepDur, double relaxDur)
    {
        preparationDuration = prepDur;
        tmpThoracic = new ErfManipulator((int) (FPS_THORACIC));
        tmpCervical = new ErfManipulator((int) (FPS_CERVICAL));
    }

    @Override
    public void setEndRotation(float[] gazeDir)
    {
        Quat4f.setFromVectors(qGaze, Vec3f.getVec3f(0, 0, 1), gazeDir);
    }

    @Override
    public DynamicTorsoGazeMU copy(AnimationPlayer p) throws MUSetupException
    {
        DynamicTorsoGazeMU copy = new DynamicTorsoGazeMU();
        copy.cervicalJoints = ImmutableList.copyOf(VJointUtils.gatherJoints(Hanim.CERVICAL_JOINTS, p.getVNext()));
        copy.thoracicJoints = ImmutableList.copyOf(VJointUtils.gatherJoints(Hanim.THORACIC_JOINTS, p.getVNext()));
        copy.joints = new ImmutableList.Builder<VJoint>().addAll(copy.thoracicJoints).addAll(copy.cervicalJoints).build();

        copy.kinematicJoints = new ImmutableSet.Builder<String>().addAll(VJointUtils.transformToSidList(copy.joints))
                .add(Hanim.r_eyeball_joint).add(Hanim.l_eyeball_joint).build();
        copy.offsetAngle = offsetAngle;
        copy.offsetDirection = offsetDirection;
        copy.influence = influence;
        copy.player = p;
        copy.woManager = p.getWoManager();
        copy.target = target;
        copy.lEye = p.getVNext().getPart(Hanim.l_eyeball_joint);
        copy.rEye = p.getVNext().getPart(Hanim.r_eyeball_joint);
        copy.lEyeCurr = p.getVCurr().getPart(Hanim.l_eyeball_joint);
        copy.rEyeCurr = p.getVCurr().getPart(Hanim.r_eyeball_joint);
        return copy;
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return kinematicJoints;
    }
}
