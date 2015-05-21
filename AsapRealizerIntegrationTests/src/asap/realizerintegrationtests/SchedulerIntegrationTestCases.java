/*******************************************************************************
 *******************************************************************************/
package asap.realizerintegrationtests;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import hmi.util.Resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import saiba.bml.core.HeadBehaviour;
import saiba.bml.core.SpeechBehaviour;
import saiba.bml.feedback.BMLWarningFeedback;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.realizer.AsapRealizer;
import asap.realizer.Engine;
import asap.realizer.anticipator.Anticipator;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizerport.util.ListBMLFeedbackListener;

/**
 * Testcases and generic setup for integration tests with the BMLScheduler
 * @author Herwin
 * 
 */
public class SchedulerIntegrationTestCases
{
    private static final double PEGBOARD_PRECISION = 0.0001;
    private static final int SCHEDULE_TIMEOUT = 10000;
    protected PegBoard pegBoard = new PegBoard();

    // test anticipator with one TimePeg 'dummy' at time 1
    public static class DummyAnticipator extends Anticipator
    {
        TimePeg sp;

        public DummyAnticipator(String id, PegBoard pb)
        {
            super(id,pb);
            sp = new TimePeg(BMLBlockPeg.GLOBALPEG);
            sp.setGlobalValue(21);
            addSynchronisationPoint("dummy", sp);
        }
    }

    static
    {
        BMLTInfo.init();
    }
    protected Resources res = new Resources("bmltest");
    protected AsapRealizer realizer;
    protected List<BMLWarningFeedback> warnings = new ArrayList<BMLWarningFeedback>();
    private Set<String> invBeh;

