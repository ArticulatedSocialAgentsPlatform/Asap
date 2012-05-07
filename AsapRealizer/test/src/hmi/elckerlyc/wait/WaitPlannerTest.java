package hmi.elckerlyc.wait;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.io.IOException;
import java.util.ArrayList;

import hmi.bml.core.WaitBehaviour;
import hmi.bml.parser.Constraint;
import hmi.elckerlyc.BehaviourPlanningException;
import hmi.elckerlyc.PlannerTests;
import hmi.elckerlyc.feedback.FeedbackManager;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.planunit.PlanManager;
import hmi.elckerlyc.planunit.TimedPlanUnit;
import hmi.elckerlyc.scheduler.TimePegAndConstraint;
import hmi.xml.XMLTokenizer;

import org.junit.Before;
import org.junit.Test;

/**
 * WaitPlanner Unit test cases
 * @author Herwin
 */
public class WaitPlannerTest
{
    private WaitPlanner waitPlanner;
    private FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    private PlannerTests<TimedWaitUnit> plannerTests;
    private static final String BMLID = "bml1";
    private static final float RESOLVE_TIME_PRECISION = 0.0001f;
    @Before
    public void setup()
    {
        waitPlanner = new WaitPlanner(mockFeedbackManager,new PlanManager<TimedWaitUnit>());
        plannerTests = new PlannerTests<TimedWaitUnit>(waitPlanner, new BMLBlockPeg(BMLID,0.3));
    }
    
    public WaitBehaviour createWaitBehaviour(String bml) throws IOException
    {
        return new WaitBehaviour(BMLID,new XMLTokenizer(bml));
    }
    
    public WaitBehaviour createWaitBehaviour() throws IOException
    {
        return createWaitBehaviour("<wait id=\"w1\" max-wait=\"10\"/>");
    }
    
    @Test
    public void testResolveUnsetStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createWaitBehaviour());
    }

    @Test
    public void testResolveStartOffset() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveStartOffset(createWaitBehaviour());
    } 
    
    @Test(expected=BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createWaitBehaviour());
    }
    
    @Test
    public void testResolve1() throws BehaviourPlanningException, IOException
    {
        WaitBehaviour wb = createWaitBehaviour("<wait id=\"w1\" max-wait=\"10\"/>");
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg startPeg = new TimePeg(BMLBlockPeg.GLOBALPEG);
        sacs.add(new TimePegAndConstraint("start", startPeg, new Constraint(), 0));
        
        TimedPlanUnit pu = waitPlanner.resolveSynchs(BMLBlockPeg.GLOBALPEG, wb, sacs);
        
        assertEquals("bml1",pu.getBMLId());
        assertEquals("w1",pu.getId());
        assertEquals(0, pu.getStartTime(),RESOLVE_TIME_PRECISION);
        assertEquals(10, pu.getEndTime(),RESOLVE_TIME_PRECISION);
        assertEquals(0, startPeg.getGlobalValue(),RESOLVE_TIME_PRECISION);
    }
    
    
    @Test
    public void testResolve2() throws BehaviourPlanningException, IOException
    {
        WaitBehaviour wb = createWaitBehaviour("<wait id=\"w1\"/>");
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg startPeg = new TimePeg(BMLBlockPeg.GLOBALPEG);
        sacs.add(new TimePegAndConstraint("start", startPeg, new Constraint(), 0));
        
        TimedPlanUnit pu = waitPlanner.resolveSynchs(BMLBlockPeg.GLOBALPEG, wb, sacs);
        
        assertEquals("bml1",pu.getBMLId());
        assertEquals("w1",pu.getId());
        assertEquals(0, pu.getStartTime(),RESOLVE_TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, pu.getEndTime(),RESOLVE_TIME_PRECISION);
        assertEquals(0, startPeg.getGlobalValue(),RESOLVE_TIME_PRECISION);
    }
    
    @Test
    public void testResolve3() throws BehaviourPlanningException, IOException
    {
        WaitBehaviour wb = createWaitBehaviour("<wait id=\"w1\"/>");
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg startPeg = new TimePeg(BMLBlockPeg.GLOBALPEG);
        startPeg.setGlobalValue(3);
        sacs.add(new TimePegAndConstraint("start", startPeg, new Constraint(), 0));
        TimePeg endPeg = new TimePeg(BMLBlockPeg.GLOBALPEG);
        endPeg.setGlobalValue(5);
        sacs.add(new TimePegAndConstraint("end", endPeg, new Constraint(), 0));
        
        TimedPlanUnit pu = waitPlanner.resolveSynchs(BMLBlockPeg.GLOBALPEG, wb, sacs);
        
        assertEquals("bml1",pu.getBMLId());
        assertEquals("w1",pu.getId());
        assertEquals(3, pu.getStartTime(),RESOLVE_TIME_PRECISION);
        assertEquals(5, pu.getEndTime(),RESOLVE_TIME_PRECISION);
        assertEquals(3, startPeg.getGlobalValue(),RESOLVE_TIME_PRECISION);
        assertEquals(5, endPeg.getGlobalValue(),RESOLVE_TIME_PRECISION);
    }   
}
