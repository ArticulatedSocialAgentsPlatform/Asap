/*******************************************************************************
 *******************************************************************************/
package asap.realizertestutil;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;

import saiba.bml.core.Behaviour;
import saiba.bml.parser.Constraint;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.Planner;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Delegate for testcases on Planner implementations 
 * @author welberge
 * @param <T>
 */
public class PlannerTests<T extends TimedPlanUnit>
{
    private final BMLBlockPeg bbPeg;
    private final Planner<T> planner;
    private final static double TIME_PRECISION = 0.0001;
    
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
        assertEquals(bbPeg.getValue(), sp.getGlobalValue(), TIME_PRECISION);

        planner.addBehaviour(bbPeg, beh, sacs, pu);
        assertEquals(bbPeg.getValue(), pu.getStartTime(), TIME_PRECISION);
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
        assertEquals(2, pu.getStartTime(),TIME_PRECISION);
        assertEquals(startPeg, pu.getTimePeg("start").getLink());
    } 
}
