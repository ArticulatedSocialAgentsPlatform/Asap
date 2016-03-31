/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.math.Vec3f;
import hmi.util.Resources;
import hmi.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import saiba.bml.BMLGestureSync;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.MovementTimingUtils;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.transitions.SlerpTransitionToPoseMU;
import asap.motionunit.MUPlayException;
import asap.motionunit.MotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.InvalidParameterException;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.Priority;

import com.google.common.collect.ImmutableSet;

/**
 * Wraps a ProcAnimationMU inbetween a slerp transitions, and an automatically generated (by the restpose)
 * relax controller
 * The ProcAnimation is played between its strokeStart and strokeEnd
 * @author Herwin
 */
@Slf4j
public class ProcAnimationGestureMU implements GestureUnit
{
    private ProcAnimationMU gestureUnit = new ProcAnimationMU();
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private SlerpTransitionToPoseMU prepUnit;
    private AnimationUnit relaxUnit;
    private VJoint vStart;
    private VJoint vNext;

    // copy of the procanimation, used to figure out the stroke start positions
    private ProcAnimationMU copyProc;
    private VJoint copyJoint;

    private Resources resource;
    private double preStrokeHoldDuration = 0;

    public double getStrokeDuration()
    {
        return gestureUnit.getPrefDuration();
    }

    public double getPreStrokeHoldDuration()
    {
        return preStrokeHoldDuration;
    }

    public double getPostStrokeHoldDuration()
    {
        return postStrokeHoldDuration;
    }

    private double postStrokeHoldDuration = 0;
    private int priority = Priority.GESTURE;
    private AnimationPlayer aniPlayer;

    private float[] getPoseInStroke(String sync)
    {
        int i = 0;
        float[] pose = new float[gestureUnit.getControlledJoints().size() * 4];

        i = 0;

        copyProc.play(copyProc.getKeyPosition(sync).time + 0.01);
        for (VJoint v : gestureUnit.getControlledJoints())
        {
            VJoint v2 = copyJoint.getPartBySid(v.getSid());
            v2.getRotation(pose, i);
            i += 4;
        }
        return pose;
    }

    public double getInterruptionDuration()
    {
        return aniPlayer.getRestPose().getTransitionToRestDuration(aniPlayer.getVCurr(),
                VJointUtils.transformToSidSet(gestureUnit.getControlledJoints()));
    }

    public double getRetractionDuration()
    {
        copyProc.play(copyProc.getKeyPosition(BMLGestureSync.STROKE_END.getId()).time - 0.01);
        return aniPlayer.getRestPose().getTransitionToRestDuration(copyJoint,
                VJointUtils.transformToSidSet(gestureUnit.getControlledJoints()));

    }

    public double getPreparationDuration()
    {
        copyProc.play(copyProc.getKeyPosition(BMLGestureSync.STROKE_START.getId()).time + 0.01);

        if (log.isDebugEnabled())
        {
            float vecDst[] = new float[3];
            float vecSrc[] = new float[3];
            copyJoint.getPart(Hanim.l_wrist).getPathTranslation(copyJoint.getPart(Hanim.l_shoulder).getParent(), vecDst);
            aniPlayer.getVCurrPartBySid(Hanim.l_wrist).getPathTranslation(aniPlayer.getVCurrPartBySid(Hanim.l_shoulder), vecSrc);
            log.debug("copyJoint lwrist pos at strokeStart:{}\n current lwrist pos:{}", Vec3f.toString(vecDst), Vec3f.toString(vecSrc));
        }

        double duration = MovementTimingUtils.getFittsMaximumLimbMovementDuration(aniPlayer.getVCurr(), copyJoint,
                VJointUtils.transformToSidSet(gestureUnit.getControlledJoints()));
        if (duration > 0) return duration;
        return 1;

        // return aniPlayer.getRestPose().getTransitionToRestDuration(vCopy, VJointUtils.transformToSidSet(gestureUnit.getControlledJoints()));
    }

    public double getPreparationFromRestDuration()
    {
        copyProc.play(getKeyPosition(BMLGestureSync.STROKE_START.getId()).time + 0.01);
        return aniPlayer.getRestPose().getTransitionToRestDuration(copyJoint,
                VJointUtils.transformToSidSet(gestureUnit.getControlledJoints()));
    }

    public int getPriority()
    {
        return priority;
    }

