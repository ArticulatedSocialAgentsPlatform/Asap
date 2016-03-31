/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.Saccade;
import hmi.neurophysics.Spine;

import java.util.List;
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
 * Helena Grillon and Daniel Thalmann, Simulating gaze attention behaviors for crowds (2009), in:<br>
 * Computer Animation and Virtual Worlds, 20 2-3(111-- 119)<br>
 * 
 * Additions to their work:<br>
 * Eyes reach the target first and then lock on to it, that is, they overshoot their end rotation
 * and then move back while remaining locked on the target (as in
 * P. Radua, D. Tweed, and T. Vilis. Three-dimensional eye, head, and chest orientations after large gaze shifts and the
 * underlying neural strategies. Journal of Neurophysiology, 72(6):2840–2852, 1994.).
 * The eye max speed and speed profile is biologically motivated<br>
 * (using R. H. S. Carpenter. Movements of the Eyes. Pion Ltd, London, UK, second edition, 1988).
 * Eyes adhere to biologically motivated rotation limits; eye rotation is calculated using Listing's law
 * (using D. Tweed. Three-dimensional model of the human eye-head saccadic system.
 * Journal of Neurophysiology, 77(2):654–666, February 1997).
 * @author Herwin
 */
public class DynamicGazeMU extends AbstractGazeMU
{
    // SMAX/Smax(t) in Grillion

    private static final double TORSO_TIME_SCALE = 2; // 2x slower than neck
    private static final int FPS_THORACIC = 3; // used as multiplier for the tmp setup
    private static final int FPS_CERVICAL = 3; // used as multiplier for the tmp setup

    private static final double CENTRAL_FOVEAL_AREA = Math.toRadians(30);
    private static final double CERVICAL_ONLY = Math.toRadians(15);
    private static final double EYE_ONLY = Math.toRadians(15);

    private TimeManipulator tmpThoracic;
    private TimeManipulator tmpCervical;    
    
    private ImmutableList<VJoint> joints;
    private ImmutableList<VJoint> cervicalJoints;
    private ImmutableList<VJoint> thoracicJoints;

    private ImmutableSet<String> kinematicJoints;

    float qStart[];
    float qStartCombined[] = Quat4f.getQuat4f();

    @Override
    protected void setInfluence(GazeInfluence influence)
    {
        super.setInfluence(influence);
        if (player != null)
        {
            gatherJoints();
        }
    }

    public DynamicGazeMU()
    {
        tmpThoracic = new ErfManipulator((int) (FPS_THORACIC));
        tmpCervical = new ErfManipulator((int) (FPS_CERVICAL));
        setupKeyPositions();
    }

