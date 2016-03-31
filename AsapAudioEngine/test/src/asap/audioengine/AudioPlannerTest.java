/*******************************************************************************
 *******************************************************************************/
package asap.audioengine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import hmi.audioenvironment.ClipSoundManager;
import hmi.util.Resources;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.parser.Constraint;
import asap.bml.ext.bmlt.BMLTAudioFileBehaviour;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.SyncAndTimePeg;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.PlanManager;
import asap.realizer.scheduler.TimePegAndConstraint;
import asap.realizertestutil.PlannerTests;

/**
 * Unit test cases for the AudioPlanner
 * @author Herwin
 * 
 */
public class AudioPlannerTest
{
    private static final String BMLID = "bml1";

    private AudioPlanner audioPlanner;
    private FeedbackManager mockBmlFeedbackManager = mock(FeedbackManager.class);
    private PlannerTests<TimedAbstractAudioUnit> plannerTests;
    private static final double RESOLVE_PRECISION = 0.001;

    private BMLTAudioFileBehaviour createAudioFileBehaviour() throws IOException
    {
        return new BMLTAudioFileBehaviour(BMLID, new XMLTokenizer(
                "<bmlt:audiofile xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"audio1\" fileName=\"audio/audience.wav\"/>"));
    }

    @Before
    public void setup()
    {
        audioPlanner = new AudioPlanner(mockBmlFeedbackManager, new Resources(""), new PlanManager<TimedAbstractAudioUnit>(),
                new ClipSoundManager());
        plannerTests = new PlannerTests<TimedAbstractAudioUnit>(audioPlanner, new BMLBlockPeg(BMLID, 0.3));
    }

    @Test
    public void testResolveUnsetStart() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveUnsetStart(createAudioFileBehaviour());
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createAudioFileBehaviour());
    }

    @Test
    public void testResolveStartOffset() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveStartOffset(createAudioFileBehaviour());
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testNonExistingAudio() throws IOException, BehaviourPlanningException
    {
        String bmlString = "<bmlt:audiofile xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"audio1\" fileName=\"audio/invalid.wav\"/>";
        BMLTAudioFileBehaviour beh = new BMLTAudioFileBehaviour(BMLID, new XMLTokenizer(bmlString));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        audioPlanner.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sacs);
    }

    @Test
    public void testAdd() throws IOException, BehaviourPlanningException
    {
        BMLTAudioFileBehaviour beh = createAudioFileBehaviour();
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        TimedAbstractAudioUnit taa = audioPlanner.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sacs);
        List<SyncAndTimePeg> syncAndPegs = audioPlanner.addBehaviour(BMLBlockPeg.GLOBALPEG, beh, sacs, taa);
        assertEquals(2, syncAndPegs.size());
        assertEquals("start", syncAndPegs.get(0).sync);
        assertEquals("end", syncAndPegs.get(1).sync);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(0).peg.getGlobalValue(), RESOLVE_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(1).peg.getGlobalValue(), RESOLVE_PRECISION);
    }
    
    @Test
    public void testAddWithStartConstraint() throws IOException, BehaviourPlanningException
    {
        BMLTAudioFileBehaviour beh = createAudioFileBehaviour();
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        sacs.add(new TimePegAndConstraint("start", new TimePeg(BMLBlockPeg.GLOBALPEG), new Constraint(), 0));
        TimedAbstractAudioUnit taa = audioPlanner.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sacs);
        List<SyncAndTimePeg> syncAndPegs = audioPlanner.addBehaviour(BMLBlockPeg.GLOBALPEG, beh, sacs, taa);
        assertEquals(2, syncAndPegs.size());
        assertEquals("start", syncAndPegs.get(0).sync);
        assertEquals("end", syncAndPegs.get(1).sync);
        assertEquals(0, syncAndPegs.get(0).peg.getGlobalValue(), RESOLVE_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, syncAndPegs.get(1).peg.getGlobalValue(), RESOLVE_PRECISION);
    }
}
