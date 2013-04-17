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
import asap.incrementalspeechengine.HesitatingSynthesisIUManager;
import asap.incrementalspeechengine.IncrementalTTSUnit;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizerport.util.ListBMLFeedbackListener;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;
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
    private static final double SPEECH_RETIMING_PRECISION = 0.01;
    @Before
    public void setup()
    {
        dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        System.setProperty("mary.base", System.getProperty("shared.project.root")
                + "/asapresource/MARYTTSIncremental/resource/MARYTTSIncremental");
    }

    private IncrementalTTSUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime, String text)
    {
        IncrementalTTSUnit ttsUnit = new IncrementalTTSUnit(bfm, bbPeg, bmlId, id, text, new HesitatingSynthesisIUManager(dispatcher,null),
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
    public void testSyncTimingUpdate() throws ParameterException, TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, 
                "<sync id=\"startS\"/>Hello <sync id=\"s1\"/> world.<sync id=\"endS\"/>");
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        double t1 = ttsUnit.getTime("s1");
        ttsUnit.setFloatParameterValue("stretch",2);
        Thread.sleep(500);
        assertThat(ttsUnit.getTime("s1"), greaterThan(t1));
    }
    
    @Test
    public void testApplyTimeConstraints() throws TimedPlanUnitPlayException, InterruptedException 
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, 
                "Hello <sync id=\"s1\"/> world.");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setTimePeg("start", TimePegUtil.createAbsoluteTimePeg(0));
        ttsUnit.setTimePeg("s1", TimePegUtil.createAbsoluteTimePeg(1));
        ttsUnit.setTimePeg("end", TimePegUtil.createAbsoluteTimePeg(1.25));
        ttsUnit.applyTimeConstraints();
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        ttsUnit.play(0);
        while(dispatcher.isSpeaking());
        ttsUnit.stop(10);
        assertEquals("s1", fbList.get(1).getSyncId());
        assertEquals(1, fbList.get(1).getTime(), SPEECH_RETIMING_PRECISION);        
        assertEquals(1.25, fbList.get(2).getTime(), SPEECH_RETIMING_PRECISION);
        assertEquals(1.25, fbList.get(3).getTime(), SPEECH_RETIMING_PRECISION);
    }
    
    @Test
    public void testApplyTwoTimeConstraints() throws TimedPlanUnitPlayException, InterruptedException 
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, 
                "Hello <sync id=\"s1\"/> world<sync id=\"s2\"/>.");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setTimePeg("start", TimePegUtil.createAbsoluteTimePeg(0));
        ttsUnit.setTimePeg("s1", TimePegUtil.createAbsoluteTimePeg(1));
        ttsUnit.setTimePeg("s2", TimePegUtil.createAbsoluteTimePeg(1.25));
        ttsUnit.setTimePeg("end", TimePegUtil.createAbsoluteTimePeg(1.25));
        ttsUnit.applyTimeConstraints();
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        ttsUnit.play(0);
        while(dispatcher.isSpeaking());
        ttsUnit.stop(10);
        assertEquals("s1", fbList.get(1).getSyncId());
        assertEquals(1, fbList.get(1).getTime(), SPEECH_RETIMING_PRECISION);        
        assertEquals("s2", fbList.get(2).getSyncId());
        assertEquals(1.25, fbList.get(2).getTime(), SPEECH_RETIMING_PRECISION);
    }
    
    @Test
    public void testApplyTimeConstraintsAndStart() throws TimedPlanUnitPlayException, InterruptedException 
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, 
                "Hello <sync id=\"s1\"/> world.");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setTimePeg("start", TimePegUtil.createAbsoluteTimePeg(0.4));
        ttsUnit.setTimePeg("s1", TimePegUtil.createAbsoluteTimePeg(0.7));
        ttsUnit.setTimePeg("end", TimePegUtil.createAbsoluteTimePeg(2));
        ttsUnit.applyTimeConstraints();
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0.4);
        ttsUnit.play(0.4);
        while(dispatcher.isSpeaking());
        ttsUnit.stop(10);
        assertEquals("start", fbList.get(0).getSyncId());
        assertEquals(0.4, fbList.get(0).getTime(), SPEECH_RETIMING_PRECISION);    
        assertEquals("s1", fbList.get(1).getSyncId());
        assertEquals(0.7, fbList.get(1).getTime(), SPEECH_RETIMING_PRECISION);        
        assertEquals("relax", fbList.get(2).getSyncId());
        assertEquals(2, fbList.get(2).getTime(), SPEECH_RETIMING_PRECISION);
        assertEquals("end", fbList.get(3).getSyncId());
        assertEquals(2, fbList.get(3).getTime(), SPEECH_RETIMING_PRECISION);
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
    public void testSyncFeedbackAtEnd() throws TimedPlanUnitPlayException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Hello <sync id=\"s1\"/> world<sync id=\"s2\"/>.");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        ttsUnit.play(0);
        while(dispatcher.isSpeaking());
        ttsUnit.stop(10);        
        assertEquals("start", fbList.get(0).getSyncId());
        assertEquals("s1", fbList.get(1).getSyncId());
        assertEquals("s2", fbList.get(2).getSyncId());
        assertEquals("relax", fbList.get(3).getSyncId());
        assertEquals("end", fbList.get(4).getSyncId());
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
