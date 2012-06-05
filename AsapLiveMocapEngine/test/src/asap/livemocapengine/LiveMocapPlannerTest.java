package asap.livemocapengine;

import static org.mockito.Mockito.mock;

import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import asap.livemocapengine.LiveMocapPlanner;
import asap.livemocapengine.binding.NameTypeBinding;
import asap.livemocapengine.bml.RemoteHeadBehaviour;
import asap.livemocapengine.planunit.LiveMocapTMU;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.PlanManager;
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
    private static final String BMLID = "bml1";
    
    @Before
    public void setup()
    {
        liveMocapPlanner = new LiveMocapPlanner(mockBmlFeedbackManager, new PlanManager<LiveMocapTMU>()
                , mockInputBinding, mockOutputBinding);
        plannerTests = new PlannerTests<LiveMocapTMU>(liveMocapPlanner,new BMLBlockPeg(BMLID,0.3));
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
}
