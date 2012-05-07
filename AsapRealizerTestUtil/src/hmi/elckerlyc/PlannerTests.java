package hmi.elckerlyc;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import hmi.bml.core.Behaviour;
import hmi.bml.parser.Constraint;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.OffsetPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.elckerlyc.util.TimePegUtil;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Delegate for testcases on Planner implementations 
 * @author welberge
 */
public class PlannerTests<T extends TimedPlanUnit>
{
    private final BMLBlockPeg bbPeg;
    private final Planner<T> planner;
    
    public PlannerTests(Planner<T> p, BMLBlockPeg bbPeg)
    {
        this.bbPeg = bbPeg;
        planner = p;
    }
    
    public void testResolveUnsetStart(Behaviour beh) throws BehaviourPlanningException
    {
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg sp = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("start", sp, new Constraint(), 0, false));

        T pu = planner.resolveSynchs(bbPeg, beh, sacs);
        assertEquals(bbPeg.getValue(), sp.getGlobalValue(), 0.0001);

        planner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(bbPeg.getValue(), pu.getStartTime(), 0.0001);
        if(pu.getEndTime()!=TimePeg.VALUE_UNKNOWN)
        {
            assertThat(pu.getEndTime(),greaterThan(bbPeg.getValue()));
        }
    }
    
    public void testResolveNonExistingSync(Behaviour beh) throws IOException, BehaviourPlanningException
    {
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg startPeg = TimePegUtil.createTimePeg(TimePeg.VALUE_UNKNOWN);
        OffsetPeg startOffsetPeg = new OffsetPeg(startPeg,0);
        
        sacs.add(new TimePegAndConstraint("unknown", TimePegUtil.createTimePeg(TimePeg.VALUE_UNKNOWN), new Constraint(), 0, false));
        sacs.add(new TimePegAndConstraint("start",startOffsetPeg , new Constraint(), 0, true));
        
        planner.resolveSynchs(bbPeg, beh, sacs);        
    } 
    
    public void testResolveStartOffset(Behaviour beh) throws IOException, BehaviourPlanningException
    {
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg startPeg = TimePegUtil.createTimePeg(0);
        
        sacs.add(new TimePegAndConstraint("start",startPeg , new Constraint(), -2, true));
        
        TimedPlanUnit pu = planner.resolveSynchs(bbPeg, beh, sacs);    
        assertEquals(2, pu.getStartTime(),0.0001);
    } 
}