    private boolean inGesturePhase(String syncId)
    {
        return !(syncId.equals(BMLGestureSync.START.getId()) || syncId.equals(BMLGestureSync.END.getId())
                || syncId.equals(BMLGestureSync.READY.getId()) || syncId.equals(BMLGestureSync.RELAX.getId()));
    }

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
        if (inGesturePhase(kp.id))
        {
            gestureUnit.addKeyPosition(kp);
        }
    }

    public void setGestureUnit(ProcAnimationMU pa) throws MUSetupException
    {
        cleanup(gestureUnit);
        cleanup(copyProc);
        gestureUnit = pa;
        for (KeyPosition kp : gestureUnit.getKeyPositions())
        {
            if (inGesturePhase(kp.id))
            {
                keyPositionManager.addKeyPosition(kp);
            }
        }
        setPreStrokeHoldDuration();
        setPostStrokeHoldDuration();
        gestureUnit.setup(vNext);
        if (vNext != null)
        {
            copyJoint = vNext.copyTree("copy-");
        }
        copyProc = gestureUnit.copy(copyJoint);
    }

    public void setAnimationPlayer(AnimationPlayer ap) throws MUSetupException
    {
        vStart = ap.getVCurr();
        vNext = ap.getVNext();
        gestureUnit.setup(ap);
        copyJoint = vNext.copyTree("copy-");
        copyProc = gestureUnit.copy(copyJoint);
        aniPlayer = ap;
    }

    public void setVNext(VJoint vj)
    {
        vNext = vj;
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
        for (KeyPosition kp : p)
        {
            if (inGesturePhase(kp.id))
            {
                gestureUnit.addKeyPosition(kp);
            }
        }
    }

    @Override
    public KeyPosition getKeyPosition(String id)
    {
        return keyPositionManager.getKeyPosition(id);
    }

    @Override
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
        if (inGesturePhase(id))
        {
            gestureUnit.removeKeyPosition(id);
        }
    }

    private void setPostStrokeHoldDuration()
    {
        KeyPosition strokeEnd = keyPositionManager.getKeyPosition(BMLGestureSync.STROKE_END.getId());
        KeyPosition strokeStart = keyPositionManager.getKeyPosition(BMLGestureSync.STROKE_START.getId());
        if (strokeEnd != null && strokeStart != null)
        {
            double val = strokeEnd.time + (strokeEnd.time - strokeStart.time) * postStrokeHoldDuration;
            KeyPosition kp = getKeyPosition(BMLGestureSync.RELAX.getId());
            if (kp == null)
            {
                addKeyPosition(new KeyPosition(BMLGestureSync.RELAX.getId(), val, 1));
            }
            else
            {
                kp.time = val;
            }
        }
    }

    private void setPreStrokeHoldDuration()
    {
        KeyPosition strokeEnd = keyPositionManager.getKeyPosition(BMLGestureSync.STROKE_END.getId());
        KeyPosition strokeStart = keyPositionManager.getKeyPosition(BMLGestureSync.STROKE_START.getId());
        if (strokeEnd != null && strokeStart != null)
        {
            double val = strokeStart.time - (strokeEnd.time - strokeStart.time) * preStrokeHoldDuration;
            KeyPosition kp = getKeyPosition(BMLGestureSync.READY.getId());
            if (kp == null)
            {
                addKeyPosition(new KeyPosition(BMLGestureSync.READY.getId(), val, 1));
            }
            else
            {
                kp.time = val;
            }
        }
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
        if (name.equals("preStrokeHoldDuration"))
        {
            preStrokeHoldDuration = value;
            setPreStrokeHoldDuration();
        }
        else if (name.equals("postStrokeHoldDuration"))
        {
            postStrokeHoldDuration = value;
            setPostStrokeHoldDuration();
        }
        else
        {
            gestureUnit.setFloatParameterValue(name, value);
            copyProc.setFloatParameterValue(name, value);
        }
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("file"))
        {
            try
            {
                ProcAnimationMU mu = new ProcAnimationMU();
                mu.readXML(resource.getReader(value));
                mu.setParameters(gestureUnit.getParameters());
                setGestureUnit(mu);
                gestureUnit.setup(aniPlayer);
            }
            catch (IOException e)
            {
                throw new InvalidParameterException(name, value, e);
            }
            catch (MUSetupException e)
            {
                throw new InvalidParameterException(name, value, e);
            }
        }
        else if (StringUtil.isNumeric(value))
        {
            setFloatParameterValue(name, Float.parseFloat(value));
        }
        else
        {
            gestureUnit.setParameterValue(name, value);
            copyProc.setParameterValue(name, value);
        }
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        return gestureUnit.getParameterValue(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        return gestureUnit.getFloatParameterValue(name);
    }

    @Override
    public void play(double t) throws MUPlayException
    {
        double strokeStartTime = keyPositionManager.getKeyPosition(BMLGestureSync.STROKE_START.getId()).time;
        double readyTime = keyPositionManager.getKeyPosition(BMLGestureSync.READY.getId()).time;
        double relaxTime = keyPositionManager.getKeyPosition(BMLGestureSync.RELAX.getId()).time;

        double strokeEndTime = keyPositionManager.getKeyPosition(BMLGestureSync.STROKE_END.getId()).time;

        double relaxDuration = 1 - relaxTime;
        log.debug("time: {}", t);

        double gestureStart = gestureUnit.getKeyPosition(BMLGestureSync.STROKE_START.getId()).time;
        double gestureEnd = gestureUnit.getKeyPosition(BMLGestureSync.STROKE_END.getId()).time;
        double gestureDur = gestureEnd - gestureStart;

        if (t < readyTime)
        {
            prepUnit.play(t / readyTime);
            log.debug("prepUnit.play: {}", t / readyTime);
        }
        else if (t <= strokeStartTime)
        {
            prepUnit.play(1);
        }
        else if (t > relaxTime)
        {
            relaxUnit.play((t - relaxTime) / relaxDuration);
            priority = Priority.GESTURE_RETRACTION;
            log.debug("relaxUnit.play: {}", (t - relaxTime) / relaxDuration);
        }
        else if (t > strokeStartTime && t < strokeEndTime)
        {
            double fraction = (t - strokeStartTime) / (strokeEndTime - strokeStartTime);
            gestureUnit.play(gestureStart + gestureDur * fraction);
            log.debug("gestureUnit.play: {}", t);
        }
        else if (t <= relaxTime)
        {
            gestureUnit.play(strokeEndTime);
        }
    }

    @Override
    public ProcAnimationGestureTMU createTMU(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
    {
        return new ProcAnimationGestureTMU(bbm, bmlBlockPeg, bmlId, id, this, pb, aniPlayer);
    }

    @Override
    public double getPreferedDuration()
    {
        return gestureUnit.getPrefDuration() + getPreparationDuration() + getRetractionDuration() + preStrokeHoldDuration
                + postStrokeHoldDuration;
    }

    @Override
    public ProcAnimationGestureMU copy(AnimationPlayer p) throws MUSetupException
    {
        ProcAnimationGestureMU copy = new ProcAnimationGestureMU();
        copy.setAnimationPlayer(p);
        copy.resource = resource;
        ProcAnimationMU copyPA = gestureUnit.copy(p);
        for (KeyPosition kp : copyPA.getKeyPositions())
        {
            copyPA.removeKeyPosition(kp.id);
        }
        copy.setGestureUnit(copyPA);
        for (KeyPosition kp : getKeyPositions())
        {
            copy.addKeyPosition(kp.deepCopy());
        }
        return copy;
    }

    public double getRelativeStrokePos()
    {
        return (gestureUnit.getKeyPosition(BMLGestureSync.STROKE.getId()).time - gestureUnit.getKeyPosition(BMLGestureSync.STROKE_START
                .getId()).time)
                / (gestureUnit.getKeyPosition(BMLGestureSync.STROKE_END.getId()).time - gestureUnit
                        .getKeyPosition(BMLGestureSync.STROKE_START.getId()).time);
    }

    public void setupTransitionUnits()
    {
        // setup transunits
        float[] strokeStartPose = getPoseInStroke(BMLGestureSync.STROKE_START.getId());

        ArrayList<VJoint> startPoseJoint = new ArrayList<VJoint>();
        for (VJoint vj : gestureUnit.getControlledJoints())
        {
            startPoseJoint.add(vStart.getPartBySid(vj.getSid()));
        }

        prepUnit = new SlerpTransitionToPoseMU(gestureUnit.getControlledJoints(), startPoseJoint, strokeStartPose);
        prepUnit.setStartPose();
    }

    public void setupRelaxUnit()
    {
        relaxUnit = createRelaxUnit();
    }

    public AnimationUnit createRelaxUnit()
    {
        return aniPlayer.getRestPose().createTransitionToRestFromVJoints(gestureUnit.getControlledJoints());
    }

    @Override
    public void setResource(Resources r)
    {
        resource = r;
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
        return gestureUnit.getKinematicJoints();
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {

    }

    private void cleanup(MotionUnit mu)
    {
        if(mu!=null)
        {
            mu.cleanup();
        }
    }
    
    @Override
    public void cleanup()
    {
        cleanup(gestureUnit);
        cleanup(relaxUnit);
        cleanup(prepUnit);
        cleanup(copyProc);        
    }

    @Override
    public Set<String> getAdditiveJoints()
    {
        return ImmutableSet.of();
    }
}
