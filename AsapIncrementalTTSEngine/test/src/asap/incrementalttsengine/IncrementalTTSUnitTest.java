package asap.incrementalttsengine;

import static org.mockito.Mockito.mock;
import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.util.Resources;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.SpeechBehaviour;
import asap.incrementalspeechengine.IncrementalTTSUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;

/**
 * Testcases for the IncrementalTTSUnit
 * @author hvanwelbergen
 * 
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class })
@PowerMockIgnore({ "javax.management.*", "ch.qos.logback.*", "org.slf4j.*" })
public class IncrementalTTSUnitTest extends AbstractTimedPlanUnitTest
{
    private SpeechBehaviour mockSpeechBehaviour = mock(SpeechBehaviour.class);
    private DispatchStream dispatcher;

    @Before
    public void setup()
    {
        dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        System.setProperty("mary.base", System.getProperty("shared.project.root")
                + "/asapresource/MARYTTSIncremental/resource/MARYTTSIncremental");
    }

    private IncrementalTTSUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime, String text)
    {
        IncrementalTTSUnit ttsUnit = new IncrementalTTSUnit(bfm, bbPeg, bmlId, id, text, dispatcher,
                new ArrayList<IncrementalLipSynchProvider>(), new NullPhonemeToVisemeMapping(), mockSpeechBehaviour);
        ttsUnit.getTimePeg("start").setGlobalValue(startTime);
        return ttsUnit;
    }

    @Override
    protected IncrementalTTSUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        return setupPlanUnit(bfm, bbPeg, id, bmlId, startTime, "Hello world");
    }

    @Test
    public void testMultipleSentences() throws TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Sentence one. Sentence two.");
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        ttsUnit.play(0);
        Thread.sleep(4000);
    }

    @Test
    public void testMultipleSentences2() throws TimedPlanUnitPlayException, InterruptedException
    {
        String text = "Elckerlyc is a BML compliant behavior realizer for generating multimodal verbal and nonverbal behavior for Virtual Humans (VHs). " +
        		"It is designed specifically for continuous (as opposed to turn-based) interaction with tight temporal coordination between the behavior " +
        		"of a VH and its interaction partners. Animation in Elckerlyc is generated using a mix between the precise temporal and " +
        		"spatial control offered by procedural motion and the naturalness of physical simulation.";
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, text);
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        ttsUnit.play(0);
        while(dispatcher.isSpeaking())
        {
            System.out.println(ttsUnit.getEndTime());
        }
    }

    @After
    public void tearDown() throws IOException
    {
        dispatcher.close();
    }

    @Test
    @Override
    // no stroke
    public void testSetStrokePeg()
    {

    }

    @Test
    @Override
    // TODO: currently doesn't work 'cause subsiding and end are changed dynamically
    public void testSubsiding() throws TimedPlanUnitPlayException
    {

    }
}
