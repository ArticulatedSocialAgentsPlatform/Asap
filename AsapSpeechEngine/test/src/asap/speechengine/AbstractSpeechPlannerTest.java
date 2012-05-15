package asap.speechengine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import saiba.bml.core.SpeechBehaviour;
import saiba.bml.parser.Constraint;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.Planner;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.PlannerTests;
/**
 * Generic unit test cases and utility functions for different types of SpeechPlanners 
 * 
 * Before tests in sub classes speechPlanner should be initialized.
 * @author welberge
 */
public abstract class AbstractSpeechPlannerTest<T extends TimedAbstractSpeechUnit>
{
    protected PlanManager<T> planManager = new PlanManager<T>();
    protected static final String SPEECHID = "speech1";
    protected static final String BMLID = "bml1";
    protected static final String SPEECHTEXT = "Hello<sync id=\"s1\"/> world";
    protected BMLBlockPeg bbPeg = new BMLBlockPeg(BMLID, 0.3);
    protected Planner<T> speechPlanner;    
    private PlannerTests<T> plannerTests;
    protected FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    
    
    public void setup()
    {
        plannerTests = new PlannerTests<T>(speechPlanner, bbPeg);
    }
    
    protected SpeechBehaviour createSpeechBehaviour(String speechBML, String bmlId) throws IOException
    {
        return new SpeechBehaviour(bmlId, new XMLTokenizer(speechBML));
    }

    protected SpeechBehaviour createSpeechBehaviour(String id, String bmlId, String speech) throws IOException
    {
        return createSpeechBehaviour(String.format("<speech id=\"%s\"><text>%s</text></speech>", id, speech), bmlId);
    }
    
    @Test
    public void testUnknownStart() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveUnsetStart(createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT));
    }

    @Test
    public void testResolveStartOffset() throws BehaviourPlanningException, IOException
    {
        plannerTests.testResolveStartOffset(createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT));
    }
    
    @Test(expected=BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT));
    }

    @Test
    public void testResolveUnknownStartUnknownEndKnownS1() throws BehaviourPlanningException, IOException
    {
        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);

        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg endPeg = new TimePeg(bbPeg);
        sacs.add(new TimePegAndConstraint("end", endPeg, new Constraint(), 0, false));
        TimePeg s1Peg = new TimePeg(bbPeg);
        s1Peg.setGlobalValue(2);
        sacs.add(new TimePegAndConstraint("s1", s1Peg, new Constraint(), 0, false));
        TimePeg startPeg = new OffsetPeg(new TimePeg(bbPeg), 0, bbPeg);
        sacs.add(new TimePegAndConstraint("start", startPeg, new Constraint(), 0, true));

        T pu = speechPlanner.resolveSynchs(bbPeg, beh, sacs);
        assertTrue(startPeg.getGlobalValue() < s1Peg.getGlobalValue());
        assertTrue(endPeg.getGlobalValue() > s1Peg.getGlobalValue());
        assertEquals(2, s1Peg.getGlobalValue(), 0.0001);

        speechPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        assertThat(s1Peg.getGlobalValue(), greaterThan(startPeg.getGlobalValue()));
        assertThat(endPeg.getGlobalValue(), greaterThan(s1Peg.getGlobalValue()));
        assertEquals(2, s1Peg.getGlobalValue(), 0.0001);
    }    
}
