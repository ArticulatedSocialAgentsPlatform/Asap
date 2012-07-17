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
package asap.nao.planunit;

import asap.nao.planunit.NUPlayException;
import asap.nao.planunit.TNUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.*;

import java.util.ArrayList;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;


import lombok.Delegate;

/**
 * @author Robin ten Buuren
 */
public class TimedNaoUnit extends TimedAbstractPlanUnit
{
    public final NaoUnit nu;
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
     * @param n
     *            nao unit
     */
    public TimedNaoUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, NaoUnit n)
    {
        super(bfm, bbPeg, bmlId, id);
        nu = n;
        puTimeManager = new PlanUnitTimeManager(n);
    }

    /**
     * Starts the PlanUnit, is only called once at start
     * 
     * @param time global start time
     * @throws TimedPlanUnitPlayException
     */
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        nu.startUnit(time);
    };

    /**
     * Gets the keyposition with id id
     */
    public KeyPosition getKeyPosition(String kid)
    {
        return nu.getKeyPosition(kid);
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
        for (KeyPosition k : nu.getKeyPositions())
        {
            if (k.time <= t)
            {
                if (!progressHandled.contains(k))
                {
                    String bmlId = getBMLId();
                    String behaviorId = getId();
                    String syncId = k.id;
                    //String id = "fb-" + bmlId + ":" + behaviorId + ":" + syncId;
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
            nu.play(t);
        }
        catch (NUPlayException ex)
        {
            throw new TNUPlayException(ex.getLocalizedMessage(), this);
        }
        sendProgress(t, time);
    }

    public void cleanup()
    {
        nu.cleanup();
    }

    /**
     * @return the encapsulated Nao unit
     */
    public NaoUnit getNaoUnit()
    {
        return nu;
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
        return nu.getReplacementGroup();
    }

    @Override
    public double getPreferedDuration()
    {
        return nu.getPreferedDuration();
    }

    @Override
    public void setParameterValue(String paramId, String value) throws ParameterException
    {
        try
        {
            nu.setParameterValue(paramId, value);
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
            nu.setFloatParameterValue(paramId, value);
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
            return nu.getFloatParameterValue(paramId);
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
            return nu.getParameterValue(paramId);
        }
        catch (ParameterNotFoundException e)
        {
            throw wrapIntoPlanUnitParameterNotFoundException(e);
        }
    }
}
