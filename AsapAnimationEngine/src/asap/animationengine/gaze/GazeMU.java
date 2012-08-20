/*******************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.neurophysics.DondersLaw;
import hmi.neurophysics.EyeSaturation;
import hmi.neurophysics.ListingsLaw;
import hmi.util.StringUtil;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectManager;

import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.core.OffsetDirection;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;
import asap.realizer.util.timemanipulator.SigmoidManipulator;
import asap.realizer.util.timemanipulator.TimeManipulator;

import com.google.common.collect.ImmutableSet;

/**
 * Timing: ready: gaze target reached relax: start to move back to previous pose
 * (should be rest pose?)
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
 *         Tweed, D., & Vilis, T. (1992).
 *         Listing's Law for Gaze-Directing Head Movements. In A. Berthoz, W.
 *         Graf, & P. Vidal (Eds.), The Head-Neck Sensory-Motor System (pp.
 *         387-391). New York: Oxford University Press.
 */
@Slf4j
public class GazeMU implements AnimationUnit
{
    protected float qGaze[];

    protected float qTemp[];

    protected float qStartLeftEye[], qStartRightEye[];

    private float qStart[];

    protected float vecTemp[];

    protected KeyPosition ready;

    protected KeyPosition relax;

    protected VJoint neck;

    protected VJoint rEye;

    protected VJoint lEye;
    
    protected VJoint rEyeCurr;

    protected VJoint lEyeCurr;
    
    protected AnimationPlayer player;

    protected WorldObjectManager woManager;

    protected String target;

    protected TimeManipulator tmp;

    protected WorldObject woTarget;

    protected double preparationDuration;

    protected double relaxDuration = TimePeg.VALUE_UNKNOWN;

    // private static Logger logger =
    // LoggerFactory.getLogger(GazeMU.class.getName());

    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private float[] localGaze = new float[3];

    protected double offsetAngle = 0;
    protected OffsetDirection offsetDirection = OffsetDirection.NONE;

    protected static final double RELATIVE_READY_TIME = 0.25;
    protected static final double RELATIVE_RELAX_TIME = 0.75;

    protected AnimationUnit relaxUnit;

    public GazeMU()
    {
        qGaze = new float[4];
        qTemp = new float[4];
        qStart = new float[4];
        qStartLeftEye = new float[4];
        qStartRightEye = new float[4];
        vecTemp = new float[3];
        ready = new KeyPosition("ready", RELATIVE_READY_TIME, 1);
        relax = new KeyPosition("relax", RELATIVE_RELAX_TIME, 1);
        addKeyPosition(ready);
        addKeyPosition(relax);
        addKeyPosition(new KeyPosition("start", 0, 1));
        addKeyPosition(new KeyPosition("end", 1, 1));
        target = "";

        // defaults from presenter
        tmp = new SigmoidManipulator(5, 1);
    }

    protected float[] getOffsetRotation()
    {
        float[] q = Quat4f.getQuat4f();
        Quat4f.setIdentity(q);
        switch (offsetDirection)
        {
        case NONE:
            break;
        case RIGHT:
            Quat4f.setFromAxisAngle4f(q, 0, -1, 0, (float) Math.toRadians(offsetAngle));
            break;
        case LEFT:
            Quat4f.setFromAxisAngle4f(q, 0, 1, 0, (float) Math.toRadians(offsetAngle));
            break;
        case UP:
            Quat4f.setFromAxisAngle4f(q, -1, 0, 0, (float) Math.toRadians(offsetAngle));
            break;
        case DOWN:
            Quat4f.setFromAxisAngle4f(q, 1, 0, 0, (float) Math.toRadians(offsetAngle));
            break;
        case UPRIGHT:
            Quat4f.setFromAxisAngle4f(q, -1, -1, 0, (float) Math.toRadians(offsetAngle));
            break;
        case UPLEFT:
            Quat4f.setFromAxisAngle4f(q, -1, 1, 0, (float) Math.toRadians(offsetAngle));
            break;
        case DOWNLEFT:
            Quat4f.setFromAxisAngle4f(q, 1, 1, 0, (float) Math.toRadians(offsetAngle));
            break;
        case DOWNRIGHT:
            Quat4f.setFromAxisAngle4f(q, 1, -1, 0, (float) Math.toRadians(offsetAngle));
            break;
        case POLAR:
            break;
        }
        return q;
    }

