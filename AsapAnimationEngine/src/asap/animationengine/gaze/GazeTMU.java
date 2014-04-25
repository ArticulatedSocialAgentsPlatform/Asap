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

import saiba.bml.BMLGestureSync;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.motionunit.MUPlayException;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Timed motion unit for gaze, makes sure the gaze start pose is set at start.
 * @author Herwin
 * 
 */
public class GazeTMU extends TimedAnimationMotionUnit
{
    private GazeMU gmu;
    private TimedAnimationUnit relaxUnit;
    private final AnimationPlayer aniPlayer;

    public GazeTMU(FeedbackManager bfm, BMLBlockPeg bmlBlockPeg, String bmlId, String id, GazeMU mu, PegBoard pb, AnimationPlayer aniPlayer)
    {
        super(bfm, bmlBlockPeg, bmlId, id, mu, pb, aniPlayer);
        this.aniPlayer = aniPlayer;
        gmu = mu;
    }

    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        super.startUnit(time);
        try
        {
            double readyTime = getTime("ready");
            double relaxTime = getTime("relax");
            gmu.setStartPose();

            double readyDuration;
            double relaxDuration;
            if (readyTime != TimePeg.VALUE_UNKNOWN)
            {
                readyDuration = readyTime - getStartTime();
            }
            else
            {
                readyDuration = gmu.getPreferedReadyDuration();

                if (getEndTime() != TimePeg.VALUE_UNKNOWN && getStartTime() != TimePeg.VALUE_UNKNOWN)
                {
                    double duration = getEndTime() - getStartTime();
                    if (duration <= readyDuration * 2)
                    {
                        readyDuration = duration * 0.5;
                    }
                }

                TimePeg startPeg = this.getTimePeg(BMLGestureSync.START.getId());
                if (startPeg == null)
                {
                    throw new TimedPlanUnitPlayException("Start peg is null", this);
                }
                OffsetPeg tpReady = new OffsetPeg(startPeg, readyDuration);
                setTimePeg("ready", tpReady);
            }

            if (relaxTime != TimePeg.VALUE_UNKNOWN)
            {
                relaxDuration = readyTime - getStartTime();
            }
            else
            {
                relaxDuration = readyDuration;
                TimePeg endPeg = getTimePeg("end");
                OffsetPeg tpRelax;
                if (endPeg != null && getEndTime() != TimePeg.VALUE_UNKNOWN)
                {
                    // only set relax if end is set, otherwise persistent gaze
                    tpRelax = new OffsetPeg(endPeg, -relaxDuration);
                    setTimePeg("relax", tpRelax);
                }
            }
            gmu.setDurations(readyDuration, relaxDuration);
        }
        catch (MUPlayException ex)
        {
            throw new TMUPlayException(ex.getLocalizedMessage(), this, ex);
        }
    }

    @Override
    protected void playUnit(double time) throws TimedPlanUnitPlayException
    {
        if (time > getRelaxTime() && !isSubUnit())
        {
            relaxUnit.play(time);
        }
        else
        {
            super.playUnit(time);
        }
    }

    @Override
    protected void relaxUnit(double time) throws TimedPlanUnitPlayException
    {
        if (isSubUnit()) return;
        TimePeg relaxPeg = getTimePeg("relax");
        TimePeg endPeg = getTimePeg("end");
        double retractionDuration = aniPlayer.getGazeTransitionToRestDuration();
        if (pegBoard.getPegKeys(endPeg).size() == 1 && !endPeg.isAbsoluteTime())
        {
            endPeg.setGlobalValue(relaxPeg.getGlobalValue() + retractionDuration);
        }
        try
        {
            relaxUnit = aniPlayer.getRestGaze().createTransitionToRest(NullFeedbackManager.getInstance(), relaxPeg, endPeg, getBMLId(),
                    getId(), bmlBlockPeg, pegBoard);
        }
        catch (TMUSetupException e)
        {
            throw new TimedPlanUnitPlayException("TMUSetupException in construction of relax unit", this, e);
        }
        relaxUnit.setTimePeg("relax", new OffsetPeg(endPeg,1));
        relaxUnit.setTimePeg("end", new OffsetPeg(endPeg,2));        
        relaxUnit.setSubUnit(true);
        relaxUnit.start(time);
        super.relaxUnit(time);
    }

    protected void gracefullInterrupt(double time) throws TimedPlanUnitPlayException
    {
        
        skipPegs(time, "ready", "strokeStart", "stroke", "strokeEnd");

        // XXX: should relax and end pegs also be detached if other behaviors are connected to them?
        getTimePeg("relax").setGlobalValue(time);
        getTimePeg("end").setGlobalValue(time + 1); // for now duration 1, should be dynamically gotten from the rest gaze pos
    }
}
