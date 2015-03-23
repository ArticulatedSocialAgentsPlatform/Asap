/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import saiba.bml.core.BehaviourBlock;
import saiba.bml.parser.AfterConstraint;

/**
 * Fast solver that can satifisfy after constraints in most BML scripts.
 * @author hvanwelbergen
 */
public class SimpleAfterConstraintSolver
{
    private boolean afterConstraintsSatisfied(BehaviourBlock bb, BMLScheduler scheduler)
    {
        for (AfterConstraint ac : scheduler.getParser().getAfterConstraints())
        {

        }
        return true;
    }

    public void scheduleAfterConstraints(BehaviourBlock bb, BMLScheduler scheduler)
    {
        if (afterConstraintsSatisfied(bb, scheduler))
        {
            return;
        }
    }
}