    private void readXML(String file)
    {
        try
        {
            realizer.scheduleBML(res.getReader(file));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setupRealizer()
    {
        realizer.setFeedbackListener(new ListBMLFeedbackListener.Builder().warningList(warnings).build());        
        new DummyAnticipator("dummyanticipator",pegBoard);
    }

    @After
    public void cleanup()
    {
        realizer.shutdown();
    }

    private void assertNoWarnings()
    {
        assertTrue("Unexpected warning(s): " + warnings, 0 == warnings.size());
    }

    private void assertOneWarning()
    {
        assertTrue("Unexpected number of warning(s), there should be only 1: " + warnings, 1 == warnings.size());
    }

    private void assertOneWarningIn(String expectedBmlId)
    {
        assertOneWarning();
        assertEquals(expectedBmlId, warnings.get(0).getId().split(":")[0]);
    }

    private void assertOneWarning(String expectedBmlId)
    {
        assertOneWarning();
        assertEquals(expectedBmlId, warnings.get(0).getId());
    }

    private void assertOneWarning(String expectedBmlId, String expectedBehId)
    {
        assertOneWarning();
        assertEquals(expectedBmlId + ":" + expectedBehId, warnings.get(0).getId());
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void timepegTestSpeechEndTimed()
    {
        // only the end of the speech is constrained, so moving the end should
        // move the start with
        // it
        readXML("testspeech_endtimed.xml");
        pegBoard.getTimePeg("bml1", "speech1", "end").setGlobalValue(6);
        Engine speechEngine = realizer.getEngine(SpeechBehaviour.class);
        invBeh = speechEngine.getInvalidBehaviours();
        assertEquals(0, invBeh.size());
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void timepegTestNod()
    {
        readXML("testnod.xml");
        assertNoWarnings();
        TimePeg nod1Start = pegBoard.getTimePeg("bml1", "nod1", "start");
        nod1Start.setGlobalValue(1);
        Engine aEngine = realizer.getEngine(HeadBehaviour.class);
        invBeh = aEngine.getInvalidBehaviours();
        assertEquals(0, invBeh.size());
        assertEquals(1, nod1Start.getGlobalValue(), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void timepegTestSpeechSyncTimed()
    {
        readXML("testspeech_synctimed.xml");
        assertNoWarnings();
        pegBoard.getTimePeg("bml1", "speech1", "s1").setGlobalValue(15);
        Engine speechEngine = realizer.getEngine(SpeechBehaviour.class);
        speechEngine.updateTiming("bml1");
        invBeh = speechEngine.getInvalidBehaviours();
        assertThat(invBeh, Matchers.<String>empty());        

        pegBoard.getTimePeg("bml1", "speech1", "start").setGlobalValue(15);
        invBeh = speechEngine.getInvalidBehaviours();
        assertTrue(invBeh.size() == 0);
        assertEquals(15, pegBoard.getPegTime("bml1", "speech1", "start"), PEGBOARD_PRECISION);
    }

    @Ignore
    @Test(timeout = SCHEDULE_TIMEOUT)
    public void timepegTestSpeechAndNodSyncTimed()
    {
        readXML("testspeechandnod_synctimed.xml");
        assertNoWarnings();
        double speechStartOrig = pegBoard.getTimePeg("bml1", "speech1", "start").getLocalValue();

        assertTrue(speechStartOrig < 10);

        // TODO: fails because start is linked to nod, so start is not resolved
        // as a OffsetPeg and
        // will not move with speech1:s1
        pegBoard.getTimePeg("bml1", "speech1", "s1").setGlobalValue(15);
        Engine speechEngine = realizer.getEngine(SpeechBehaviour.class);
        invBeh = speechEngine.getInvalidBehaviours();
        assertTrue(invBeh.size() == 0);

        Engine animationEngine = realizer.getEngine(HeadBehaviour.class);
        invBeh = animationEngine.getInvalidBehaviours();
        assertTrue(invBeh.size() == 0);

        double speechStartNew = pegBoard.getTimePeg("bml1", "speech1", "start").getGlobalValue();
        assertTrue(speechStartNew - speechStartOrig == 5);
        // assertTrue(invBeh.get(0).equals("bml1:speech1"));
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testPostureShiftBehaviour()
    {
        readXML("testpostureshift.xml");
        assertNoWarnings();
        assertEquals(0, pegBoard.getRelativePegTime("bml1", "shift1", "start"), PEGBOARD_PRECISION);
        assertEquals(3, pegBoard.getRelativePegTime("bml1", "shift1", "end"), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testGazeShiftBehaviour()
    {
        readXML("testgazeshift.xml");
        assertNoWarnings();
        assertEquals(0, pegBoard.getRelativePegTime("bml1", "shift1", "start"), PEGBOARD_PRECISION);
        assertEquals(3, pegBoard.getRelativePegTime("bml1", "shift1", "end"), PEGBOARD_PRECISION);        
    }
    
    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testMurmlPalmOrientation()
    {
        readXML("murml/murmlpalmorientation.xml");
        assertNoWarnings();
        assertEquals(2, pegBoard.getRelativePegTime("bml1", "gesture1", "start"), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testMurmlRelativePalmOrientation()
    {
        readXML("murml/murmlrelativepalmorientation.xml");
        assertNoWarnings();
        assertEquals(2, pegBoard.getRelativePegTime("bml1", "gesture1", "start"), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testMurmlHandLocation()
    {
        readXML("murml/murmlhandlocation.xml");
        assertNoWarnings();
        assertEquals(0, pegBoard.getRelativePegTime("bml1", "gesture1", "start"), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testMurmlHandLocationStrokeStartSynched()
    {
        readXML("murml/murmlhandlocationstrokestartsynched.xml");
        assertNoWarnings();
        assertEquals(0, pegBoard.getRelativePegTime("bml1", "gesture1", "start"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "speech1", "t1"), pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"),
                PEGBOARD_PRECISION);
        assertThat(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"), greaterThan(0d));
        assertThat(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"),
                greaterThan(pegBoard.getRelativePegTime("bml1", "gesture1", "start")));
        assertEquals(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeEnd"),
                pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testMurmlHandLocationStrokeStartSynchedPostStrokeHold()
    {
        readXML("murml/murmlhandlocationstrokestartsynchedpoststrokehold.xml");
        assertNoWarnings();
        assertEquals(0, pegBoard.getRelativePegTime("bml1", "gesture1", "start"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "speech1", "t1"), pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"),
                PEGBOARD_PRECISION);
        assertThat(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"), greaterThan(0d));
        assertThat(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"),
                greaterThan(pegBoard.getRelativePegTime("bml1", "gesture1", "start")));
        assertEquals(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeEnd") + 2,
                pegBoard.getRelativePegTime("bml1", "gesture1", "relax"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"),
                pegBoard.getRelativePegTime("bml1", "gesture1", "strokeEnd"), PEGBOARD_PRECISION);        
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testMurmlHandLocationStrokeStartAndEndSynched()
    {
        readXML("murml/murmlhandlocationstrokestartsynchedstrokeendsynched.xml");
        assertNoWarnings();
        assertEquals(0, pegBoard.getRelativePegTime("bml1", "gesture1", "start"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "speech1", "t1"), pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"),
                PEGBOARD_PRECISION);
        assertThat(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"), greaterThan(0d));
        assertThat(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"),
                greaterThan(pegBoard.getRelativePegTime("bml1", "gesture1", "start")));
        assertEquals(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeEnd"), pegBoard.getRelativePegTime("bml1", "speech1", "t2"),
                PEGBOARD_PRECISION);
    }
    
    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testMurmlHandLocationStrokeStartAndEndSynchedSeq()
    {
        readXML("murml/murmlhandlocationstrokestartsynchedstrokeendsynchedseq.xml");
        assertNoWarnings();
        assertEquals(0, pegBoard.getRelativePegTime("bml1", "gesture1", "start"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "speech1", "t1"), pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"),
                PEGBOARD_PRECISION);
        assertThat(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"), greaterThan(0d));
        assertThat(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeStart"),
                greaterThan(pegBoard.getRelativePegTime("bml1", "gesture1", "start")));
        assertEquals(pegBoard.getRelativePegTime("bml1", "gesture1", "strokeEnd"), pegBoard.getRelativePegTime("bml1", "speech1", "t2"),
                PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void bmltDummyAnticipatorTest()
    {
        readXML("bmlt/testanticipatedspeech.xml");
        assertNoWarnings();
        assertTrue(pegBoard.getPegTime("bml1", "speech1", "start") == 21);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void bmltDummyAnticipatorTestOffset()
    {
        readXML("bmlt/testanticipatedspeech_offset.xml");
        assertNoWarnings();
        assertTrue(pegBoard.getPegTime("bml1", "speech1", "start") == 22);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testInvalidBmlreference()
    {
        readXML("bmlt/testinvalidbmlref.xml");
        assertOneWarningIn("bml1");
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testInvalidAnticipator()
    {
        readXML("bmlt/testinvalidanticipator.xml");
        assertOneWarning("bml1", "speech1");
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testInvalidAnticipatorSync()
    {
        readXML("bmlt/testinvalidanticipatorsync.xml");
        assertOneWarning("bml1", "speech1");
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void bmltConstraintAnticipatorTest()
    {
        Anticipator antip = new Anticipator("anticipator1", pegBoard);
        TimePeg s1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        s1.setGlobalValue(1);
        antip.addSynchronisationPoint("sync1", s1);
        TimePeg s2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        s2.setGlobalValue(2);
        antip.addSynchronisationPoint("sync2", s2);
        TimePeg s3 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        s3.setGlobalValue(3);
        antip.addSynchronisationPoint("sync3", s3);
        TimePeg s4 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        s4.setGlobalValue(4);
        antip.addSynchronisationPoint("sync4", s4);
        readXML("bmlt/testanticipator.xml");
        assertNoWarnings();

        assertEquals(1, pegBoard.getPegTime("bml1", "nod1", "start"), PEGBOARD_PRECISION);
        assertEquals(2, pegBoard.getPegTime("bml1", "nod1", "end"), PEGBOARD_PRECISION);
        assertEquals(2, pegBoard.getPegTime("bml1", "nod1", "end"), PEGBOARD_PRECISION);
        assertEquals(2, pegBoard.getPegTime("bml1", "nod2", "start"), PEGBOARD_PRECISION);
        assertEquals(3, pegBoard.getPegTime("bml1", "nod2", "end"), PEGBOARD_PRECISION);
        assertEquals(3, pegBoard.getPegTime("bml1", "nod3", "start"), PEGBOARD_PRECISION);
        assertEquals(4, pegBoard.getPegTime("bml1", "nod3", "end"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getTimePeg("bml1", "nod1", "end"), pegBoard.getTimePeg("bml1", "nod2", "start"));
        assertEquals(pegBoard.getTimePeg("bml1", "nod2", "end"), pegBoard.getTimePeg("bml1", "nod3", "start"));

        antip.getSynchronisationPoint("sync2").setGlobalValue(2.2);
        antip.getSynchronisationPoint("sync3").setGlobalValue(3.2);

        assertEquals(2.2, pegBoard.getPegTime("bml1", "nod1", "end"), PEGBOARD_PRECISION);
        assertEquals(2.2, pegBoard.getPegTime("bml1", "nod2", "start"), PEGBOARD_PRECISION);
        assertEquals(3.2, pegBoard.getPegTime("bml1", "nod2", "end"), PEGBOARD_PRECISION);
        assertEquals(3.2, pegBoard.getPegTime("bml1", "nod3", "start"), PEGBOARD_PRECISION);        
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void bmltConstraintTest()
    {
        readXML("bmlt/test_speech_syncs.xml");
        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start"), 2, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml1", "speech1", "tm1"), pegBoard.getPegTime("bml1", "ref1", "strokeEnd"), PEGBOARD_PRECISION);

    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testParameterValueChange()
    {
        readXML("asap/parametervaluechange.xml");
        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start"), 0, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "pvc1", "start"), 0, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "end"),
                pegBoard.getRelativePegTime("bml1", "bml1", "pvc1", "end"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "end"),
                pegBoard.getRelativePegTime("bml1", "bml1", "speech2", "start"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getTimePeg("bml1", "speech1", "end"), pegBoard.getTimePeg("bml1", "speech2", "start"));
        assertEquals(pegBoard.getTimePeg("bml1", "speech1", "end"), pegBoard.getTimePeg("bml1", "pvc1", "end"));
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testBMLTAudio()
    {
        readXML("bmlt/bmltaudio.xml");
        assertNoWarnings();
        assertEquals(0, pegBoard.getRelativePegTime("bml1", "bml1", "audio1", "start"), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testBMLTNoise()
    {
        readXML("bmlt/testnoise.xml");
        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("b", "b", "n", "start"), 0, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("b", "b", "n", "end"), 5000, PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testBMLTTightMergedParamChange()
    {
        readXML("bmlt/testnoise.xml");
        assertNoWarnings();
        readXML("bmlt/testnoisechange.xml");
        assertNoWarnings();
        assertEquals(0, pegBoard.getRelativePegTime("b", "b", "n", "start"), PEGBOARD_PRECISION);
        assertEquals(5000, pegBoard.getRelativePegTime("b", "b", "n", "end"), PEGBOARD_PRECISION);
        assertEquals(16, pegBoard.getRelativePegTime("b", "bml2", "pvc", "start"), PEGBOARD_PRECISION);
        assertEquals(20, pegBoard.getRelativePegTime("b", "bml2", "pvc", "end"), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testWaitDoubleSync()
    {
        readXML("waitdoublesync.xml");
        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start"), 0, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "w1", "start"), 0, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "end"),
                pegBoard.getRelativePegTime("bml1", "bml1", "w1", "end"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "end"),
                pegBoard.getRelativePegTime("bml1", "bml1", "speech2", "start"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getTimePeg("bml1", "speech1", "end"), pegBoard.getTimePeg("bml1", "speech2", "start"));
        assertEquals(pegBoard.getTimePeg("bml1", "speech1", "end"), pegBoard.getTimePeg("bml1", "w1", "end"));
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeech3x()
    {
        readXML("testspeech3x.xml");
        assertNoWarnings();
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "speech10", "start") == 4);
        assertEquals(pegBoard.getPegTime("bml1", "speech11", "start"), pegBoard.getPegTime("bml1", "speech10", "end") + 4,
                PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml1", "speech12", "start"), pegBoard.getPegTime("bml1", "speech11", "end") + 4,
                PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechRelOffsets()
    {
        readXML("testspeechrel_offsets.xml");
        assertNoWarnings();
        double speech1Start = pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start");
        assertTrue(speech1Start == 4);
        assertEquals(pegBoard.getPegTime("bml1", "speech2", "start"), pegBoard.getPegTime("bml1", "speech1", "end") + 5, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "nod2", "start"), speech1Start - 2, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml1", "nod2", "end"), pegBoard.getPegTime("bml1", "speech2", "end") + 3, PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testNodOffset()
    {
        readXML("testnod_offset.xml");
        assertNoWarnings();

        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "start"), 0, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "nod2", "start"), 2, PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testTimeShift()
    {
        readXML("testtimeshift.xml");

        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "start"), 0, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml1", "speech1", "start"), pegBoard.getPegTime("bml1", "nod1", "end"), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testNodInvalidTime()
    {
        readXML("testnod_invalidtime.xml");
        assertOneWarning("bml1", "nod1");

    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechInvalidTime()
    {
        readXML("testspeech_invalidtime.xml");
        assertThat(realizer.getScheduler().getBehaviours("bml1"), Matchers.<String> empty());
        assertOneWarning("bml1", "speech1");
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testNodAbsolute()
    {
        readXML("testnod_absolute.xml");

        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "start"), 0, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "end"), 2, PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testNod2xAbsolute()
    {
        readXML("testnod2x_absolute.xml");
        assertNoWarnings();
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "start") == 1);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod2", "start") == 3);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechEndTimed()
    {
        readXML("testspeech_endtimed.xml");
        assertNoWarnings();
        assertEquals(5, pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "end"), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testNod()
    {
        readXML("testnod.xml");
        assertNoWarnings();
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "start") == 0);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testNodUnknownAttributes()
    {
        readXML("testnod_unknownattributes.xml");
        assertNoWarnings();
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "start") == 0);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechEndTimed2()
    {
        readXML("testspeech_endtimed2.xml");

        assertOneWarning("bml1:speech1");
        assertThat(realizer.getScheduler().getBehaviours("bml1"), Matchers.<String> empty());
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeech2Linked()
    {
        readXML("testspeech_2linked.xml");
        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start"), 0, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml1", "speech2", "start"), pegBoard.getPegTime("bml1", "speech1", "end"), PEGBOARD_PRECISION);
    }

    @Test
    // (timeout = SCHEDULE_TIMEOUT)
    public void testOffset2()
    {
        readXML("testoffset2.xml");
        assertNoWarnings();

        assertEquals(4, pegBoard.getRelativePegTime("bml1", "bml1", "g1", "start"), PEGBOARD_PRECISION);
        assertEquals(3, pegBoard.getRelativePegTime("bml1", "bml1", "g2", "start"), PEGBOARD_PRECISION);
        assertEquals(5, pegBoard.getRelativePegTime("bml1", "bml1", "g2", "end"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml1", "h1", "start"), pegBoard.getPegTime("bml1", "g1", "end"), PEGBOARD_PRECISION);
        assertThat(pegBoard.getRelativePegTime("bml1", "bml1", "h1", "start"), greaterThan(4d));
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testOffset3()
    {
        readXML("testoffset3.xml");
        assertNoWarnings();

        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "g1", "start") == 4);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "g2", "start") == 3);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "g2", "end") == 5);
        assertTrue(pegBoard.getPegTime("bml1", "h1", "start") == pegBoard.getPegTime("bml1", "g1", "end"));
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "h1", "start") > 4);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "gaze1", "start"), 1, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml1", "gaze1", "end"), pegBoard.getPegTime("bml1", "g1", "end") + 3, PEGBOARD_PRECISION);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "gaze1", "end") > 7);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testOffsetChain()
    {
        readXML("testoffsetchain.xml");
        assertNoWarnings();

        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "h1", "start") == 4);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "h2", "start") == 6);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "h3", "start") == 8);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "h4", "start") == 10);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testOffsetChainReversed()
    {
        readXML("testoffsetchainreversed.xml");
        assertNoWarnings();

        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "h1", "start"), 4, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "h2", "start"), 6, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "h3", "start"), 8, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "h4", "start"), 10, PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testEmpty()
    {
        readXML("empty.xml");
        assertNoWarnings();
        assertThat(realizer.getScheduler().getBehaviours("bml1"), Matchers.<String> empty());
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechInvalidTimeSync()
    {
        // TODO: tests really fast speaking (1 word in 0.01 seconds), works fine
        // with speaking to
        // text. Should this really fail??
        readXML("testspeech_invalidtimesync.xml");
        assertOneWarning("bml1", "speech1");
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    @Ignore
    public void testSpeechInvalidTimeSync2()
    {
        // TODO: tests really slow speaking (1 word in 10 seconds), works fine
        // with speaking through
        // text. Should this really fail??
        readXML("testspeech_invalidtimesync2.xml");
        assertNoWarnings();

        Set<String> failedBehaviours = new HashSet<String>();
        failedBehaviours.add("speech1");
        assertOneWarning("bml1", "speech1");
        assertThat(realizer.getScheduler().getBehaviours("bml1"), Matchers.<String> empty());
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechSyncTimed()
    {
        readXML("testspeech_synctimed.xml");
        assertNoWarnings();
        assertEquals(10, pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "s1"), PEGBOARD_PRECISION);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start") < 10);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechSyncTimed2x()
    {
        readXML("testspeech_synctimed2x.xml");
        assertNoWarnings();
        assertEquals(10, pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "s1"),PEGBOARD_PRECISION);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start") < 10);
        assertEquals(20,pegBoard.getRelativePegTime("bml1", "bml1", "speech2", "s1"), PEGBOARD_PRECISION);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "speech2", "start") < 20);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechAndNodSyncTimed()
    {
        readXML("testspeechandnod_synctimed.xml");
        assertNoWarnings();
        assertTrue(pegBoard.getPegTime("bml1", "speech1", "start") == pegBoard.getPegTime("bml1", "nod1", "start"));

        assertEquals(10, pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "s1"),PEGBOARD_PRECISION);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start") < 10);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechSyncAtStart()
    {
        readXML("testspeech_syncatstart.xml");
        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "welkom", "start"), 0, PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechSyncAtStartAndToBeat()
    {
        readXML("testspeech_syncatstartandtobeat.xml");
        assertNoWarnings();
        assertEquals(0, pegBoard.getRelativePegTime("bml1", "bml1", "welkom", "start"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml1", "g1", "start"), pegBoard.getPegTime("bml1", "welkom", "deicticheart1"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml1", "g1", "end"), pegBoard.getPegTime("bml1", "welkom", "deicticheart1") + 2,
                PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechUnknownBehavior()
    {
        readXML("testspeech_unknownbehavior.xml");
        assertOneWarning("bml1");
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechUnknownSync()
    {
        readXML("testspeech_unknownsync.xml");
        assertOneWarningIn("bml1");
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testAbs()
    {
        readXML("testabs.xml");
        assertNoWarnings();

        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "start") == 1);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "g1", "start") == 1);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start") == 1);
        assertTrue(pegBoard.getTimePeg("bml1", "speech1", "start") != pegBoard.getTimePeg("bml1", "g1", "start"));
        assertTrue(pegBoard.getTimePeg("bml1", "speech1", "start") != pegBoard.getTimePeg("bml1", "nod1", "start"));
        assertTrue(pegBoard.getTimePeg("bml1", "g1", "start") != pegBoard.getTimePeg("bml1", "nod1", "start"));
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testOffsetChainNeg()
    {
        readXML("testoffsetchainneg.xml");
        assertNoWarnings();

        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start"), 0, PEGBOARD_PRECISION);
        assertTrue(pegBoard.getPegTime("bml1", "h1", "start") == pegBoard.getPegTime("bml1", "speech1", "s1"));
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "h1", "start") > 0);
        assertTrue(pegBoard.getPegTime("bml1", "h1", "end") == pegBoard.getPegTime("bml1", "h2", "start"));
        assertTrue(pegBoard.getPegTime("bml1", "h2", "start") > pegBoard.getPegTime("bml1", "h1", "start"));
        assertTrue(pegBoard.getPegTime("bml1", "h3", "start") == pegBoard.getPegTime("bml1", "h2", "end"));
        assertTrue(pegBoard.getPegTime("bml1", "h3", "start") > pegBoard.getPegTime("bml1", "h2", "start"));
        assertTrue(pegBoard.getPegTime("bml1", "h4", "start") == pegBoard.getPegTime("bml1", "h3", "end"));
        assertTrue(pegBoard.getPegTime("bml1", "h4", "start") > pegBoard.getPegTime("bml1", "h3", "start"));
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testBMLNoId()
    {
        readXML("bmlnoid.xml");
        assertOneWarning();
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testBeatReadyTimed()
    {
        readXML("testbeatreadytimed.xml");
        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "beat1", "start"), 1, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "beat1", "ready"), 2, PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testOffsetGazeTimed()
    {
        readXML("testoffsetgaze.xml");
        assertNoWarnings();

        assertEquals(pegBoard.getRelativePegTime("bml1", "gaze1", "start"), 2, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "speech1", "start"), pegBoard.getRelativePegTime("bml1", "gaze1", "ready"),
                PEGBOARD_PRECISION);
        assertTrue(pegBoard.getRelativePegTime("bml1", "speech1", "start") > 2);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    @Ignore
    // fails because of smartbody scheduling algorithm
    public void testOffsetGazeTimed2()
    {
        readXML("testoffsetgaze2.xml");
        assertNoWarnings();

        assertEquals(pegBoard.getRelativePegTime("bml1", "gaze1", "start"), 2, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "speech1", "start"), pegBoard.getRelativePegTime("bml1", "gaze1", "ready"),
                PEGBOARD_PRECISION);
        assertTrue(pegBoard.getRelativePegTime("bml1", "speech1", "start") > 2);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testNods()
    {
        readXML("testnods.xml");
        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "start"), 1, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml1", "tilt1", "start"), pegBoard.getPegTime("bml1", "nod1", "end") + 1, PEGBOARD_PRECISION);
        assertTrue(pegBoard.getPegTime("bml1", "nod1", "start") < pegBoard.getPegTime("bml1", "nod1", "end"));
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechNodTimedToSync()
    {
        readXML("testspeech_nodtimedtosync.xml");
        assertNoWarnings();

        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start") == 6);
        assertTrue(pegBoard.getPegTime("bml1", "speech1", "syncstart1") == pegBoard.getPegTime("bml1", "nod1", "start"));
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "end") == 9);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechNodTimedToSyncInverse()
    {
        readXML("testspeech_nodtimedtosyncinverseorder.xml");
        assertNoWarnings();

        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start") == 6);
        assertTrue(pegBoard.getPegTime("bml1", "speech1", "syncstart1") == pegBoard.getPegTime("bml1", "nod1", "start"));
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "end") == 9);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechNodTimedToSyncCapitalization()
    {
        readXML("testspeech_nodtimedtosync_capitalization.xml");
        assertNoWarnings();

        assertTrue(pegBoard
                .getRelativePegTime("BMLWithCapitalizedStuff", "BMLWithCapitalizedStuff", "speech1WithCapitalizedStuff", "start") == 6);
        assertTrue(pegBoard.getPegTime("BMLWithCapitalizedStuff", "speech1WithCapitalizedStuff", "syncStart_1") == pegBoard.getPegTime(
                "BMLWithCapitalizedStuff", "nod1WithCapitalizedStuff", "start"));
        assertTrue(pegBoard.getRelativePegTime("BMLWithCapitalizedStuff", "BMLWithCapitalizedStuff", "nod1WithCapitalizedStuff", "end") == 9);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testNodAndBeat()
    {
        readXML("testnodandbeat.xml");
        assertNoWarnings();
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "start") == 3);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "end") == 5);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "stroke") == pegBoard.getRelativePegTime("bml1", "bml1", "beat1",
                "start"));
    }

    @Ignore
    // smartbody scheduler failure
    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testNodAndBeatSwitchedOrder()
    {
        readXML("testnodandbeat_switchedorder.xml");
        assertNoWarnings();
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "start") == 3);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "end") == 5);
        assertTrue(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "stroke") == pegBoard.getRelativePegTime("bml1", "bml1", "beat1",
                "start"));
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testBeatAndNod()
    {
        readXML("testbeatandnod.xml");
        assertNoWarnings();
        assertEquals(3, pegBoard.getRelativePegTime("bml1", "bml1", "beat1", "start"), PEGBOARD_PRECISION);
        assertEquals(7, pegBoard.getRelativePegTime("bml1", "bml1", "beat1", "end"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "beat1", "strokeEnd"),
                pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "start"), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechNodTimedToSyncOffset()
    {
        readXML("testspeech_nodtimedtosyncoffset.xml");
        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "speech1", "start"), 6, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml1", "speech1", "syncstart1") + 1, pegBoard.getPegTime("bml1", "nod1", "start"),
                PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "nod1", "end"), 9, PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testSpeechGestures()
    {
        readXML("testspeechgestures.xml");

        assertNoWarnings();
        assertEquals(1, pegBoard.getRelativePegTime("bml1", "bml1", "welkom", "deicticheart1"), PEGBOARD_PRECISION);
        assertEquals(2, pegBoard.getRelativePegTime("bml1", "bml1", "transleft", "end"), PEGBOARD_PRECISION);
        assertEquals(1, pegBoard.getRelativePegTime("bml1", "bml1", "g1", "start"), PEGBOARD_PRECISION);

        assertEquals(0, pegBoard.getRelativePegTime("bml1", "bml1", "transleft", "start"), PEGBOARD_PRECISION);
        assertEquals(3, pegBoard.getRelativePegTime("bml1", "bml1", "g1", "end"), PEGBOARD_PRECISION);
        assertEquals(3.5, pegBoard.getRelativePegTime("bml1", "bml1", "relaxleft", "start"), PEGBOARD_PRECISION);
        assertEquals(5.8, pegBoard.getRelativePegTime("bml1", "bml1", "relaxleft", "end"), PEGBOARD_PRECISION);

    }

    @Test//(timeout = SCHEDULE_TIMEOUT)
    public void testGestureAtStart()
    {
        readXML("testspeech_gesturestart.xml");
        assertNoWarnings();
        assertEquals(0, pegBoard.getRelativePegTime("bml2", "bml2", "g1", "start"), PEGBOARD_PRECISION);
        assertEquals(pegBoard.getPegTime("bml2", "speech1", "this"), pegBoard.getPegTime("bml2", "g1", "stroke"), PEGBOARD_PRECISION);
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testInvalidXML()
    {
        readXML("testinvalidxml.xml");
        assertOneWarning();
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testInvalidXML2()
    {
        readXML("testinvalidxml2.xml");
        assertOneWarning();
    }

    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testGazeReadyTimed()
    {
        readXML("testgazereadytimed.xml");
        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "gaze1", "ready"), 3, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "gaze1", "end"), 10, PEGBOARD_PRECISION);
        assertThat(pegBoard.getRelativePegTime("bml1", "bml1", "gaze1", "start"), greaterThan(-PEGBOARD_PRECISION));
        assertThat(pegBoard.getRelativePegTime("bml1", "bml1", "gaze1", "start"), lessThan(3d));
    }

    @Ignore
    // smartbody scheduler failure
    @Test(timeout = SCHEDULE_TIMEOUT)
    public void testOffset()
    {
        readXML("testoffset.xml");
        System.out.println(warnings);
        assertNoWarnings();
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "g1", "start"), 4, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "g2", "start"), 3, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "g2", "end"), 4, PEGBOARD_PRECISION);
        assertEquals(pegBoard.getRelativePegTime("bml1", "bml1", "h1", "start"), pegBoard.getRelativePegTime("bml1", "bml1", "g1", "end"),
                PEGBOARD_PRECISION);
        assertThat(pegBoard.getRelativePegTime("bml1", "bml1", "h1", "start"), greaterThan(4d));
    }
}
