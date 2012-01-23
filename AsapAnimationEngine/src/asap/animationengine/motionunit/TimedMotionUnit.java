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
package asap.animationengine.motionunit;

import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.NullFeedbackManager;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.elckerlyc.planunit.ParameterNotFoundException;
import hmi.elckerlyc.planunit.PlanUnitFloatParameterNotFoundException;
import hmi.elckerlyc.planunit.PlanUnitParameterNotFoundException;
import hmi.elckerlyc.planunit.TimedAbstractPlanUnit;
import hmi.elckerlyc.planunit.PlanUnitTimeManager;

import java.util.ArrayList;
import java.util.Set;
import lombok.Delegate;

import hmi.bml.feedback.BMLSyncPointProgressFeedback;

/**
 * When you do not set an end time peg, 'UNKNNOWN' is assumed. This leads to the TimedMotionUnit being timed as
 * starttime..starttime+mu.getpreferredduration() When you do not set a start time peg, the animation cannot be played
 * 
 * @author welberge
 */
public class TimedMotionUnit extends TimedAbstractPlanUnit
{
    // private Logger logger = LoggerFactory.getLogger(TimedMotionUnit.class.getName());
    private final MotionUnit mu;
    protected ArrayList<KeyPosition> progressHandled = new ArrayList<KeyPosition>();
    
    @Delegate
    protected final PlanUnitTimeManager puTimeManager;
    public Set<String> getKinematicJoints(){return mu.getKinematicJoints();}
    public Set<String> getPhysicalJoints(){return mu.getPhysicalJoints();};
    
    /**
     * Constructor
     * @param bmlBlockPeg
     * @param bmlId BML block id
     * @param id behaviour id
     * @param m motion unit
     */
    public TimedMotionUnit(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, MotionUnit m)
    {
        super(bbf, bmlBlockPeg, bmlId, id);
        mu = m;
        puTimeManager = new PlanUnitTimeManager(mu);
    }

    public TimedMotionUnit(BMLBlockPeg bmlBlockPeg, String bmlId, String id, MotionUnit m)
    {
        this(NullFeedbackManager.getInstance(), bmlBlockPeg, bmlId, id, m);
    }

    

    public KeyPosition getKeyPosition(String kid)
    {
        return getMotionUnit().getKeyPosition(kid);
    }

    /**
     * Send progress feedback for all key positions passed at canonical time t.
     * 
     * @param t canonical time 0 &lt= t &lt=1
     * @param time time since start of BML execution
     */
    private void sendProgress(double t, double time)
    {
        for (KeyPosition k : mu.getKeyPositions())
        {
            if (k.time <= t)
            {
                if (!progressHandled.contains(k))
                {
                    String bmlId = getBMLId();
                    String behaviorId = getId();
                    String syncId = k.id;
                    double bmlBlockTime = time - bmlBlockPeg.getValue();             
                    feedback(new BMLSyncPointProgressFeedback(bmlId, behaviorId, syncId, bmlBlockTime, time));
                    progressHandled.add(k);
                }
            }
        }
    }

    @Override
    protected void playUnit(double time) throws TMUPlayException
    {
        double t = puTimeManager.getRelativeTime(time);        
        try
        {
            // logger.debug("Timed Motion Unit play {}",time);
            mu.play(t);
        }
        catch (MUPlayException ex)
        {
            throw new TMUPlayException(ex.getLocalizedMessage(), this, ex);
        }
        sendProgress(t, time);
    }

    @Override
    public void stopUnit(double time)
    {
        if (time < getEndTime())
        {
            sendProgress(puTimeManager.getRelativeTime(time), time);
        }
        else
        {
            sendProgress(1, time);
        }
    }

    /**
     * @return the encapsulated motion unit
     */
    public MotionUnit getMotionUnit()
    {
        return mu;
    }

    @Override
    public String getReplacementGroup()
    {
        return mu.getReplacementGroup();
    }

    @Override
    public double getPreferedDuration()
    {
        return mu.getPreferedDuration();
    }

    @Override
    public void setParameterValue(String paramId, String value) throws ParameterException
    {
        try
        {
            mu.setParameterValue(paramId, value);
        }
        catch (ParameterNotFoundException e)
        {
            throw new PlanUnitParameterNotFoundException(getBMLId(), getId(), e.getParamId(), e);
        }
    }

    @Override
    public void setFloatParameterValue(String paramId, float value) throws ParameterException
    {
        try
        {
            mu.setFloatParameterValue(paramId, value);
        }
        catch (ParameterNotFoundException e)
        {
            throw new PlanUnitFloatParameterNotFoundException(getBMLId(), getId(), e.getParamId(), e);
        }
    }

    @Override
    public float getFloatParameterValue(String paramId) throws ParameterException
    {
        try
        {
            return mu.getFloatParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw new PlanUnitFloatParameterNotFoundException(getBMLId(), getId(), e.getParamId(), e);
        }
    }

    @Override
    public String getParameterValue(String paramId) throws ParameterException
    {
        try
        {
            return mu.getParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw new PlanUnitParameterNotFoundException(getBMLId(), getId(), e.getParamId(), e);
        }
    }
}
