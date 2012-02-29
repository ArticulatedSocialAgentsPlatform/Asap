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
package asap.animationengine.pointing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.animationengine.motionunit.MUPlayException;
import asap.animationengine.motionunit.TMUPlayException;
import asap.animationengine.motionunit.TimedMotionUnit;

import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.OffsetPeg;
import hmi.elckerlyc.PegBoard;
import hmi.elckerlyc.TimedPlanUnitPlayException;
import hmi.elckerlyc.TimePeg;
import hmi.elckerlyc.feedback.FeedbackManager;

/**
 * A timed motionunit for pointing
 * @author hvanwelbergen
 */
public class PointingTMU extends TimedMotionUnit
{
    private PointingMU pmu;
    private static Logger logger = LoggerFactory.getLogger(PointingTMU.class.getName());
    
    public PointingTMU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId,String id,PointingMU mu, PegBoard pb)
    {
        super(bfm, bbPeg, bmlId,id,mu, pb);    
        pmu = mu;
    }
    
    @Override
    public void startUnit(double time) throws TimedPlanUnitPlayException
    {
        try
        {
            double readyTime = getTime("ready");
            double relaxTime = getTime("relax");
            double readyDuration;
            double relaxDuration;
            if(readyTime!=TimePeg.VALUE_UNKNOWN)
            {
                readyDuration = readyTime-getStartTime();                    
            }
            else
            {
                //no ready peg, create one
                //TODO: determine readyDuration with Fitts' law
                readyDuration = 1;
                double afterReady = getNextPegTime("ready");
                logger.debug("after ready: {}",afterReady);
                if(afterReady!=TimePeg.VALUE_UNKNOWN)
                {
                    double preparationDur = afterReady-getPrevPegTime("ready");
                    logger.debug("preparationDur {}",preparationDur);
                    if(readyDuration>preparationDur*0.5)
                    {
                        readyDuration = preparationDur*0.5;
                    }
                }
                TimePeg startPeg = getTimePeg("start");
                if(startPeg == null)
                {
                    throw new TMUPlayException("Start peg of pointing tmu does not exist",this);
                }
                else
                {
                    OffsetPeg tpReady = new OffsetPeg(startPeg,readyDuration);
                    setTimePeg("ready", tpReady);
                }
            }
            
            if(relaxTime==TimePeg.VALUE_UNKNOWN)//insert relax time peg
            {
                relaxDuration = readyDuration;
                double retractionDur = getNextPegTime("relax")-getPrevPegTime("relax");
                logger.debug("retractionDur: {}= {} - {}",new Object[]{retractionDur,getNextPegTime("relax"),getPrevPegTime("relax")});
                if(relaxDuration>retractionDur)
                {
                    relaxDuration = retractionDur;
                }
                TimePeg endPeg = getTimePeg("end");
                OffsetPeg tpRelax;
                if(endPeg!=null && getEndTime()!=TimePeg.VALUE_UNKNOWN)
                {
                    //only set relax if end is set, otherwise persistent point
                    tpRelax = new OffsetPeg(endPeg,-relaxDuration);
                    setTimePeg("relax", tpRelax);
                }
            }
            pmu.setStartPose(readyDuration);
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
        pmu.setupRelaxUnit();
    }
}