    @Override
    public GazeMU copy(AnimationPlayer p) throws MUSetupException
    {
        GazeMU gmu = new GazeMU();
        gmu.neck = p.getVNext().getPart(Hanim.skullbase);
        gmu.lEye = p.getVNext().getPart(Hanim.l_eyeball_joint);
        gmu.rEye = p.getVNext().getPart(Hanim.r_eyeball_joint);
        gmu.lEyeCurr = p.getVCurr().getPart(Hanim.l_eyeball_joint);
        gmu.rEyeCurr = p.getVCurr().getPart(Hanim.r_eyeball_joint);
        gmu.offsetAngle = offsetAngle;
        gmu.offsetDirection = offsetDirection;
        gmu.player = p;
        gmu.woManager = p.getWoManager();
        gmu.target = target;
        return gmu;
    }

    public void setTarget(String target) throws MUPlayException
    {
        if (woManager == null)
        {
            throw new MUPlayException("Gaze target not found, no WorldObjectManager set up.", this);
        }
        woTarget = woManager.getWorldObject(target);
        if (woTarget == null)
        {
            throw new MUPlayException("Gaze target not found", this);
        }
        woTarget.getTranslation2(localGaze, neck);
        Quat4f.transformVec3f(getOffsetRotation(), localGaze);
        setEndRotation(localGaze);
    }

    public void setStartPose() throws MUPlayException
    {

        player.getVCurr().getPart(Hanim.skullbase).getRotation(qStart);
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
    void setEndRotation(float[] gazeDir) throws MUPlayException
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
    public double getPreferedDuration()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getReadyDuration()
    {
        // TODO: determine readyDuration with Fitts' law
        return 1;
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
        if (t < 0.25)
        {
            float tManip = (float) tmp.manip(t / 0.25);
            Quat4f.interpolate(qTemp, qStart, qGaze, tManip);
            neck.setRotation(qTemp);
            playEyes(t);
        }
        else if (t > 0.75)
        {
            relaxUnit.play((t - 0.75) / 0.25);
            log.debug("play relax {}",(t - 0.75) / 0.25);
        }
        else
        {
            neck.setRotation(qGaze);
            playEyes(t);
        }
    }

    @Override
    public TimedAnimationMotionUnit createTMU(FeedbackManager bfm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
    {
        return new GazeTMU(bfm, bmlBlockPeg, bmlId, id, this, pb);
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("target"))
        {
            target = value;
        }
        else if (name.equals("offsetdirection"))
        {
            offsetDirection = OffsetDirection.valueOf(value);
        }
        else if (StringUtil.isNumeric(value))
        {
            setFloatParameterValue(name, Float.parseFloat(value));
        }
        else throw new InvalidParameterException(name, value);
    }

    @Override
    public String getParameterValue(String name) throws ParameterNotFoundException
    {
        if (name.equals("target")) return target;
        if (name.equals("offsetdirection")) return "" + offsetDirection;
        return "" + getFloatParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterNotFoundException
    {
        if (name.equals("offsetangle")) return (float) offsetAngle;
        throw new ParameterNotFoundException(name);
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterNotFoundException
    {
        if (name.equals("offsetangle"))
        {
            offsetAngle = value;
        }
        else
        {
            throw new ParameterNotFoundException(name);
        }
    }

    /**
     * Set the time manipulator that describes the velocity profile for attack
     * and decay. Default is SigmoidManipulator(3,4)
     */
    public void setTimeManipulator(TimeManipulator tmp)
    {
        this.tmp = tmp;
    }

    @Override
    public String getReplacementGroup()
    {
        return "gaze";
    }

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    @Override
    public List<KeyPosition> getKeyPositions()
    {
        return keyPositionManager.getKeyPositions();
    }

    @Override
    public void setKeyPositions(List<KeyPosition> p)
    {
        keyPositionManager.setKeyPositions(p);
    }

    @Override
    public KeyPosition getKeyPosition(String name)
    {
        return keyPositionManager.getKeyPosition(name);
    }

    @Override
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

    private static final Set<String> PHJOINTS = ImmutableSet.of();
    private static final Set<String> KINJOINTSALL = ImmutableSet.of(Hanim.skullbase, Hanim.l_eyeball_joint, Hanim.r_eyeball_joint);
    private static final Set<String> KINJOINTSNECK = ImmutableSet.of(Hanim.skullbase);

    @Override
    public Set<String> getPhysicalJoints()
    {
        return PHJOINTS;
    }

    public void setupRelaxUnit()
    {
        relaxUnit = player.getRestPose().createTransitionToRest(getKinematicJoints());
    }

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
    public void startUnit(double t) throws MUPlayException
    {

    }
}
