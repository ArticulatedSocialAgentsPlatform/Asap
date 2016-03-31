/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.transitions;

import hmi.animation.VJoint;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import asap.motionunit.MUPlayException;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;

/**
 * Transitions from a dynamic start position to a pose.
 * @author Herwin
 */
public abstract class TransitionToPoseMU extends TransitionMU
{
    protected float startPose[] = null;
    protected float endPose[];
    protected List<VJoint> startJoints;
    protected float result[];
    
    public TransitionToPoseMU()
    {
        super();
    }
    
    public TransitionToPoseMU(List<VJoint> j, List<VJoint> startPoseJoints, float ep[])
    {
        super();
        joints = j;
        startJoints = startPoseJoints;
        if (ep != null)
        {
            endPose = Arrays.copyOf(ep, ep.length);
            result = new float[ep.length];
        }
        else
        {
            endPose = null;
        }        
    }
    
    /**
     * Set the start pose
     * 
     * @param sp
     *            the new start pose
     */
    public void setStartPose(float sp[])
    {
        startPose = Arrays.copyOf(sp, sp.length);
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
        Collection<String> j = Collections2.transform(joints, new Function<VJoint, String>()
        {
            @Override
            public String apply(VJoint joint)
            {
                if (joint == null) return "";
                return joint.getSid();
            }
        });
        return ImmutableSet.copyOf(j);
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {
        setStartPose();         
    }
}
