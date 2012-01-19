package asap.planunit;

import hmi.elckerlyc.TimePeg;
import hmi.elckerlyc.planunit.TimedPlanUnit;

import java.util.Comparator;

/**
 * Compares two TimedPlanUnits by priority, then by start time 
 * @author hvanwelbergen
 */
public class PlanUnitPriorityComparator implements Comparator<TimedPlanUnit>
{
    @Override
    public int compare(TimedPlanUnit pu1, TimedPlanUnit pu2)
    {
        if (pu1.getPriority()<pu2.getPriority()) return 1;
        if (pu1.getPriority()>pu2.getPriority()) return -1;
        if(pu1.getStartTime()==TimePeg.VALUE_UNKNOWN || pu1.getStartTime()==TimePeg.VALUE_UNKNOWN) return 0;
        if (pu1.getStartTime()>pu2.getStartTime()) return -1;
        if (pu1.getStartTime()<pu2.getStartTime()) return 1;
        return 0;
    }
}
