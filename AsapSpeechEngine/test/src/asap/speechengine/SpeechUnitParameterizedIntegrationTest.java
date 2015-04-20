/*******************************************************************************
 *******************************************************************************/
package asap.speechengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import hmi.audioenvironment.LWJGLJoalSoundManager;
import hmi.audioenvironment.SoundManager;
import hmi.testutil.LabelledParameterized;
import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.util.OS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import saiba.bml.core.SpeechBehaviour;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.marytts5binding.MaryTTSBindingFactory;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.TimedPlanUnitPlayException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizerport.util.ListBMLFeedbackListener;
import asap.sapittsbinding.SAPITTSBindingFactory;
import asap.speechengine.ttsbinding.TTSBinding;
import asap.speechengine.ttsbinding.TTSBindingFactory;

@RunWith(LabelledParameterized.class)
public class SpeechUnitParameterizedIntegrationTest
{
    private TTSBinding ttsBinding;
    private TimedTTSUnitFactory ttsUnitFactory;
    private static FeedbackManager fbManager = new FeedbackManagerImpl(new BMLBlockManager(), "character1");
    private static SoundManager soundManager = new LWJGLJoalSoundManager();
    private static final double PRECISION = 0.001d;

    public SpeechUnitParameterizedIntegrationTest(String label, TimedTTSUnitFactory fa, TTSBindingFactory bindingFactory)
    {
        ttsBinding = bindingFactory.createBinding();
        ttsUnitFactory = fa;
    }

    @BeforeClass
    public static void beforeClass()
    {
        soundManager.init();
    }

    @AfterClass
    public static void afterClass()
    {
        soundManager.shutdown();
    }

    @Before
    public void setup()
    {

    }

    @After
    public void cleanup()
    {
        ttsBinding.cleanup();
    }

    @Test
    public void testSpeak() throws TimedPlanUnitPlayException, SpeechUnitPlanningException
    {
        TimedTTSUnit ttsUnit = ttsUnitFactory.createTimedTTSUnit(BMLBlockPeg.GLOBALPEG, "Hello world", "voice1", "bml1", "speech1",
                ttsBinding, SpeechBehaviour.class);
        ttsUnit.setup();
        List<BMLSyncPointProgressFeedback> fbList = new ArrayList<BMLSyncPointProgressFeedback>();
        ListBMLFeedbackListener fbl = new ListBMLFeedbackListener.Builder().feedBackList(fbList).build();
        fbManager.addFeedbackListener(fbl);
        TimePeg tpStart = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tpStart.setGlobalValue(0);
        ttsUnit.setStart(tpStart);
        assertEquals(TimePeg.VALUE_UNKNOWN, ttsUnit.getEndTime(), PRECISION);
        assertTrue(ttsUnit.getPreferedDuration() > 0);
        ttsUnit.setState(TimedPlanUnitState.LURKING);
        ttsUnit.start(0);
        assertEquals(TimedPlanUnitState.IN_EXEC, ttsUnit.getState());
        ttsUnit.play(0);
        assertEquals(1, fbList.size());
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
        ttsUnit.play(10);
        assertEquals(TimedPlanUnitState.DONE, ttsUnit.getState());
        // System.out.println(fbList);
        assertEquals(2, fbList.size());
    }

    @Parameters
    public static Collection<Object[]> configs() throws Exception
    {
        Collection<Object[]> objs = new ArrayList<Object[]>();

        List<TimedTTSUnitFactory> ttsFactories = new ArrayList<TimedTTSUnitFactory>();
        ttsFactories.add(new WavTTSUnitFactory(fbManager, soundManager));

        List<TTSBindingFactory> ttsBindingFactories = new ArrayList<TTSBindingFactory>();
        if (OS.equalsOS(OS.WINDOWS))
        {
            Object obj[] = new Object[3];
            obj[0] = "DirectTTSUnitFactory, SAPITTSBinding";
            obj[1] = new DirectTTSUnitFactory(fbManager);
            obj[2] = new SAPITTSBindingFactory();
            objs.add(obj);
            ttsBindingFactories.add(new SAPITTSBindingFactory());
        }

        ttsBindingFactories.add(new MaryTTSBindingFactory(new NullPhonemeToVisemeMapping()));

        for (TimedTTSUnitFactory ttsF : ttsFactories)
        {
            for (TTSBindingFactory bind : ttsBindingFactories)
            {
                Object obj[] = new Object[3];
                obj[0] = ttsF.getClass().getName() + ", " + bind.getClass().getName();
                obj[1] = ttsF;
                obj[2] = bind;
                objs.add(obj);
            }
        }
        return objs;
    }
}
