/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import saiba.bml.core.BMLBlockComposition;
import saiba.bml.core.Behaviour;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.parser.Constraint;
import saiba.bml.parser.SyncPoint;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

/**
 * Improved SmartBodySchedulingStrategy, sorts the behaviors (e.g. on rigidity) before scheduling them.
 * Current scheduling algorithms do not solve all scheduling issues with ordering (see Herwin's thesis for more detail).
 * @author welberge
 */
public class SortedSmartBodySchedulingStrategy implements SchedulingStrategy
{
    private SmartBodySchedulingStrategy strategy;
    private SimpleAfterConstraintSolver afterConstraintSolver = new SimpleAfterConstraintSolver();
    
    public SortedSmartBodySchedulingStrategy(PegBoard pb)
    {
        strategy = new SmartBodySchedulingStrategy(pb);
    }
    
    

    private static class SimpleBehaviourComparator implements Comparator<Behaviour>
    {
        private final BMLScheduler scheduler;
        private Map<Behaviour, Integer> behOrder = new HashMap<Behaviour, Integer>();

        public SimpleBehaviourComparator(List<Behaviour> behs, BMLScheduler s)
        {
            scheduler = s;
            int i = 0;
            for (Behaviour b : behs)
            {
                behOrder.put(b, i);
                i++;
            }
        }

        private int getAbsoluteConstraints(Behaviour b)
        {
            int absconstr = 0;
            for (Constraint c : scheduler.getParser().getConstraints(b.getBmlId(),b.id))
            {
                boolean isHard = false;
                for (SyncPoint s : c.getTargets())
                {
                    if (s.getBehaviourId() == null)
                    {
                        isHard = true;
                    }
                }
                if (isHard)
                {
                    absconstr++;
                }
            }
            return absconstr;
        }

        private boolean nonRigidLoop(Behaviour b1, Behaviour b2)
        {
            boolean nonRigid = true;
            boolean inLoop = false;
            for (List<Behaviour> ugLoops : scheduler.getParser().getUngroundedLoops(b1.getBmlId(), b1.id))
            {
                if(ugLoops.contains(b2))
                {
                    inLoop = true;
                    for(Behaviour beh:ugLoops)
                    {
                        if(scheduler.getRigidity(beh)>=1) nonRigid = false;                            
                    }                        
                }
            }
            return nonRigid&&inLoop;
        }
        
        @Override
        public int compare(Behaviour o1, Behaviour o2)
        {
            int o1AbsConstr = getAbsoluteConstraints(o1);
            int o2AbsConstr = getAbsoluteConstraints(o2);
            double rig1 = scheduler.getRigidity(o1);
            double rig2 = scheduler.getRigidity(o2);

            // If it's rigid and has an absolute constraint, the timing is completely fixed. Schedule first.
            if (rig1 >= 1 && rig2 < 1)
            {
                if (o1AbsConstr > 0) return -1;
            }
            if (rig2 >= 1 && rig1 < 1)
            {
                if (o2AbsConstr > 0) return 1;
            }
            
            // If it's rigid schedule now.
            if (rig1 >= 1 && rig2 < 1)
            {
                if(nonRigidLoop(o1,o2))
                {
                    return -1;
                }
            }
            if (rig2 >= 1 && rig1 < 1)
            {
                if(nonRigidLoop(o2,o1))
                {
                    return 1;
                }                
            }
     
            // schedule last if completely flexible
            if (rig1 <= 0 && rig2 > 0) return 1;
            if (rig2 <= 0 && rig1 > 0) return -1;

            if (behOrder.get(o1) < behOrder.get(o2)) return -1;
            if (behOrder.get(o2) < behOrder.get(o1)) return 1;
            return 0;
        }
    }

    @Override
    public void schedule(BMLBlockComposition mechanism, BehaviourBlock bb, BMLBlockPeg bmlBlockPeg, BMLScheduler scheduler,
            double schedulingTime)
    {
        Collections.sort(bb.behaviours, new SimpleBehaviourComparator(bb.behaviours, scheduler));
        strategy.schedule(mechanism, bb, bmlBlockPeg, scheduler, schedulingTime);
        afterConstraintSolver.scheduleAfterConstraints(bb,scheduler);        
    }
}
