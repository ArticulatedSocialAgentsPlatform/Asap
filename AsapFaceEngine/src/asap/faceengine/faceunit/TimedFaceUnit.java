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

import asap.motionunit.TMUPlayException;
import asap.motionunit.TimedMotionUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitPlayException;
/**
 * When you do not set an end time peg, 'UNKNOWN' is assumed. This leads to the faceunit being timed
 * as starttime..starttime+getpreferredduration
 * 
 * When you do not set a start time peg, the animation cannot be played
 * 
 * @author Dennis Reidsma
 */
public class TimedFaceUnit extends TimedMotionUnit
{
    public final FaceUnit fu;
    private volatile boolean handleGracefulInterrupt = false;
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
    public TimedFaceUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, FaceUnit f, PegBoard pb)
    {
        super(bfm, bbPeg, bmlId, id, f, pb);
        fu = f;        
    }

    /**
     * @return the encapsulated face unit
     */
    public FaceUnit getFaceUnit()
    {
        return fu;
    }     
    
    @Override
    public void playUnit(double time) throws TMUPlayException
    {
        if (handleGracefulInterrupt)
        {
            skipPegs(time, "attackPeak");
            getTimePeg("relax").setGlobalValue(time);
            getTimePeg("end").setGlobalValue(time + 0.15);
            handleGracefulInterrupt = false;
        }
        super.playUnit(time);
    }
    
    @Override
    protected void gracefullInterrupt(double time) throws TimedPlanUnitPlayException
    {
        if(!isSubUnit())
        {
            fu.interruptFromHere();
            handleGracefulInterrupt = true; //handle it in the play thread
        }
        else
        {
            stop(time);
        }
    }
}
