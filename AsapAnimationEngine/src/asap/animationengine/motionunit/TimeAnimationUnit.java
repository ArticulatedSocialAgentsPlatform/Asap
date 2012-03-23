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

import hmi.bml.core.Behaviour;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.feedback.NullFeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.PegBoard;
import hmi.elckerlyc.scheduler.LinearStretchResolver;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.elckerlyc.scheduler.UniModalResolver;

import java.util.List;
import java.util.Set;

import asap.motionunit.TMUPlayException;
import asap.motionunit.TimedMotionUnit;
/**
 * When you do not set an end time peg, 'UNKNOWN' is assumed. This leads to the TimedMotionUnit being timed as
 * starttime..starttime+mu.getpreferredduration() When you do not set a start time peg, the animation cannot be played
 * 
 * @author welberge
 */
public class TimeAnimationUnit extends TimedMotionUnit
{
    private final AnimationUnit mu;
    private final UniModalResolver resolver = new LinearStretchResolver();
    protected final PegBoard pegBoard;    
    public Set<String> getKinematicJoints(){return mu.getKinematicJoints();}
    public Set<String> getPhysicalJoints(){return mu.getPhysicalJoints();};
    
    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac)throws BehaviourPlanningException
    {
        resolver.resolveSynchs(bbPeg, b, sac, this);
    }

    /**
     * Constructor
     * @param bmlBlockPeg
     * @param bmlId BML block id
     * @param id behaviour id
     * @param m motion unit
     */
    public TimeAnimationUnit(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationUnit m, PegBoard pb)
    {
        super(bbf, bmlBlockPeg, bmlId, id, m );
        mu = m;
        pegBoard = pb;
    }

    public TimeAnimationUnit(BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationUnit m, PegBoard pb)
    {
        this(NullFeedbackManager.getInstance(), bmlBlockPeg, bmlId, id, m, pb);
    }

    
    public void updateTiming(double time) throws TMUPlayException
    {
        
    }
    

    /**
     * @return the encapsulated motion unit
     */
    public AnimationUnit getMotionUnit()
    {
        return mu;
    }    
}
