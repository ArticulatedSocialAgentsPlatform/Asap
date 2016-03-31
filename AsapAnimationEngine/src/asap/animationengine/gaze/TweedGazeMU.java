/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.DondersLaw;
import hmi.neurophysics.EyeSaturation;
import hmi.neurophysics.ListingsLaw;
import hmi.worldobjectenvironment.AbsolutePositionWorldObject;

import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUSetupException;
import asap.motionunit.MUPlayException;
import asap.realizer.pegboard.TimePeg;
import asap.timemanipulator.SigmoidManipulator;
import asap.timemanipulator.TimeManipulator;

import com.google.common.collect.ImmutableSet;

/**
 * Timing: ready: gaze target reached relax: start to move back to rest pose
 * 
 * @author welberge
 * 
 *         Theoretical background
 * 
 *         End pose of the head:<br>
 *         Tweed, D. (1997). Three-dimensional model of
 *         the human eye-head saccadic system. The Journal of Neurophysiology,
 *         77 (2), pp. 654-666.
 * 
 *         Fixed head rotation axis during gaze:<br>
 *         Tweed, D., and Vilis, T. (1992).
 *         Listing's Law for Gaze-Directing Head Movements. In A. Berthoz, W.
 *         Graf, and P. Vidal (Eds.), The Head-Neck Sensory-Motor System (pp.
 *         387-391). New York: Oxford University Press.
 */
public class TweedGazeMU extends AbstractGazeMU
{
    protected float qTemp[];
    protected float qStartLeftEye[], qStartRightEye[];
    private float qStart[];
    protected VJoint neck;   
    protected TimeManipulator tmp;    
    protected double relaxDuration = TimePeg.VALUE_UNKNOWN;
        
    
    public TweedGazeMU()
    {
        qTemp = new float[4];
        qStart = new float[4];
        qStartLeftEye = new float[4];
        qStartRightEye = new float[4];
        setupKeyPositions();

        // defaults from presenter
        tmp = new SigmoidManipulator(5, 1);
    }

    @Override
    public TweedGazeMU copy(AnimationPlayer p) throws MUSetupException
    {
        TweedGazeMU gmu = new TweedGazeMU();
        gmu.neck = p.getVNextPartBySid(Hanim.skullbase);
        gmu.lEye = p.getVNextPartBySid(Hanim.l_eyeball_joint);
        gmu.rEye = p.getVNextPartBySid(Hanim.r_eyeball_joint);
        gmu.lEyeCurr = p.getVCurrPartBySid(Hanim.l_eyeball_joint);
        gmu.rEyeCurr = p.getVCurrPartBySid(Hanim.r_eyeball_joint);
        gmu.offsetAngle = offsetAngle;
        gmu.offsetDirection = offsetDirection;
        gmu.player = p;
        gmu.woManager = p.getWoManager();
        gmu.target = target;
        return gmu;
    }
    
    protected void setTarget(float[] absTarget)
    {
        woTarget = new AbsolutePositionWorldObject(absTarget);
        setTarget();
    }

    @Override
    public void setStartPose() throws MUPlayException
    {

        player.getVCurrPartBySid(Hanim.skullbase).getRotation(qStart);
        if (lEyeCurr != null && rEyeCurr != null)
        {
            lEyeCurr.getRotation(qStartLeftEye);
            rEyeCurr.getRotation(qStartRightEye);
        }
        setTarget(target);
    }

    public void setDurations(double prepDur, double relaxDur)
    {
        preparationDuration = prepDur;
        relaxDuration = relaxDur;
    }

    /**
     * @param gazeDir
     *            gaze direction
     */
    public void setEndRotation(float[] gazeDir)
    {
        float normDir[] = new float[3];
        Vec3f.normalize(normDir, gazeDir);
        DondersLaw.dondersHead(normDir, qGaze);
    }

    /**
     * @param gazeDir
     *            gaze direction
     */
    void setEndRotation(float[] gazeDir, float[] q) throws MUPlayException
    {
        float normDir[] = new float[3];
        Vec3f.normalize(normDir, gazeDir);
        DondersLaw.dondersHead(normDir, q);
    }
    
    @Override
    public double getPreferedStayDuration()
    {
        return 2;
    }

    public double getPreferedReadyDuration()
    {
        float q[] = Quat4f.getQuat4f();
        Quat4f.set(q, qGaze);
        Quat4f.mulConjugateRight(q, qStart);
        return TARGET_IMPORTANCE * Quat4f.getAngle(q) / NECK_VELOCITY;

    }

