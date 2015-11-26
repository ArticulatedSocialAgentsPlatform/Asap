/*******************************************************************************
 *******************************************************************************/
package asap.realizer.activate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.parser.Constraint;
import asap.bml.ext.bmla.BMLAActivateBehaviour;
import asap.bml.ext.bmla.BMLAInfo;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.PlannerTests;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Test cases for the ActivatePlanner
 * @author welberge
 *
 */
public class ActivatePlannerTest
{
    private ActivatePlanner activatePlanner;
    private FeedbackManager mockBmlFeedbackManager = mock(FeedbackManager.class);
    private static final String BMLID = "bml1";
    private PlannerTests<TimedActivateUnit> plannerTests;
    
    @Before
    public void setup()
    {
        activatePlanner = new ActivatePlanner(mockBmlFeedbackManager, new PlanManager<TimedActivateUnit>());
        plannerTests = new PlannerTests<TimedActivateUnit>(activatePlanner,new BMLBlockPeg(BMLID,0.3));
    }

    private BMLAActivateBehaviour createActivateBehaviour(String bml) throws IOException
    {
        return new BMLAActivateBehaviour(BMLID, new XMLTokenizer(bml));
    }

    private BMLAActivateBehaviour createActivateBehaviour() throws IOException
    {
        return createActivateBehaviour("<activate xmlns=\""+BMLAInfo.BMLA_NAMESPACE+"\" target=\"bml3\" id=\"i1\"/>");
    }
    
    @Test
    public void testResolveUnsetStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createActivateBehaviour());
    }
    
    @Test
    public void testResolveStartOffset() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveStartOffset(createActivateBehaviour());
    }
    
    @Test(expected=BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createActivateBehaviour());
    }
    
    @Test
    public void testResolve() throws BehaviourPlanningException, IOException
    {
        BMLAActivateBehaviour ipb = createActivateBehaviour(
                "<activate xmlns=\""+BMLAInfo.BMLA_NAMESPACE+"\" target=\"bml3\" id=\"a1\"/>");
        ArrayList<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        sac.add(new TimePegAndConstraint("start", tp, new Constraint(), 0));
        activatePlanner.resolveSynchs(BMLBlockPeg.GLOBALPEG, ipb, sac);

        assertEquals(0, tp.getGlobalValue(), 0.0001);
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testResolveWithUnknownSyncAndStart() throws IOException, BehaviourPlanningException
    {
        BMLAActivateBehaviour ipb = createActivateBehaviour(
                "<activate xmlns=\""+BMLAInfo.BMLA_NAMESPACE+"\" target=\"bml3\" id=\"i1\"/>");
        ArrayList<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        sac.add(new TimePegAndConstraint("unknown", tp, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(0), new Constraint(), 0));
        activatePlanner.resolveSynchs(BMLBlockPeg.GLOBALPEG, ipb, sac);
    }

    @Test
    public void testAdd() throws BehaviourPlanningException, IOException
    {
        BMLAActivateBehaviour ipb = createActivateBehaviour(
                "<activate xmlns=\""+BMLAInfo.BMLA_NAMESPACE+"\" target=\"bml3\" id=\"i1\"/>");
        ArrayList<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);

        sac.add(new TimePegAndConstraint("start", tp, new Constraint(), 0));
        TimedActivateUnit p = activatePlanner.resolveSynchs(BMLBlockPeg.GLOBALPEG, ipb, sac);
        List<SyncAndTimePeg> satp = activatePlanner.addBehaviour(BMLBlockPeg.GLOBALPEG, ipb, sac, p);
        assertEquals(1, satp.size());
        assertEquals(tp, satp.get(0).peg);
    }
}
