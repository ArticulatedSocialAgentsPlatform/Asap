package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.math.Vec4f;
import hmi.neurophysics.EyeSaturation;
import hmi.neurophysics.ListingsLaw;
import hmi.neurophysics.Saccade;
import hmi.neurophysics.Torso;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUSetupException;
import asap.motionunit.MUPlayException;
import asap.timemanipulator.ErfManipulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@Slf4j
public class DynamicTorsoGazeMU extends GazeMU
{
    // SMAX/Smax(t) in Grillion

    private static final double TORSO_TIME_SCALE = 2; // 2x slower than neck
    private static final int FPS = 4; // used as multiplier for the tmp setup

    private ImmutableList<VJoint> joints;
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

    // copy-paste from DynamicEyeGazeMU
    private void setEndEyeRotation(float[] gazeDir, VJoint eye, float qEye[]) throws MUPlayException
    {
        woTarget = woManager.getWorldObject(target);
        if (woTarget == null)
        {
            throw new MUPlayException("Gaze target not found", this);
        }
        woTarget.getTranslation2(gazeDir, eye);
        Quat4f.transformVec3f(getOffsetRotation(), gazeDir);
        Vec3f.normalize(gazeDir);
        float q[] = Quat4f.getQuat4f();
        ListingsLaw.listingsEye(gazeDir, q);
        EyeSaturation.sat(q, Quat4f.getIdentity(), qEye);
    }

    public void playEye(double t) throws MUPlayException
    {
        VJoint neck = joints.get(joints.size() - 1);
        float targetPosCurr[] = Vec3f.getVec3f();
        woTarget.getTranslation2(targetPosCurr, neck);
        Quat4f.transformVec3f(getOffsetRotation(), targetPosCurr);

        float qCurrRight[] = Quat4f.getQuat4f();
        float qCurrLeft[] = Quat4f.getQuat4f();
        lEyeCurr.getRotation(qCurrLeft);
        rEyeCurr.getRotation(qCurrRight);
        float qDesRight[] = Quat4f.getQuat4f();
        float qDesLeft[] = Quat4f.getQuat4f();
        setEndEyeRotation(targetPosCurr, lEye, qDesLeft);
        setEndEyeRotation(targetPosCurr, rEye, qDesRight);

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
                Quat4f.interpolate(q, qCurrLeft, qDesLeft, (float)delta);
                lEye.setRotation(q);
                Quat4f.interpolate(q, qCurrRight, qDesRight, (float)delta);
                rEye.setRotation(q);
            }
        }
        else if (t < RELATIVE_RELAX_TIME)
        {
            lEye.setRotation(qDesLeft);
            rEye.setRotation(qDesRight);
        }
    }

    @Override
    public void play(double t) throws MUPlayException
    {
        setTarget();
        float qSpine[] = getSpine(qGaze);
        float q[] = new float[joints.size() * 4];
        if (t < RELATIVE_READY_TIME)
        {
            double tRel = t / RELATIVE_READY_TIME;
            Quat4f.interpolateArrays(q, qStart, qSpine, (float) tmp.manip(tRel));
            setSpine(q);
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
        woTarget.getTranslation2(localGaze, joints.get(joints.size() - 1));
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
        tmp = new ErfManipulator((int) (prepDur * FPS));
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
        List<VJoint> joints = new ArrayList<VJoint>(VJointUtils.gatherJoints(Hanim.THORACIC_JOINTS, p.getVNext()));
        joints.addAll(VJointUtils.gatherJoints(Hanim.CERVICAL_JOINTS, p.getVNext()));
        copy.joints = ImmutableList.copyOf(joints);
        copy.kinematicJoints = new ImmutableSet.Builder<String>().addAll(VJointUtils.transformToSidList(joints)).add(Hanim.r_eyeball_joint)
                .add(Hanim.l_eyeball_joint).build();
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
