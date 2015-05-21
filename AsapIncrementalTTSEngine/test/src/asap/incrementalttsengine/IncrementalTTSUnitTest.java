/*******************************************************************************
 *******************************************************************************/
package asap.incrementalttsengine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.util.Resources;
import hmi.util.SystemClock;
import inpro.apps.SimpleMonitor;
import inpro.apps.util.MonitorCommandLineParser;
import inpro.audio.DispatchStream;
import inpro.incremental.unit.IU;
import inpro.synthesis.MaryAdapter;
import inpro.synthesis.MaryAdapter5internal;
import inpro.synthesis.hts.IUBasedFullPStream;
import inpro.synthesis.hts.VocodingAudioStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import marytts.util.data.audio.DDSAudioInputStream;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.core.SpeechBehaviour;
import saiba.bml.parser.BMLParser;
import asap.incrementalspeechengine.IncrementalTTSUnit;
import asap.incrementalspeechengine.PhraseIUManager;
import asap.realizer.SyncPointNotFoundException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.NullFeedbackManager;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLASchedulingHandler;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.scheduler.BMLScheduler;
import asap.realizer.scheduler.SortedSmartBodySchedulingStrategy;
import asap.realizerport.util.ListBMLFeedbackListener;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Testcases for the IncrementalTTSUnit
 * @author hvanwelbergen
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class, BMLScheduler.class })
@PowerMockIgnore({ "javax.management.*", "ch.qos.logback.*", "org.slf4j.*" })
public class IncrementalTTSUnitTest extends AbstractTimedPlanUnitTest
{
    private SpeechBehaviour mockSpeechBehaviour = mock(SpeechBehaviour.class);
    private DispatchStream dispatcher;
    private static final double TIMING_PRECISION = 0.01;
    private static final double SPEECH_RETIMING_PRECISION = 0.05; // TODO: more precision
    private PegBoard pegBoard = new PegBoard();
    private BMLScheduler bmlScheduler;
    private SystemClock clock = new SystemClock();

    @Before
    public void setup() throws IOException
    {
        System.setProperty("mary.base", System.getProperty("shared.project.root")
                + "/asapresource/MARYTTSIncremental/resource/MARYTTSIncremental");
        DispatchStream dummydispatcher = SimpleMonitor.setupDispatcher(new MonitorCommandLineParser(new String[] { "-D", "-c",
                "" + new Resources("").getURL("sphinx-config.xml") }));
        List<IU> wordIUs = MaryAdapter.getInstance().text2IUs("Heating up.");
        dummydispatcher.playStream(new DDSAudioInputStream(new VocodingAudioStream(new IUBasedFullPStream(wordIUs.get(0)),
                MaryAdapter5internal.getDefaultHMMData(), true), new AudioFormat(16000.0F, 16, 1, true, false)), true);
        dummydispatcher.waitUntilDone();
        dummydispatcher.close();

        MaryAdapter.getInstance();
        clock.start();
        bmlScheduler = new BMLScheduler("id1", new BMLParser(), NullFeedbackManager.getInstance(), clock, new BMLASchedulingHandler(
                new SortedSmartBodySchedulingStrategy(pegBoard), pegBoard), new BMLBlockManager(), pegBoard);
        dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
    }

    @After
    public void tearDown() throws IOException
    {
        dispatcher.waitUntilDone();
        dispatcher.close();
    }