    private void playEye(double t, float[] qDesNeck, float[] qStartEye, VJoint eye, VJoint eyeCurr) throws MUPlayException
    {

        float gazeDir[] = Vec3f.getVec3f();
        Vec3f.set(gazeDir, localGaze);
        Vec3f.normalize(gazeDir);

        float eyeRotationDes[] = Quat4f.getQuat4f(); // desired final eye
                                                     // rotation
        float qDesNeckConj[] = Quat4f.getQuat4f();
        Quat4f.conjugate(qDesNeckConj, qDesNeck);
        Quat4f.transformVec3f(qDesNeckConj, gazeDir);
        ListingsLaw.listingsEye(gazeDir, eyeRotationDes);

        float eyeRotationSpace[] = Quat4f.getQuat4f();
        Quat4f.mul(eyeRotationSpace, qDesNeck, eyeRotationDes);
        float eyeRotationDesCur[] = Quat4f.getQuat4f(); // desired eye rotation,
                                                        // given current head
                                                        // position
        float qNeck[] = Quat4f.getQuat4f();
        neck.getRotation(qNeck);
        Quat4f.conjugate(qNeck);
        Quat4f.mul(eyeRotationDesCur, qNeck, eyeRotationSpace);
        float eyeRotationSat[] = Quat4f.getQuat4f();

        if (!EyeSaturation.isSaturized(eyeRotationDes))
        {
            throw new MUPlayException("Eye gaze at target violates eye saturation constraints.", this);
        }
        EyeSaturation.sat(eyeRotationDesCur, eyeRotationDes, eyeRotationSat);

        float[] qCurr = Quat4f.getQuat4f();
        eyeCurr.getRotation(qCurr);

        if (t < RELATIVE_READY_TIME)
        {

            double remDuration = ((0.25 - t) / 0.25) * preparationDuration;
            float deltaT = (float) (player.getStepTime() / remDuration);
            Quat4f.interpolate(qTemp, qCurr, eyeRotationSat, deltaT);
            eye.setRotation(qTemp);
        }
        else if (t > RELATIVE_RELAX_TIME)
        {
            double remDuration = ((1 - t) / 0.25) * relaxDuration;
            float deltaT = (float) (player.getStepTime() / remDuration);
            Quat4f.interpolate(qTemp, qCurr, qStartEye, deltaT);
            eye.setRotation(qTemp);

            // logger.debug("Gaze relax at {}, relax duration {}",deltaT,relaxDuration);
        }
        else
        {
            // logger.debug("Gaze at target");
            eye.setRotation(eyeRotationSat);
        }
    }

    private void playEyes(double t) throws MUPlayException
    {
        if (rEye != null && lEye != null)
        {
            playEye(t, qGaze, qStartLeftEye, lEye, rEyeCurr);
            playEye(t, qGaze, qStartRightEye, rEye, rEyeCurr);
        }
    }

    @Override
    public void play(double t) throws MUPlayException
    {
        if (t < RELATIVE_READY_TIME)
        {
            float tManip = (float) tmp.manip(t / RELATIVE_READY_TIME);
            Quat4f.interpolate(qTemp, qStart, qGaze, tManip);
            neck.setRotation(qTemp);
            playEyes(t);
        }
        else
        {
            neck.setRotation(qGaze);
            playEyes(t);
        }
    }

    

    public void setTarget()
    {
        if(!isLocal)
        {
            woTarget.getTranslation2(localGaze, neck);
        }
        Quat4f.transformVec3f(getOffsetRotation(), localGaze);
        setEndRotation(localGaze);
    }

    /**
     * Set the time manipulator that describes the velocity profile for attack
     * and decay. Default is SigmoidManipulator(3,4)
     */
    public void setTimeManipulator(TimeManipulator tmp)
    {
        this.tmp = tmp;
    }

    
    private static final Set<String> KINJOINTSALL = ImmutableSet.of(Hanim.skullbase, Hanim.l_eyeball_joint, Hanim.r_eyeball_joint);
    private static final Set<String> KINJOINTSNECK = ImmutableSet.of(Hanim.skullbase);

    @Override
    public Set<String> getKinematicJoints()
    {
        if (lEye == null || rEye == null)
        {
            return KINJOINTSNECK;
        }
        else
        {
            return KINJOINTSALL;
        }
    }
    
    @Override
    public Set<String> getAdditiveJoints()
    {
        return ImmutableSet.of();
    }
}
