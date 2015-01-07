/*******************************************************************************
 *******************************************************************************/
package asap.realizer.planunit;


import java.io.Serializable;
import java.util.Comparator;

import asap.realizer.pegboard.TimePeg;

/**
 * Compares two TimedPlanUnits by priority, then by start time
 * @author hvanwelbergen
 */
public class PlanUnitPriorityComparator implements Comparator<TimedPlanUnit>, Serializable
{
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(TimedPlanUnit pu1, TimedPlanUnit pu2)
    {
        if (pu1.getPriority() < pu2.getPriority()) return 1;
        if (pu1.getPriority() > pu2.getPriority()) return -1;
        if (pu1.getStartTime() == TimePeg.VALUE_UNKNOWN || pu1.getStartTime() == TimePeg.VALUE_UNKNOWN) return 0;
        if (pu1.getStartTime() > pu2.getStartTime()) return -1;
        if (pu1.getStartTime() < pu2.getStartTime()) return 1;
        return 0;
    }
}
