package asap.incrementalttsengine;

import static org.mockito.Mockito.mock;
import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.util.Resources;
import hmi.xml.XMLTokenizer;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;

import java.io.IOException;
import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import saiba.bml.core.SpeechBehaviour;
import asap.incrementalspeechengine.IncrementalTTSPlanner;
import asap.incrementalspeechengine.IncrementalTTSUnit;
import asap.realizer.BehaviourPlanningException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.PlanManager;
import asap.realizertestutil.PlannerTests;

/**
 * unit tests for the IncrementalTTSPlanner
 * @author hvanwelbergen
 *
 */
public class IncrementalTTSPlannerTest
{
    private PlannerTests<IncrementalTTSUnit> plannerTests;
    private FeedbackManager mockBmlFeedbackManager = mock(FeedbackManager.class);
    private static final String BMLID = "bml1";
    private IncrementalTTSPlanner incTTSPlanner;
    private static DispatchStream dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
    
    @AfterClass
    public static void oneTimeCleanup() throws IOException
    {
        dispatcher.close();
    }
    
    private SpeechBehaviour createSpeechBehaviour() throws IOException
    {
        return new SpeechBehaviour(BMLID, new XMLTokenizer(
                "<speech xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"speech1\"><text>Hello world.</text></speech>"));
    }

    @Before
    public void setup()
    {
        System.setProperty("mary.base", System.getProperty("shared.project.root")
                + "/asapresource/MARYTTSIncremental/resource/MARYTTSIncremental");
        incTTSPlanner = new IncrementalTTSPlanner(mockBmlFeedbackManager, new PlanManager<IncrementalTTSUnit>(), dispatcher, new NullPhonemeToVisemeMapping(),
                new HashSet<IncrementalLipSynchProvider>());
        plannerTests = new PlannerTests<IncrementalTTSUnit>(incTTSPlanner, new BMLBlockPeg(BMLID,0.3));
    }

    @Test
    public void testResolveUnsetStart() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveUnsetStart(createSpeechBehaviour());
    }

    @Test(expected = BehaviourPlanningException.class)
    public void testResolveNonExistingSync() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveNonExistingSync(createSpeechBehaviour());
    }
    
    @Test
    public void testResolveStartOffset() throws IOException, BehaviourPlanningException
    {
        plannerTests.testResolveStartOffset(createSpeechBehaviour());
    }
}
