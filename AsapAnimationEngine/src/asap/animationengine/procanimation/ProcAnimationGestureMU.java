package asap.animationengine.procanimation;

import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.bml.BMLGestureSync;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.planunit.InvalidParameterException;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.KeyPositionManagerImpl;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.util.Resources;
import hmi.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUPlayException;
import asap.animationengine.motionunit.MotionUnit;
import asap.animationengine.transitions.SlerpTransitionToPoseMU;
import asap.planunit.Priority;

import com.google.common.collect.ImmutableSet;

/**
 * Wraps a ProcAnimationMU inbetween a slerp transitions, and an automatically generated (by the restpose)
 * relax controller
 * The ProcAnimation is played between its strokeStart and strokeEnd
 * @author Herwin
 */
public class ProcAnimationGestureMU implements GestureUnit
{
    private static Logger logger = LoggerFactory.getLogger(ProcAnimationGestureMU.class.getName());
    private ProcAnimationMU gestureUnit = new ProcAnimationMU();
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private SlerpTransitionToPoseMU prepUnit;
    private MotionUnit relaxUnit;
    private VJoint vStart;
    private VJoint vNext;
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
        VJoint vCopy = vStart.copyTree("copy-");
        ProcAnimationMU copyProc = gestureUnit.copy(vCopy);

        i = 0;
        copyProc.play(getKeyPosition(sync).time);
        for (VJoint v : gestureUnit.getControlledJoints())
        {
            VJoint v2 = vCopy.getPartBySid(v.getSid());
            v2.getRotation(pose, i);
            i += 4;
        }
        return pose;
    }

    public double getRetractionDuration()
    {
        VJoint vCopy = vStart.copyTree("copy-");
        ProcAnimationMU copyProc = gestureUnit.copy(vCopy);
        copyProc.play(getKeyPosition(BMLGestureSync.STROKE_END.getId()).time-0.01);
        return aniPlayer.getRestPose().getTransitionToRestDuration(vCopy, VJointUtils.transformToSidSet(gestureUnit.getControlledJoints()));
    }

    //TODO: dynamic determination from current hand position
    public double getPreparationDuration()
    {
        VJoint vCopy = vStart.copyTree("copy-");
        ProcAnimationMU copyProc = gestureUnit.copy(vCopy);
        copyProc.play(getKeyPosition(BMLGestureSync.STROKE_START.getId()).time+0.01);

        return aniPlayer.getRestPose().getTransitionToRestDuration(vCopy, VJointUtils.transformToSidSet(gestureUnit.getControlledJoints()));
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

    public void setGestureUnit(ProcAnimationMU pa)
    {
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
    }

    public void setAnimationPlayer(AnimationPlayer ap)
    {
        vStart = ap.getVCurr();
        vNext = ap.getVNext();
        gestureUnit.setup(vNext);
        aniPlayer = ap;
    }

    public void setVStartJoint(VJoint vj)
    {
        vStart = vj;
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
                gestureUnit.setup(vNext);
            }
            catch (IOException e)
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
        logger.debug("time: {}", t);

        double gestureStart = gestureUnit.getKeyPosition(BMLGestureSync.STROKE_START.getId()).time;
        double gestureEnd = gestureUnit.getKeyPosition(BMLGestureSync.STROKE_END.getId()).time;
        double gestureDur = gestureEnd - gestureStart;

        if (t < readyTime)
        {
            prepUnit.play(t / readyTime);
            logger.debug("prepUnit.play: {}", t / readyTime);
        }
        else if (t > relaxTime)
        {
            relaxUnit.play((t - relaxTime) / relaxDuration);
            priority = Priority.GESTURE_RETRACTION;
            logger.debug("relaxUnit.play: {}", (t - relaxTime) / relaxDuration);
        }
        else if (t > strokeStartTime && t < strokeEndTime)
        {
            double fraction = (t - strokeStartTime) / (strokeEndTime - strokeStartTime);
            gestureUnit.play(gestureStart + gestureDur * fraction);
            logger.debug("gestureUnit.play: {}", t);
        }
    }

    @Override
    public ProcAnimationGestureTMU createTMU(FeedbackManager bbm, BMLBlockPeg bmlBlockPeg, String bmlId, String id)
    {
        return new ProcAnimationGestureTMU(bbm, bmlBlockPeg, bmlId, id, this);
    }

    @Override
    public double getPreferedDuration()
    {
        return gestureUnit.getPrefDuration() + getPreparationDuration() + getRetractionDuration() + preStrokeHoldDuration
                + postStrokeHoldDuration;
    }

    @Override
    public ProcAnimationGestureMU copy(AnimationPlayer p)
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

    @Override
    public String getReplacementGroup()
    {
        return gestureUnit.getReplacementGroup();
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
        // setup keypos
        // double strokeDuration = gestureUnit.getPrefDuration();
        // double duration = this.getPreferedDuration();
        // double postHoldDur = gestureUnit.getPrefDuration()*postStrokeHoldDuration;
        // double preHoldDur = gestureUnit.getPrefDuration()*preStrokeHoldDuration;
        //
        // double relStrokePos = getRelativeStrokePos();
        //
        // keyPositionManager.getKeyPosition(BMLGestureSync.READY.getId()).time = defaultRelaxDuration/duration;
        // keyPositionManager.getKeyPosition(BMLGestureSync.STROKE_START.getId()).time = (defaultRelaxDuration+preHoldDur)/duration;
        // keyPositionManager.getKeyPosition(BMLGestureSync.STROKE.getId()).time = (defaultRelaxDuration+preHoldDur+strokeDuration*relStrokePos)/duration;
        // keyPositionManager.getKeyPosition(BMLGestureSync.STROKE_END.getId()).time = (defaultRelaxDuration+preHoldDur+strokeDuration)/duration;
        // keyPositionManager.getKeyPosition(BMLGestureSync.RELAX.getId()).time = (defaultRelaxDuration+postHoldDur+
        // preHoldDur+strokeDuration)/duration;

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
        relaxUnit = aniPlayer.getRestPose().createTransitionToRest(VJointUtils.transformToSidSet(gestureUnit.getControlledJoints()));
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
}
