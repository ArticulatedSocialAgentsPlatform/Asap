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

import saiba.bml.core.Behaviour;

import java.util.List;
import java.util.Set;

import asap.motionunit.TMUPlayException;
import asap.motionunit.TimedMotionUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.LinearStretchResolver;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizer.scheduler.UniModalResolver;
/**
 * When you do not set an end time peg, 'UNKNOWN' is assumed. This leads to the TimedMotionUnit being timed as
 * starttime..starttime+mu.getpreferredduration() When you do not set a start time peg, the animation cannot be played
 * 
 * @author welberge
 */
public class TimedAnimationUnit extends TimedMotionUnit
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
    public TimedAnimationUnit(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationUnit m, PegBoard pb)
    {
        super(bbf, bmlBlockPeg, bmlId, id, m );
        mu = m;
        pegBoard = pb;
    }

    public TimedAnimationUnit(BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationUnit m, PegBoard pb)
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
