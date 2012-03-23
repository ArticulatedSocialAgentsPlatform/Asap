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
package asap.animationengine.transitions;


import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.NullFeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.TimedPlanUnitPlayException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.animationengine.gaze.GazeMU;
import asap.animationengine.motionunit.TimeAnimationUnit;


/**
 * Creates a transition between the predicted pose at its end and the start pose
 * at the start of the TimedMotionUnit. 
 * @author Herwin
 */
public class TransitionTMU extends TimeAnimationUnit
{
    protected TransitionMU transitionUnit;
    private static Logger logger = LoggerFactory.getLogger(GazeMU.class.getName()); 
    
    public TransitionTMU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId,String behId,TransitionMU m, PegBoard pb)
    {
        super(bfm, bbPeg, bmlId,behId,m, pb);        
        transitionUnit = m;        
    }

    public TransitionTMU(BMLBlockPeg bbPeg, String bmlId,String behId,TransitionMU m, PegBoard pb)
    {
        this(NullFeedbackManager.getInstance(),bbPeg,bmlId,behId,m, pb);        
    }
    
    @Override
    public void startUnit(double t) throws TimedPlanUnitPlayException
    {
        transitionUnit.setStartPose();
        double duration;
        double endTime;
        if(getEndTime()==TimePeg.VALUE_UNKNOWN)
        {
            duration = transitionUnit.getPreferedDuration();
            endTime = getStartTime()+duration;            
        }
        else
        {
            duration = getEndTime() - getStartTime();
            endTime = getEndTime();
        }
        transitionUnit.setEndPose(endTime, duration);
        logger.debug("Setting start and end pose");
    }    
}
