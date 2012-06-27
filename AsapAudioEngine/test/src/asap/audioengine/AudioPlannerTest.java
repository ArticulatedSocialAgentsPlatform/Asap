package asap.audioengine;

import static org.mockito.Mockito.mock;
import hmi.audioenvironment.ClipSoundManager;
import hmi.util.Resources;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import asap.bml.ext.bmlt.BMLTAudioFileBehaviour;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
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

    private BMLTAudioFileBehaviour createAudioFileBehaviour() throws IOException
    {
        return new BMLTAudioFileBehaviour(BMLID, new XMLTokenizer(
                "<bmlt:audiofile xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"audio1\" fileName=\"audio/audience.wav\"/>"));
    }

    @Before
    public void setup()
    {
        audioPlanner = new AudioPlanner(mockBmlFeedbackManager, new Resources(""), new PlanManager<TimedAbstractAudioUnit>(),new ClipSoundManager());
        plannerTests = new PlannerTests<TimedAbstractAudioUnit>(audioPlanner, new BMLBlockPeg(BMLID,0.3));
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
    
    @Test(expected=BehaviourPlanningException.class)
    public void testNonExistingAudio() throws IOException, BehaviourPlanningException
    {
        String bmlString = "<bmlt:audiofile xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"audio1\" fileName=\"audio/invalid.wav\"/>";
        BMLTAudioFileBehaviour beh = new BMLTAudioFileBehaviour(BMLID, new XMLTokenizer(bmlString));
        ArrayList<TimePegAndConstraint> sacs = new ArrayList<TimePegAndConstraint>();
        audioPlanner.resolveSynchs(BMLBlockPeg.GLOBALPEG, beh, sacs);
    }
}
