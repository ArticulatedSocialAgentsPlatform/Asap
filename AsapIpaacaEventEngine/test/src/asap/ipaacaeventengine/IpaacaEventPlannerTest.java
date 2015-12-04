package asap.ipaacaeventengine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.parser.Constraint;
import asap.ipaacaeventengine.bml.IpaacaEventBehaviour;
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
 * Unit tests for the IpaacaEventPlanner
 * @author herwinvw
 *
 */
public class IpaacaEventPlannerTest
{
    private IpaacaEventPlanner planner;
    private FeedbackManager mockBmlFeedbackManager = mock(FeedbackManager.class);
    private MessageManager mockMessageManager = mock(MessageManager.class);
    private static final String BMLID = "bml1";
    private PlannerTests<TimedIpaacaMessageUnit> plannerTests;

    @Before
    public void setup()
    {
        planner = new IpaacaEventPlanner(mockBmlFeedbackManager, new PlanManager<TimedIpaacaMessageUnit>(), mockMessageManager);
        plannerTests = new PlannerTests<TimedIpaacaMessageUnit>(planner, new BMLBlockPeg(BMLID, 0.3));
    }
    
    private IpaacaEventBehaviour createEventBehaviour() throws IOException
    {
        return createMessageBehaviour("<ipaaca:ipaacaevent xmlns:ipaaca=\""+IpaacaEventBehaviour.NAMESPACE+"\" id=\"i1\">"
                + "<message category=\"cat1\">"
                + "</message>"
                + "</ipaacaevent>");
    }
    
    private IpaacaEventBehaviour createMessageBehaviour(String bml) throws IOException
    {
        return new IpaacaEventBehaviour(BMLID, new XMLTokenizer(bml));
    }
    
    @Test
    public void testResolveUnsetStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createEventBehaviour());
    }
    
    @Test
    public void testResolveStartOffset() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveStartOffset(createEventBehaviour());
    }
    
    @Test(expected=BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createEventBehaviour());
    }
    
    @Test
    public void testResolve() throws BehaviourPlanningException, IOException
    {
        IpaacaEventBehaviour ipb = createMessageBehaviour(
                "<ipaaca:ipaacaevent xmlns:ipaaca=\""+IpaacaEventBehaviour.NAMESPACE+"\" id=\"a1\">"
                        + "<message category=\"cat1\">"
                        + "</message>" 
                        + "</ipaaca:ipaacaevent>");
        ArrayList<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        sac.add(new TimePegAndConstraint("start", tp, new Constraint(), 0));
        planner.resolveSynchs(BMLBlockPeg.GLOBALPEG, ipb, sac);

        assertEquals(0, tp.getGlobalValue(), 0.0001);
    }
    
    @Test(expected = BehaviourPlanningException.class)
    public void testResolveWithUnknownSyncAndStart() throws IOException, BehaviourPlanningException
    {
        IpaacaEventBehaviour ipb = createMessageBehaviour(
                "<ipaaca:ipaacaevent xmlns:ipaaca=\""+IpaacaEventBehaviour.NAMESPACE+"\" id=\"a1\">"
                        + "<message category=\"cat1\">"
                        + "</message>" 
                        + "</ipaaca:ipaacaevent>");
        ArrayList<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        sac.add(new TimePegAndConstraint("unknown", tp, new Constraint(), 0));
        sac.add(new TimePegAndConstraint("start", TimePegUtil.createTimePeg(0), new Constraint(), 0));
        planner.resolveSynchs(BMLBlockPeg.GLOBALPEG, ipb, sac);
    }
    
    @Test
    public void testAdd() throws BehaviourPlanningException, IOException
    {
        IpaacaEventBehaviour ipb = createMessageBehaviour(
                "<ipaaca:ipaacaevent xmlns:ipaaca=\""+IpaacaEventBehaviour.NAMESPACE+"\" id=\"a1\">"
                        + "<message category=\"cat1\">"
                        + "</message>" 
                        + "</ipaaca:ipaacaevent>");
        ArrayList<TimePegAndConstraint> sac = new ArrayList<TimePegAndConstraint>();
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);

        sac.add(new TimePegAndConstraint("start", tp, new Constraint(), 0));
        TimedIpaacaMessageUnit p = planner.resolveSynchs(BMLBlockPeg.GLOBALPEG, ipb, sac);
        List<SyncAndTimePeg> satp = planner.addBehaviour(BMLBlockPeg.GLOBALPEG, ipb, sac, p);
        assertEquals(1, satp.size());
        assertEquals(tp, satp.get(0).peg);
    }
}