    private IncrementalTTSUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime, String text)
    {
        IncrementalTTSUnit ttsUnit = new IncrementalTTSUnit(bfm, bbPeg, bmlId, id, text,
                new PhraseIUManager(dispatcher, null, bmlScheduler), new ArrayList<IncrementalLipSynchProvider>(),
                new NullPhonemeToVisemeMapping(), mockSpeechBehaviour);
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
        //IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Sentence one. Sentence two.");
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Sentence one. Sentence two.");
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        ttsUnit.play(0);
        clock.setMediaSeconds(0);
        while (ttsUnit.isPlaying())
        {
            ttsUnit.play(clock.getMediaSeconds());
            Thread.sleep(10);
        }
        assertThat(ttsUnit.getAvailableSyncs(), IsIterableContainingInOrder.contains("start", "relax", "end"));
    }

    @Test
    public void testEmptySpeech() throws TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        clock.setMediaSeconds(0);
        ttsUnit.start(0);
        ttsUnit.play(0);
        ttsUnit.stop(10);        
    }

    @Test
    public void testStartRelaxEndFeedback() throws TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Hello world.");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        clock.setMediaSeconds(0);
        ttsUnit.start(0);
        ttsUnit.play(0);
        while (ttsUnit.isPlaying())
        {
            ttsUnit.play(clock.getMediaSeconds());
            Thread.sleep(10);
        }
    }

    @Test
    public void testHasSync() throws TimedPlanUnitPlayException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Hello <sync id=\"s1\"/> world.");
        assertThat(ttsUnit.getAvailableSyncs(), IsIterableContainingInOrder.contains("start", "s1", "relax", "end"));
    }

    @Test
    public void testSyncTiming() throws TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0,
                "<sync id=\"startS\"/>Hello <sync id=\"s1\"/> world.<sync id=\"endS\"/>");
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        clock.setMediaSeconds(0);
        ttsUnit.start(0);
        while (ttsUnit.isPlaying())
        {
            ttsUnit.play(clock.getMediaSeconds());
            Thread.sleep(10);
        }
        assertThat(ttsUnit.getTime("startS"), greaterThanOrEqualTo(ttsUnit.getTime("start")));
        assertThat(ttsUnit.getTime("s1"), greaterThan(ttsUnit.getTime("startS")));
        assertThat(ttsUnit.getTime("s1"), greaterThan(ttsUnit.getStartTime()));
        assertThat(ttsUnit.getTime("s1"), lessThan(ttsUnit.getEndTime()));
        assertEquals(ttsUnit.getEndTime(), ttsUnit.getTime("endS"), TIMING_PRECISION);
    }

    @Test
    public void testSyncTimingUpdate() throws ParameterException, TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Hello <sync id=\"s1\"/> world.");
        clock.setMediaSeconds(0);
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        double t1 = ttsUnit.getTime("s1");
        ttsUnit.setFloatParameterValue("stretch", 2);
        while (ttsUnit.isPlaying())
        {
            ttsUnit.play(clock.getMediaSeconds());
            Thread.sleep(10);
        }
        assertEquals("s1", fbList.get(1).getSyncId());
        assertThat(fbList.get(1).getTime(), greaterThan(t1));
        assertThat(ttsUnit.getTime("s1"), greaterThan(t1));
    }

    @Test
    public void testApplyTimeConstraints() throws TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0,
                "Hello cruel <sync id=\"s1\"/>world.");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setTimePeg("start", TimePegUtil.createAbsoluteTimePeg(0));
        ttsUnit.setTimePeg("s1", TimePegUtil.createAbsoluteTimePeg(1));
        ttsUnit.setTimePeg("end", TimePegUtil.createAbsoluteTimePeg(1.25));

        ttsUnit.applyTimeConstraints();
        assertEquals(0, ttsUnit.getTime("start"), TIMING_PRECISION);
        assertEquals(1, ttsUnit.getTime("s1"), TIMING_PRECISION);
        assertEquals(1.25, ttsUnit.getTime("end"), TIMING_PRECISION);

        ttsUnit.setState(TimedPlanUnitState.LURKING);
        clock.setMediaSeconds(0);
        ttsUnit.start(0);
        assertEquals(0, ttsUnit.getTime("start"), TIMING_PRECISION);
        assertEquals(1, ttsUnit.getTime("s1"), TIMING_PRECISION);
        assertEquals(1.25, ttsUnit.getTime("end"), TIMING_PRECISION);

        while (ttsUnit.isPlaying())
        {
            ttsUnit.play(clock.getMediaSeconds());
            Thread.sleep(10);
        }
        assertEquals("s1", fbList.get(1).getSyncId());
        assertEquals(0, fbList.get(0).getTime(), SPEECH_RETIMING_PRECISION);
        assertEquals(1, fbList.get(1).getTime(), SPEECH_RETIMING_PRECISION);
        assertEquals(1.25, fbList.get(2).getTime(), SPEECH_RETIMING_PRECISION);
        assertEquals(1.25, fbList.get(3).getTime(), SPEECH_RETIMING_PRECISION);
    }

    @Test
    public void testApplyTwoTimeConstraints() throws TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0,
                "Hello <sync id=\"s1\"/> world<sync id=\"s2\"/>.");
        clock.setMediaSeconds(0);
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setTimePeg("start", TimePegUtil.createAbsoluteTimePeg(0));
        ttsUnit.setTimePeg("s1", TimePegUtil.createAbsoluteTimePeg(1));
        ttsUnit.setTimePeg("s2", TimePegUtil.createAbsoluteTimePeg(1.25));
        ttsUnit.setTimePeg("end", TimePegUtil.createAbsoluteTimePeg(1.25));
        ttsUnit.applyTimeConstraints();
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        while (ttsUnit.isPlaying())
        {
            ttsUnit.play(clock.getMediaSeconds());
            Thread.sleep(10);
        }
        dispatcher.waitUntilDone();
        assertEquals("s1", fbList.get(1).getSyncId());
        assertEquals(1, fbList.get(1).getTime(), SPEECH_RETIMING_PRECISION);
        assertEquals("s2", fbList.get(2).getSyncId());
        assertEquals(1.25, fbList.get(2).getTime(), SPEECH_RETIMING_PRECISION);
    }

    @Test
    public void testApplyTimeConstraintsAndStart() throws TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0.4, "Hello <sync id=\"s1\"/> world.");
        clock.setMediaSeconds(0.4);
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setTimePeg("start", TimePegUtil.createAbsoluteTimePeg(0.4));
        ttsUnit.setTimePeg("s1", TimePegUtil.createAbsoluteTimePeg(0.7));
        ttsUnit.setTimePeg("end", TimePegUtil.createAbsoluteTimePeg(2));
        ttsUnit.applyTimeConstraints();
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0.4);
        ttsUnit.play(0.4);
        while (ttsUnit.isPlaying())
        {
            ttsUnit.play(clock.getMediaSeconds());
            Thread.sleep(10);
        }
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
    public void testSyncFeedback() throws TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0, "Hello <sync id=\"s1\"/> world.");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        clock.setMediaSeconds(0);
        ttsUnit.start(0);
        ttsUnit.play(0);
        while (ttsUnit.isPlaying())
        {
            ttsUnit.play(clock.getMediaSeconds());
            Thread.sleep(10);
        }
        assertEquals("start", fbList.get(0).getSyncId());
        assertEquals("s1", fbList.get(1).getSyncId());
        assertEquals("relax", fbList.get(2).getSyncId());
        assertEquals("end", fbList.get(3).getSyncId());
    }

    @Test
    public void testSyncFeedbackAtEnd() throws TimedPlanUnitPlayException, InterruptedException
    {
        IncrementalTTSUnit ttsUnit = setupPlanUnit(fbManager, BMLBlockPeg.GLOBALPEG, "beh1", "bml1", 0,
                "Hello <sync id=\"s1\"/> world<sync id=\"s2\"/>.");
        fbManager.addFeedbackListener(new ListBMLFeedbackListener.Builder().feedBackList(fbList).build());
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        clock.setMediaSeconds(0);
        ttsUnit.start(0);
        ttsUnit.play(0);
        while (ttsUnit.isPlaying())
        {
            ttsUnit.play(clock.getMediaSeconds());
            Thread.sleep(10);
        }
        assertEquals("start", fbList.get(0).getSyncId());
        assertEquals("Full feedback list:" + fbList, "s1", fbList.get(1).getSyncId());
        assertEquals("Full feedback list:" + fbList, "s2", fbList.get(2).getSyncId());
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
