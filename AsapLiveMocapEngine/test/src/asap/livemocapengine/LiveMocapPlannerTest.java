/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.headandgazeembodiments.EulerHeadEmbodiment;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.parser.Constraint;
import asap.livemocapengine.binding.NameTypeBinding;
import asap.livemocapengine.bml.RemoteHeadBehaviour;
import asap.livemocapengine.inputs.EulerInput;
import asap.livemocapengine.planunit.LiveMocapTMU;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.PlannerTests;
/**
 * Unit tests for the AsapLiveMocapPlanner
 * @author welberge
 */
public class LiveMocapPlannerTest
{
    private LiveMocapPlanner liveMocapPlanner;
    private PlannerTests<LiveMocapTMU> plannerTests;
    private FeedbackManager mockBmlFeedbackManager = mock(FeedbackManager.class); 
    private NameTypeBinding mockInputBinding = mock(NameTypeBinding.class);
    private NameTypeBinding mockOutputBinding = mock(NameTypeBinding.class);
    private EulerInput mockEulerInput = mock(EulerInput.class);
    private EulerHeadEmbodiment mockEmbodiment = mock(EulerHeadEmbodiment.class);
    private static final String BMLID = "bml1";
    private final BMLBlockPeg bbPeg = new BMLBlockPeg("Peg1", 0.3);
    private static final float PLAN_PRECISION = 0.00001f;
    
    @Before
    public void setup()
    {
        liveMocapPlanner = new LiveMocapPlanner(mockBmlFeedbackManager, new PlanManager<LiveMocapTMU>()
                , mockInputBinding, mockOutputBinding);
        plannerTests = new PlannerTests<LiveMocapTMU>(liveMocapPlanner,new BMLBlockPeg(BMLID,0.3));
        when(mockInputBinding.get("input1", EulerInput.class)).thenReturn(mockEulerInput);
        when(mockOutputBinding.get("output1", EulerHeadEmbodiment.class)).thenReturn(mockEmbodiment);
    }
    
    public RemoteHeadBehaviour createRemoteHeadBehaviour() throws IOException
    {
        String str = "<remoteHead xmlns=\"http://asap-project.org/livemocap\" " +
        "id=\"rhead1\" input=\"input1\" output=\"output1\"/>";
        return new RemoteHeadBehaviour(BMLID, new XMLTokenizer(str));
    }
    
    @Test
    public void testResolveUnsetStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createRemoteHeadBehaviour());
    }
    
    @Test
    public void testResolveStartOffset() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveStartOffset(createRemoteHeadBehaviour());
    }
    
    @Test(expected=BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createRemoteHeadBehaviour());
    }
    
    @Test
    public void testAdd() throws IOException, BehaviourPlanningException
    {
        RemoteHeadBehaviour beh = createRemoteHeadBehaviour();
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        LiveMocapTMU lmu = liveMocapPlanner.resolveSynchs(bbPeg, beh, sacs);
        List<SyncAndTimePeg> syncAndPegs = liveMocapPlanner.addBehaviour(BMLBlockPeg.GLOBALPEG, beh, sacs, lmu);
        assertEquals(2, syncAndPegs.size());
        assertEquals("start", syncAndPegs.get(0).sync);
        assertEquals("end", syncAndPegs.get(1).sync);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(0).peg.getGlobalValue(), PLAN_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(1).peg.getGlobalValue(), PLAN_PRECISION);
    }
    
    @Test
    public void testAddWithStartConstraint() throws IOException, BehaviourPlanningException
    {
        RemoteHeadBehaviour beh = createRemoteHeadBehaviour();
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        sacs.add(new TimePegAndConstraint("start", new TimePeg(BMLBlockPeg.GLOBALPEG), new Constraint(), 0));
        LiveMocapTMU lmu = liveMocapPlanner.resolveSynchs(bbPeg, beh, sacs);
        List<SyncAndTimePeg> syncAndPegs = liveMocapPlanner.addBehaviour(BMLBlockPeg.GLOBALPEG, beh, sacs, lmu);
        assertEquals(2, syncAndPegs.size());
        assertEquals("start", syncAndPegs.get(0).sync);
        assertEquals("end", syncAndPegs.get(1).sync);
        assertEquals(0, syncAndPegs.get(0).peg.getGlobalValue(), PLAN_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(1).peg.getGlobalValue(), PLAN_PRECISION);
    }
}
