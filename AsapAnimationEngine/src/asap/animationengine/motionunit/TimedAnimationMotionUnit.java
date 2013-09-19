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

import java.util.List;
import java.util.Set;

import saiba.bml.core.Behaviour;
import asap.motionunit.TMUPlayException;
import asap.motionunit.TimedMotionUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.Priority;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.scheduler.LinearStretchResolver;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizer.scheduler.UniModalResolver;

/**
 * A TimedAnimationUnit implementation that delegates the motion execution etc to an AnimationUnit
 * 
 * When you do not set an end time peg, 'UNKNOWN' is assumed. This leads to the TimedMotionUnit being timed as
 * starttime..starttime+mu.getpreferredduration() When you do not set a start time peg, the animation cannot be played
 * 
 * @author welberge
 */
public class TimedAnimationMotionUnit extends TimedMotionUnit implements TimedAnimationUnit
{
    private final AnimationUnit mu;
    private final UniModalResolver resolver = new LinearStretchResolver();
    protected final PegBoard pegBoard;

    public Set<String> getKinematicJoints()
    {
        return mu.getKinematicJoints();
    }

    public Set<String> getPhysicalJoints()
    {
        return mu.getPhysicalJoints();
    };

    public void resolveSynchs(BMLBlockPeg bbPeg, Behaviour b, List<TimePegAndConstraint> sac) throws BehaviourPlanningException
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
    public TimedAnimationMotionUnit(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationUnit m, PegBoard pb)
    {
        super(bbf, bmlBlockPeg, bmlId, id, m);
        setPriority(Priority.GESTURE);
        mu = m;
        pegBoard = pb;
    }

    public TimedAnimationMotionUnit(BMLBlockPeg bmlBlockPeg, String bmlId, String id, AnimationUnit m, PegBoard pb)
    {
        this(NullFeedbackManager.getInstance(), bmlBlockPeg, bmlId, id, m, pb);
    }

    public void updateTiming(double time) throws TMUPlayException
    {

    }

    protected void skipPegs(double time, String... pegs)
    {
        for (String peg : pegs)
        {
            if (getTime(peg) > time)
            {
                TimePeg tp = getTimePeg(peg);
                TimePeg tpNew = tp;
                if (pegBoard.getPegKeys(tp).size() > 1)
                {
                    tpNew = new TimePeg(tp.getBmlBlockPeg());
                    pegBoard.addTimePeg(getBMLId(), getId(), peg, tpNew);
                }
                tpNew.setGlobalValue(time - 0.01);
                setTimePeg(peg, tpNew);
            }
        }
    }

    @Override
    public double getPreparationDuration()
    {
        return 0;
    }

    @Override
    public double getRetractionDuration()
    {
        return 0;
    }

    @Override
    public double getStrokeDuration()
    {
        return getPreferedDuration() - getPreparationDuration() - getRetractionDuration();
    }
    
    protected void gracefullInterrupt(double time)throws TimedPlanUnitPlayException
    {
        
    }
    
    @Override
    public void interrupt(double time) throws TimedPlanUnitPlayException
    {
        System.out.println("interrupt "+"at t="+time+" "+getId()+" state: "+this.getState()+" relax time: "+getTime("relax")+" ready time: "+getTime("ready"));
        switch (getState())
        {
        case IN_PREP:
        case PENDING:
        case LURKING:
            stop(time);
            break; // just remove yourself
        case IN_EXEC:
            gracefullInterrupt(time);
            break; // gracefully interrupt yourself
        case SUBSIDING: // nothing to be done
        case DONE:
        default:
            break;
        }
    }
}
