package asap.animationengine.gaze;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;
import hmi.math.Quat4f;
import hmi.math.Vec3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUSetupException;
import asap.motionunit.MUPlayException;
import asap.timemanipulator.ErfManipulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class DynamicTorsoGazeMU extends GazeMU
{
    //SMAX/Smax(t) in Grillion
    
    private static final double TORSO_TIME_SCALE = 2;   //2x slower than neck
    private static final double EYE_TIME_SCALE = 0.5;   //2x faster than neck
    private static final int FPS = 30;               //used as multiplier for the tmp setup
    private float[] localGaze = new float[3];    
    private ImmutableList<VJoint> joints;
    private ImmutableSet<String> kinematicJoints;
    
    float qRot[] = Quat4f.getQuat4f();
    private double preparationDuration;
    
    public DynamicTorsoGazeMU()
    {
        setupKeyPositions();
    }
    
    @Override
    public void play(double t) throws MUPlayException
    {
        if (t < RELATIVE_READY_TIME)
        {
            
        }
        else if (t > 0.75)
        {

        }
        else
        {

        }
    }

    @Override
    public void setStartPose() throws MUPlayException
    {

    }
    
    protected void setTarget() throws MUPlayException
    {
        woTarget.getTranslation2(localGaze, joints.get(0));
        Quat4f.transformVec3f(getOffsetRotation(), localGaze);
        setEndRotation(localGaze);        
    }
    
    private double getAngle()
    {
        return Quat4f.getAngle(qRot);
    }
    
    @Override
    public double getPreferedReadyDuration()
    {
        return TARGET_IMPORTANCE*getAngle()/NECK_VELOCITY;
    }

    @Override
    public void setDurations(double prepDur, double relaxDur)
    {
        preparationDuration = prepDur;
        tmp = new ErfManipulator((int)(prepDur*FPS));
    }

    @Override
    public void setEndRotation(float[] gazeDir)
    {
        Quat4f.setFromVectors(qRot, Vec3f.getVec3f(0,0,1), gazeDir);
    }

    @Override
    public DynamicTorsoGazeMU copy(AnimationPlayer p) throws MUSetupException
    {
        DynamicTorsoGazeMU copy = new DynamicTorsoGazeMU();
        List<VJoint> joints = new ArrayList<VJoint>(VJointUtils.gatherJoints(Hanim.THORACIC_JOINTS, p.getVNext()));
        joints.addAll(VJointUtils.gatherJoints(Hanim.CERVICAL_JOINTS, p.getVNext()));
        copy.joints = ImmutableList.copyOf(joints);
        copy.kinematicJoints = ImmutableSet.copyOf(VJointUtils.transformToSidList(joints));
        copy.offsetAngle = offsetAngle;
        copy.offsetDirection = offsetDirection;
        copy.player = p;
        copy.woManager = p.getWoManager();
        copy.target = target;        
        return copy;
    }
    
    @Override
    public Set<String> getKinematicJoints()
    {
        return kinematicJoints;
    }
}
