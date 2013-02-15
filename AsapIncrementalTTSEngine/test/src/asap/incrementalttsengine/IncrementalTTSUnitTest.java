package asap.incrementalttsengine;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.util.Resources;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;

import java.io.IOException;
import java.util.ArrayList;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.SpeechBehaviour;
import asap.incrementalspeechengine.IncrementalTTSUnit;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizerport.util.ListBMLFeedbackListener;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
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
    private static final double TIMING_PRECISION = 0.0001;
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
    public void testMultipleSentences() throws TimedPlanUnitPlayException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Sentence one. Sentence two.");
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        ttsUnit.play(0);
        while(dispatcher.isSpeaking());
    }

    @Test
    public void testStartRelaxEndFeedback() throws TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Hello world.");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        ttsUnit.play(0);
        ttsUnit.stop(10);
        assertEquals("start", fbList.get(0).getSyncId());
        assertEquals("relax", fbList.get(1).getSyncId());
        assertEquals("end", fbList.get(2).getSyncId());
    }
    
    @Test
    public void testHasSync() throws TimedPlanUnitPlayException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Hello <sync id=\"s1\"/> world.");
        assertThat(ttsUnit.getAvailableSyncs(),IsIterableContainingInOrder.contains("start","s1","relax","end"));
    }
    
    @Test
    public void testSyncTiming() throws TimedPlanUnitPlayException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, 
                "<sync id=\"startS\"/>Hello <sync id=\"s1\"/> world.<sync id=\"endS\"/>");
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        assertEquals(ttsUnit.getStartTime(), ttsUnit.getTime("startS"),TIMING_PRECISION);
        assertThat(ttsUnit.getTime("s1"), greaterThan(ttsUnit.getStartTime()));
        assertThat(ttsUnit.getTime("s1"), lessThan(ttsUnit.getEndTime()));
        assertEquals(ttsUnit.getEndTime(), ttsUnit.getTime("endS"),TIMING_PRECISION);
    }
    
    @Test
    public void testSyncFeedback() throws TimedPlanUnitPlayException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Hello <sync id=\"s1\"/> world.");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        ttsUnit.play(0);
        while(dispatcher.isSpeaking());
        ttsUnit.stop(10);        
        assertEquals("start", fbList.get(0).getSyncId());
        assertEquals("s1", fbList.get(1).getSyncId());
        assertEquals("relax", fbList.get(2).getSyncId());
        assertEquals("end", fbList.get(3).getSyncId());
    }

    @Test
    public void testSyncRelativeTiming() throws TimedPlanUnitPlayException, SyncPointNotFoundException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, 
                "<sync id=\"startS\"/>Hello <sync id=\"s1\"/> world.<sync id=\"endS\"/>");
        assertEquals(0, ttsUnit.getRelativeTime("startS"), TIMING_PRECISION);
        assertEquals(0, ttsUnit.getRelativeTime("start"), TIMING_PRECISION);
        assertThat(ttsUnit.getRelativeTime("s1"), greaterThan(0d));
        assertThat(ttsUnit.getRelativeTime("s1"), lessThan(1d));
        assertEquals(1, ttsUnit.getRelativeTime("endS"), TIMING_PRECISION);
        assertEquals(1, ttsUnit.getRelativeTime("end"), TIMING_PRECISION);
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
