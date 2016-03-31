/*******************************************************************************
 *******************************************************************************/
package asap.textengine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import saiba.bml.core.SpeechBehaviour;
import saiba.bml.parser.Constraint;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.Planner;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.PlannerTests;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Generic unit test cases and utility functions for different types of SpeechPlanners
 * 
 * Before tests in sub classes speechPlanner should be initialized.
 * @author welberge
 * @param <T>
 */
public abstract class AbstractTextPlannerTest<T extends TimedSpeechTextUnit>
{
    protected static final String SPEECHID = "speech1";
    protected static final String BMLID = "bml1";
    protected static final String SPEECHTEXT = "Hello<sync id=\"s1\"/> world";
    protected BMLBlockPeg bbPeg = new BMLBlockPeg(BMLID, 0.3);
    protected Planner<T> speechPlanner;
    private PlannerTests<T> plannerTests;
    protected FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    private static final double RESOLVE_PRECISION = 0.001;
    
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
        return createSpeechBehaviour(String.format("<speech xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"%s\"><text>%s</text></speech>", id, speech), bmlId);
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

    @Test(expected = BehaviourPlanningException.class)
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
    
    @Test
    public void testResolveEndConstraint() throws IOException, BehaviourPlanningException
    {
        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimePeg endPeg = TimePegUtil.createTimePeg(10);        
        sacs.add(new TimePegAndConstraint("end",endPeg , new Constraint(), 0, true));
        
        T pu = speechPlanner.resolveSynchs(bbPeg, beh, sacs);    
        assertEquals(10, pu.getEndTime(),RESOLVE_PRECISION);
        assertEquals(endPeg, pu.getTimePeg("end"));
    } 
    
    @Test
    public void testAdd() throws IOException, BehaviourPlanningException
    {
        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        T pu = speechPlanner.resolveSynchs(bbPeg, beh, sacs);
        List<SyncAndTimePeg> syncAndPegs = speechPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        
        
        assertEquals(3, syncAndPegs.size());
        assertEquals("start", syncAndPegs.get(0).sync);
        assertEquals("s1", syncAndPegs.get(1).sync);
        assertEquals("end", syncAndPegs.get(2).sync);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(0).peg.getGlobalValue(), RESOLVE_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(1).peg.getGlobalValue(), RESOLVE_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(2).peg.getGlobalValue(), RESOLVE_PRECISION);
    }
    
    @Test
    public void testAddWithStartConstraint() throws IOException, BehaviourPlanningException
    {
        SpeechBehaviour beh = createSpeechBehaviour(SPEECHID, BMLID, SPEECHTEXT);
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        sacs.add(new TimePegAndConstraint("start", new TimePeg(BMLBlockPeg.GLOBALPEG), new Constraint(), 0));
        T pu = speechPlanner.resolveSynchs(bbPeg, beh, sacs);        
        List<SyncAndTimePeg> syncAndPegs = speechPlanner.addBehaviour(bbPeg, beh, sacs, pu);
        
        
        assertEquals(3, syncAndPegs.size());
        assertEquals("start", syncAndPegs.get(0).sync);
        assertEquals("s1", syncAndPegs.get(1).sync);
        assertEquals("end", syncAndPegs.get(2).sync);
        assertEquals(0, syncAndPegs.get(0).peg.getGlobalValue(), RESOLVE_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(1).peg.getGlobalValue(), RESOLVE_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(2).peg.getGlobalValue(), RESOLVE_PRECISION);
    }
}
