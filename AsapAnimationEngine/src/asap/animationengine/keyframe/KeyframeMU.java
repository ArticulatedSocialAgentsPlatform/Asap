/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.keyframe;

import hmi.animation.SkeletonInterpolator;
import hmi.animation.VJoint;
import hmi.animation.VJointUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import lombok.Setter;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterNotFoundException;

import com.google.common.collect.ImmutableSet;

/**
 * Motion unit for keyframe/mocap motion specified in a SkeletonInterpolator.
 * 
 * @author Herwin van Welbergen
 * 
 */
public class KeyframeMU implements AnimationUnit
{
    private final SkeletonInterpolator baseIp;
    private SkeletonInterpolator currentIp;
    private HashMap<String, String> parameters = new HashMap<String, String>(); // name => value set
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private boolean mirror = false;
    private VJoint joint;

    @Setter
    private boolean additive = false;

    private Set<String> filter = new HashSet<String>();
    private AnimationPlayer aniPlayer;

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
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

    public KeyframeMU(SkeletonInterpolator si)
    {
        baseIp = si;
        currentIp = new SkeletonInterpolator(si);
    }

    public void setTarget(VJoint v)
    {
        baseIp.setTarget(v);
        currentIp.setTarget(v);
    }

    public Set<String> getJoints()
    {
        return new HashSet<String>(Arrays.asList(currentIp.getPartIds()));
    }

    @Override
    public KeyPosition getKeyPosition(String name)
    {
        return keyPositionManager.getKeyPosition(name);
    }

    @Override
    public double getPreferedDuration()
    {
        return currentIp.getEndTime() - currentIp.getStartTime();
    }

    @Override
    public void play(double t)
    {
        currentIp.time(currentIp.getStartTime() + getPreferedDuration() * t);
    }

    private void applyParameters()
    {
        currentIp = new SkeletonInterpolator(baseIp);
        if (mirror)
        {
            currentIp.mirror();
        }
        if (filter.size() > 0)
        {
            currentIp.filterJoints(filter);
        }
    }

    public void filterJoints(Set<String> jointFilter)
    {
        filter.clear();
        filter.addAll(jointFilter);
        StringBuffer value = new StringBuffer();
        for (String joint : jointFilter)
        {
            value.append(joint);
            value.append(" ");
        }
        parameters.put("joints", value.toString().trim());
        applyParameters();
    }

    /**
     * @return the unfiltered, unmirrored etc skeletonInterpolator
     */
    public SkeletonInterpolator getSkeletonInterpolator()
    {
        return baseIp;
    }

    public KeyframeMU copy(VJoint v) throws MUSetupException
    {
        ArrayList<VJoint> vjParts = new ArrayList<VJoint>();
        for (String s : baseIp.getPartIds())
        {
            for (VJoint vj : v.getParts())
            {
                if (s.equals(vj.getSid()))
                {
                    vjParts.add(vj);
                }
            }
        }
        Set<String> skelJoints = new HashSet<String>(Arrays.asList(baseIp.getPartIds()));
        skelJoints.removeAll(VJointUtils.transformToSidSet(vjParts));
        if (!skelJoints.isEmpty())
        {
            throw new MUSetupException("Joints "+skelJoints+
                    " in SkeletonInterpolator not available in the steered body.", this);
        }

        SkeletonInterpolator ipCopy = new SkeletonInterpolator(baseIp, vjParts.toArray(new VJoint[vjParts.size()]));
        KeyframeMU copy = new KeyframeMU(ipCopy);
        copy.additive = additive;
        copy.setTarget(v);
        copy.joint = v;
        for (Entry<String, String> paramValue : parameters.entrySet())
        {
            copy.setParameterValue(paramValue.getKey(), paramValue.getValue());
        }
        for (KeyPosition kp : getKeyPositions())
        {
            copy.addKeyPosition(kp);
        }
        return copy;
    }

    @Override
    public void setFloatParameterValue(String name, float value)
    {
        parameters.put(name, "" + value);
    }

    @Override
    public void setParameterValue(String name, String value)
    {
        if (name.equals("joints"))
        {
            String joints[] = value.split("\\s");
            filter.clear();
            for (String joint : joints)
            {
                filter.add(joint);
            }
            // ip.filterJoints(jointSet);
            applyParameters();
        }
        else if (name.equals("additive"))
        {
            additive = Boolean.parseBoolean("additive");
        }
        else if (name.equals("mirror"))
        {
            mirror = Boolean.parseBoolean(value);
            applyParameters();
        }
        parameters.put(name, value);
    }

    @Override
    public String getParameterValue(String name) throws ParameterNotFoundException
    {
        if (name.equals("mirror")) return "false";// no need to store mirroring
        if (name.equals("additive"))
        {
            return "" + additive;
        }
        if (parameters.get(name) == null)
        {
            throw new ParameterNotFoundException(name);
        }
        else return parameters.get(name);

    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterNotFoundException
    {
        if (parameters.get(name) == null)
        {
            throw new ParameterNotFoundException(name);
        }
        float value = 0;
        try
        {
            value = Float.parseFloat(parameters.get(name));
        }
        catch (NumberFormatException ex)
        {
            throw new ParameterNotFoundException(name);
        }
        return value;
    }

    @Override
    public TimedAnimationMotionUnit createTMU(FeedbackManager bbm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedAnimationMotionUnit(bbm, bbPeg, bmlId, id, this, pb, aniPlayer);
    }

    @Override
    public KeyframeMU copy(AnimationPlayer p) throws MUSetupException
    {
        this.aniPlayer = p;
        if (additive)
        {
            return copy(p.constructAdditiveBody(getAdditiveJoints()));
        }
        return copy(p.getVNext());
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
        if (additive)
        {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(currentIp.getPartIds());
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {

    }

    @Override
    public void cleanup()
    {
        if (aniPlayer != null)
        {
            aniPlayer.removeAdditiveBody(joint);
        }
    }

    @Override
    public Set<String> getAdditiveJoints()
    {
        if (!additive)
        {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(currentIp.getPartIds());
    }
}
