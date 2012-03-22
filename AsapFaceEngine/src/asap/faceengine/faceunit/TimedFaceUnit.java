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
package asap.faceengine.faceunit;

import hmi.bml.feedback.BMLSyncPointProgressFeedback;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.planunit.KeyPosition;
import hmi.elckerlyc.planunit.ParameterException;
import hmi.elckerlyc.planunit.ParameterNotFoundException;
import hmi.elckerlyc.planunit.PlanUnitTimeManager;
import hmi.elckerlyc.planunit.TimedAbstractPlanUnit;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;

import java.util.ArrayList;

import lombok.Delegate;
import asap.motionunit.MUPlayException;

/**
 * When you do not set an end time peg, 'UNKNOWN' is assumed. This leads to the faceunit being timed
 * as starttime..starttime+getpreferredduration
 * 
 * When you do not set a start time peg, the animation cannot be played
 * 
 * @author Dennis Reidsma
 */
public class TimedFaceUnit extends TimedAbstractPlanUnit
{
    public final FaceUnit fu;
    protected ArrayList<KeyPosition> progressHandled = new ArrayList<KeyPosition>();
    
    @Delegate
    private final PlanUnitTimeManager puTimeManager;

    /**
     * Constructor
     * 
     * @param bmlId
     *            BML block id
     * @param id
     *            behaviour id
     * @param f
     *            face unit
     */
    public TimedFaceUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, FaceUnit f)
    {
        super(bfm, bbPeg, bmlId, id);
        fu = f;
        puTimeManager = new PlanUnitTimeManager(f);
    }

    /**
     * Gets the keyposition with id id
     */
    public KeyPosition getKeyPosition(String kid)
    {
        return fu.getKeyPosition(kid);
    }

    /**
     * Send progress feedback for all key positions passed at canonical time t.
     * 
     * @param t
     *            canonical time 0 &lt= t &lt=1
     * @param time
     *            time since start of BML execution
     */
    private void sendProgress(double t, double time)
    {
        for (KeyPosition k : fu.getKeyPositions())
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
            else
            {
                if (progressHandled.contains(k))
                {
                    progressHandled.remove(k);
                }
            }
        }
    }

    @Override
    public void playUnit(double time) throws TimedPlanUnitPlayException
    {
        double t = puTimeManager.getRelativeTime(time);
        try
        {
            fu.play(t);
        }
        catch (MUPlayException ex)
        {
            throw new TFUPlayException(ex.getLocalizedMessage(), this, ex);
        }
        sendProgress(t, time);
    }

    public void cleanup()
    {
        fu.cleanup();
    }

    /**
     * @return the encapsulated face unit
     */
    public FaceUnit getFaceUnit()
    {
        return fu;
    }

    @Override
    protected void stopUnit(double time) throws TimedPlanUnitPlayException
    {
        sendProgress(1, time);
        cleanup();
    }

    @Override
    public String getReplacementGroup()
    {
        return fu.getReplacementGroup();
    }

    @Override
    public double getPreferedDuration()
    {
        return fu.getPreferedDuration();
    }

    @Override
    public void setParameterValue(String paramId, String value) throws ParameterException
    {
        try
        {
            fu.setParameterValue(paramId, value);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }

    }

    @Override
    public void setFloatParameterValue(String paramId, float value) throws ParameterException
    {
        try
        {
            fu.setFloatParameterValue(paramId, value);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitFloatParameterNotFoundException(e);
        }
    }

    @Override
    public float getFloatParameterValue(String paramId) throws ParameterException
    {
        try
        {
            return fu.getFloatParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitFloatParameterNotFoundException(e);
        }
    }

    @Override
    public String getParameterValue(String paramId) throws ParameterException
    {
        try
        {
            return fu.getParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }
    }
}
