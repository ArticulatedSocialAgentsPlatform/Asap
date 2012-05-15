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

import asap.animationengine.motionunit.*;
import asap.motionunit.MUPlayException;
import asap.motionunit.TMUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import saiba.bml.BMLGestureSync;

/**
 * Timed motion unit for gaze, makes sure the gaze start pose is set at start.
 * @author Herwin
 *
 */
public class GazeTMU extends TimedAnimationUnit
{
    private GazeMU gmu;
    
    public GazeTMU(FeedbackManager bfm,BMLBlockPeg bmlBlockPeg,String bmlId,String id,GazeMU mu, PegBoard pb)
    {
        super(bfm,bmlBlockPeg,bmlId, id,mu,pb);    
        gmu = mu;
    }
    
    @Override
    protected void startUnit(double time) throws TimedPlanUnitPlayException
    {
        try
        {
            double readyTime = getTime("ready");
            double relaxTime = getTime("relax");
            gmu.setStartPose();
            
            double readyDuration;
            double relaxDuration;
            if(readyTime!=TimePeg.VALUE_UNKNOWN)
            {
                readyDuration = readyTime-getStartTime();
            }
            else
            {
                readyDuration = gmu.getReadyDuration();
                
                if(getEndTime()!=TimePeg.VALUE_UNKNOWN && getStartTime()!=TimePeg.VALUE_UNKNOWN )
                {
                    double duration = getEndTime()-getStartTime();
                    if(duration<=readyDuration*2)
                    {
                        readyDuration = duration * 0.5;
                    }
                }
                
                TimePeg startPeg = this.getTimePeg(BMLGestureSync.START.getId());
                if(startPeg == null)
                {
                    throw new TimedPlanUnitPlayException("Start peg is null",this);                    
                }
                OffsetPeg tpReady = new OffsetPeg(startPeg,readyDuration);
                setTimePeg("ready", tpReady);
            }
            
            if(relaxTime!=TimePeg.VALUE_UNKNOWN)
            {
                relaxDuration = readyTime-getStartTime();
            }
            else
            {
                relaxDuration = readyDuration;
                TimePeg endPeg = getTimePeg("end");
                OffsetPeg tpRelax;
                if(endPeg!=null && getEndTime()!=TimePeg.VALUE_UNKNOWN)
                {
                    //only set relax if end is set, otherwise persistent gaze
                    tpRelax = new OffsetPeg(endPeg,-relaxDuration);
                    setTimePeg("relax", tpRelax);
                }
            }
            gmu.setDurations(readyDuration, relaxDuration);
        }
        catch(MUPlayException ex)
        {
            throw new TMUPlayException(ex.getLocalizedMessage(),this,ex);            
        }        
    }   
    
    @Override
    protected void relaxUnit(double time) throws TimedPlanUnitPlayException
    {
        super.relaxUnit(time);
        gmu.setupRelaxUnit();        
    }
    
    
}