    private float[] getSpine(float q[])
    {
        float spineGaze[] = Quat4f.getQuat4f(qGaze);
        float[] spineRots = new float[joints.size() * 4];

        int i = 0;
        // if(Quat4f.getAngle(spineGaze)<EYE_ONLY)
        // {
        // for (VJoint vj : joints)
        // {
        // player.getVCurrPartBySid(vj.getSid()).getRotation(spineRots,i);
        // i+=4;
        // }
        // return spineRots;
        // }

        if (!isLocal)
        {
            float aa[] = Quat4f.getAxisAngle4fFromQuat4f(spineGaze);
            Quat4f.setFromAxisAngle4f(spineGaze, aa[0], aa[1], aa[2], aa[3] - (float) EYE_ONLY);
        }

        List<VJoint> jointsToSteer = joints;
        // if(Quat4f.getAngle(spineGaze)<CERVICAL_ONLY)
        // {
        // jointsToSteer = cervicalJoints;
        // for (VJoint vj : thoracicJoints)
        // {
        // player.getVCurrPartBySid(vj.getSid()).getRotation(spineRots,i);
        // i+=4;
        // }
        // }

        float[] rpy = Vec3f.getVec3f();
        Quat4f.getRollPitchYaw(spineGaze, rpy);

        int n = jointsToSteer.size();
        for (i = 1; i <= jointsToSteer.size(); i++)
        {
            float s = (float) Spine.getLinearIncrease(i, n);
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

    public void playEye(double t, float qDesRight[], float qDesLeft[]) throws MUPlayException
    {
        float qCurrRight[] = Quat4f.getQuat4f();
        float qCurrLeft[] = Quat4f.getQuat4f();
        lEyeCurr.getRotation(qCurrLeft);
        rEyeCurr.getRotation(qCurrRight);

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
        else// if (t < RELATIVE_RELAX_TIME)
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

        float qDesRight[] = Quat4f.getQuat4f();
        float qDesLeft[] = Quat4f.getQuat4f();
        setEndEyeRotation(lEyeCurr, qDesLeft);
        setEndEyeRotation(rEyeCurr, qDesRight);

        playSpine(t, qSpine);
        playEye(t, qDesLeft, qDesRight);
    }

    private void playSpine(double t, float[] qSpine) throws MUPlayException
    {
        if (t < RELATIVE_READY_TIME)
        {
            double tRel = t / RELATIVE_READY_TIME;
            if (influence == GazeInfluence.WAIST)
            {
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
            else
            {
                setCervical(qStart, qSpine, tRel);
            }
        }
        else
        {
            setSpine(qSpine);
        }
    }

    @Override
    public void setStartPose() throws MUPlayException
    {
        if(joints.size()>0)//setup spine+neck rotations
        {
            VJoint vjTop = joints.get(joints.size() - 1);
            VJoint vjRoot = joints.get(0);
            vjTop.getPathRotation(vjRoot, qStartCombined);
    
            qStart = new float[joints.size() * 4];
            int i = 0;
            for (VJoint vj : joints)
            {
                VJoint vjCur = player.getVCurrPartBySid(vj.getSid());
                {
                    vjCur.getRotation(qStart, i);
                }
                i += 4;
            }
        }
        if (woTarget == null && !isLocal)
        {
            setTarget(target);
        }
    }

    @Override
    public void setTarget()
    {
        if (!isLocal)
        {
            VJoint neck, currentNeck;
            if(joints.size()>0)
            {
                neck = joints.get(joints.size() - 1);            
                currentNeck = player.getVCurrPartBySid(neck.getSid());
            }
            else
            {
                neck = null;
                currentNeck = null;
            }
            woTarget.getTranslation2(localGaze, currentNeck);

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
        }
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
        switch (influence)
        {
        case WAIST:
            return TORSO_TIME_SCALE * TARGET_IMPORTANCE * getAngle(qGaze, qStartCombined) / NECK_VELOCITY;
        case NECK:
        case SHOULDER:
        default:
            return TARGET_IMPORTANCE * getAngle(qGaze, qStartCombined) / NECK_VELOCITY;
        }

    }

    @Override
    public void setDurations(double prepDur, double relaxDur)
    {
        preparationDuration = prepDur;
    }

    @Override
    public void setEndRotation(float[] gazeDir)
    {
        Quat4f.setFromVectors(qGaze, Vec3f.getVec3f(0, 0, 1), gazeDir);
    }

    private static final String[] NECK_JOINTS = new String[] { Hanim.vc6, Hanim.vc5, Hanim.vc4, Hanim.vc3, Hanim.vc2, Hanim.vc1,
            Hanim.skullbase };

    private void gatherJoints()
    {
        switch (influence)
        {
        case EYES:
            thoracicJoints = ImmutableList.of();
            cervicalJoints = ImmutableList.of();
            break;
        default:
        case NECK:
            cervicalJoints = ImmutableList.copyOf(VJointUtils.gatherJoints(NECK_JOINTS, player.getVNext()));
            thoracicJoints = ImmutableList.of();
            break;
        case SHOULDER:
            List<VJoint> cerv = VJointUtils.gatherJoints(Hanim.CERVICAL_JOINTS, player.getVNext());
            List<VJoint> shoulderPath = player.getVNext().getPath(player.getVNextPartBySid(Hanim.r_shoulder));
            while (!shoulderPath.contains(cerv.get(0)))
            {
                cerv.add(0, cerv.get(0).getParent());
            }
            cervicalJoints = ImmutableList.copyOf(cerv);
            thoracicJoints = ImmutableList.of();
            break;
        case WAIST:
            cervicalJoints = ImmutableList.copyOf(VJointUtils.gatherJoints(Hanim.CERVICAL_JOINTS, player.getVNext()));
            thoracicJoints = ImmutableList.copyOf(VJointUtils.gatherJoints(Hanim.THORACIC_JOINTS, player.getVNext()));
            break;
        }
        joints = new ImmutableList.Builder<VJoint>().addAll(thoracicJoints).addAll(cervicalJoints).build();

        kinematicJoints = new ImmutableSet.Builder<String>().addAll(VJointUtils.transformToSidList(joints)).add(Hanim.r_eyeball_joint)
                .add(Hanim.l_eyeball_joint).build();
    }

    public void setPlayer(AnimationPlayer p)
    {
        player = p;
        lEye = p.getVNextPartBySid(Hanim.l_eyeball_joint);
        rEye = p.getVNextPartBySid(Hanim.r_eyeball_joint);
        lEyeCurr = p.getVCurrPartBySid(Hanim.l_eyeball_joint);
        rEyeCurr = p.getVCurrPartBySid(Hanim.r_eyeball_joint);
        woManager = p.getWoManager();
    }

    @Override
    public DynamicGazeMU copy(AnimationPlayer p) throws MUSetupException
    {
        DynamicGazeMU copy = new DynamicGazeMU();
        copy.influence = influence;
        copy.offsetAngle = offsetAngle;
        copy.offsetDirection = offsetDirection;
        copy.target = target;
        copy.setPlayer(p);
        copy.gatherJoints();
        return copy;
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return kinematicJoints;
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {
        setStartPose();
        super.startUnit(t);
    }

    /**
     * Time to stay on target
     */
    @Override
    public double getPreferedStayDuration()
    {
        return 2;
    }
    
    @Override
    public Set<String> getAdditiveJoints()
    {
        return ImmutableSet.of();
    }
}
