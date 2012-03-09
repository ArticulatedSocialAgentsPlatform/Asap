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
package asap.animationengine.keyframe;

import hmi.animation.*;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.KeyPositionManager;
import hmi.elckerlyc.planunit.KeyPositionManagerImpl;
import hmi.elckerlyc.planunit.ParameterNotFoundException;

import java.util.*;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableSet;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.*;


/**
 * Motion unit for keyframe/mocap motion specified in a SkeletonInterpolator.
 * 
 * @author Herwin van Welbergen
 * 
 */
public class KeyframeMU implements MotionUnit
{
    private SkeletonInterpolator baseIp;
    private SkeletonInterpolator currentIp;
    private HashMap<String, String> parameters = new HashMap<String, String>(); // name => value set
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private boolean mirror = false;
    private Set<String> filter = new HashSet<String>();

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

    public MotionUnit copy(VJoint v)
    {
        VJoint[] empty = new VJoint[0];
        ArrayList<VJoint> vjParts = new ArrayList<VJoint>();
        for (String s : baseIp.getPartIds())
        {
            for (VJoint vj : v.getParts())
            {
                if (vj.getSid().equals(s))
                {
                    vjParts.add(vj);
                }
            }
        }
        SkeletonInterpolator ipPredict = new SkeletonInterpolator(baseIp, vjParts.toArray(empty));
        KeyframeMU copy = new KeyframeMU(ipPredict);
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
        if (name.equals("mirror"))
            return "false";// no need to store mirroring
        if (parameters.get(name) == null)
        {
            throw new ParameterNotFoundException(name);
        }
        else
            return parameters.get(name);

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
    public TimedMotionUnit createTMU(FeedbackManager bbm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedMotionUnit(bbm, bbPeg, bmlId, id, this, pb);
    }

    @Override
    public MotionUnit copy(AnimationPlayer p)
    {
        return copy(p.getVNext());
    }

    @Override
    public String getReplacementGroup()
    {
        try
        {
            return getParameterValue("replacementgroup");
        }
        catch (ParameterNotFoundException e)
        {
            return null;
        }
    }
    
    private static final Set<String>PHJOINTS = ImmutableSet.of(); 
    
    @Override
    public Set<String> getPhysicalJoints()
    {
        return PHJOINTS;
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        return ImmutableSet.copyOf(currentIp.getPartIds());
    } 
}
