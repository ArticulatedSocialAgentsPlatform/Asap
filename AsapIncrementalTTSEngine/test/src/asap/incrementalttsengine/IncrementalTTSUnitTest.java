package asap.incrementalttsengine;

import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.util.Resources;
import inpro.apps.SimpleMonitor;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.incrementalspeechengine.IncrementalTTSUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.lipsync.LipSynchProvider;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.TimedPlanUnit;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizertestutil.planunit.AbstractTimedPlanUnitTest;

/**
 * Testcases for the IncrementalTTSUnit
 * @author hvanwelbergen
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BMLBlockManager.class })
public class IncrementalTTSUnitTest extends AbstractTimedPlanUnitTest
{

    @Override
    protected TimedPlanUnit setupPlanUnit(FeedbackManager bfm, BMLBlockPeg bbPeg, String id, String bmlId, double startTime)
    {
        System.setProperty("mary.base", System.getProperty("shared.project.root")
                + "/asapresource/MARYTTSIncremental/resource/MARYTTSIncremental");
        IncrementalTTSUnit ttsUnit = new IncrementalTTSUnit(bfm, bbPeg, bmlId, id, "Hello world",
                SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml")), new ArrayList<LipSynchProvider>(),
                new NullPhonemeToVisemeMapping());
        ttsUnit.getTimePeg("start").setGlobalValue(startTime);
        return ttsUnit;
    }

    @Test
    @Override
    // no stroke
    public void testSetStrokePeg()
    {

    }
}
