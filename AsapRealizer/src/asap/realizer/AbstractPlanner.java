/*******************************************************************************
 *******************************************************************************/
package asap.realizer;

import java.util.ArrayList;
import java.util.List;

import saiba.bml.core.Behaviour;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.TimePegAndConstraint;


/**
 * Skeleton implementation of a Planner. Handles the feedbackListeners and delegates some
 * functionality to the players.
 * 
 * @author Herwin
 * @param <T> type of TimedPlanUnit the planner manages
 */
public abstract class AbstractPlanner<T extends TimedPlanUnit> implements Planner<T>
{
    protected final PlanManager<T> planManager;
    protected final FeedbackManager fbManager;

    protected TimePegAndConstraint getSac(String sync, List<TimePegAndConstraint> sacs)
    {
        for (TimePegAndConstraint sac : sacs)
        {
            if (sac.syncId.equals(sync))
            {
                return sac;
            }
        }
        return null;
    }
    
    protected TimePegAndConstraint getSacStart(List<TimePegAndConstraint> sacs)
    {
        return getSac("start", sacs);
    }

    protected TimePegAndConstraint getSacEnd(List<TimePegAndConstraint> sacs)
    {
        return getSac("end", sacs);
    }
    
    public AbstractPlanner(FeedbackManager fbm, PlanManager<T> planManager)
    {
        fbManager = fbm;
        this.planManager = planManager;
    } 
    
    public ArrayList<SyncAndTimePeg> constructSyncAndTimePegs(BMLBlockPeg bbPeg, Behaviour b, T bs)
    {
        ArrayList<SyncAndTimePeg> satp = new ArrayList<SyncAndTimePeg>();
        for (String sync : bs.getAvailableSyncs())
        {
            TimePeg p = bs.getTimePeg(sync);
            if (p == null)
            {
                p = new TimePeg(bbPeg);
                bs.setTimePeg(sync,p);
            }
            satp.add(new SyncAndTimePeg(b.getBmlId(), b.id, sync, p));            
        }
        return satp;
    }
    
    protected void setTimePegFromSac(String syncId, TimePegAndConstraint sac, T iu)
    {
        TimePeg tp;
        if(sac.offset==0)
        {
            tp = sac.peg;
        }
        else
        {
            tp = new OffsetPeg(sac.peg,-sac.offset);
        }
        iu.setTimePeg(syncId, tp);
    }
    
    @Override
    public void shutdown()
    {
        
    }
}
