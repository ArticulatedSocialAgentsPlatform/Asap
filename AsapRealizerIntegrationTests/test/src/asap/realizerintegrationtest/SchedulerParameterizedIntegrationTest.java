/*******************************************************************************
 *******************************************************************************/
package asap.realizerintegrationtest;

import hmi.animation.VJoint;
import hmi.audioenvironment.LWJGLJoalSoundManager;
import hmi.audioenvironment.SoundManager;
import hmi.physics.mixed.MixedSystem;
import hmi.physics.ode.OdeHumanoid;
import hmi.testutil.LabelledParameterized;
import hmi.testutil.animation.HanimBody;
import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.util.OS;
import hmi.util.Resources;
import hmi.util.SystemClock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.odejava.Odejava;

import saiba.bml.core.BMLBehaviorAttributeExtension;
import saiba.bml.parser.BMLParser;
import asap.animationengine.AnimationPlanPlayer;
import asap.animationengine.AnimationPlanner;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.gaze.ForwardRestGaze;
import asap.animationengine.gaze.GazeInfluence;
import asap.animationengine.gaze.RestGaze;
import asap.animationengine.gesturebinding.GestureBinding;
import asap.animationengine.gesturebinding.HnsHandshape;
import asap.animationengine.gesturebinding.SpeechBinding;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.restpose.SkeletonPoseRestPose;
import asap.audioengine.AudioPlanner;
import asap.audioengine.TimedAbstractAudioUnit;
import asap.bml.ext.bmla.BMLABMLBehaviorAttributes;
import asap.hns.Hns;
import asap.marytts5binding.MaryTTSBindingFactory;
import asap.realizer.AsapRealizer;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Player;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.parametervaluechange.ParameterValueChangePlanner;
import asap.realizer.parametervaluechange.TimedParameterValueChangeUnit;
import asap.realizer.parametervaluechange.TrajectoryBinding;
import asap.realizer.planunit.DefaultTimedPlanUnitPlayer;
import asap.realizer.planunit.MultiThreadedPlanPlayer;
import asap.realizer.planunit.PlanManager;
import asap.realizer.planunit.SingleThreadedPlanPlayer;
import asap.realizer.scheduler.BMLBlockManager;
import asap.realizer.wait.TimedWaitUnit;
import asap.realizer.wait.WaitPlanner;
import asap.realizerintegrationtests.SchedulerIntegrationTestCases;
import asap.sapittsbinding.SAPITTSBindingFactory;
import asap.speechengine.DirectTTSUnitFactory;
import asap.speechengine.TTSPlanner;
import asap.speechengine.TimedTTSUnit;
import asap.speechengine.TimedTTSUnitFactory;
import asap.speechengine.WavTTSUnitFactory;
import asap.speechengine.ttsbinding.TTSBindingFactory;
import asap.textengine.StdoutTextOutput;
import asap.textengine.TextPlanner;
import asap.textengine.TimedSpeechTextUnit;

import com.google.common.collect.ImmutableSet;

interface SpeechEngineFactory
{
    Engine createEngine(FeedbackManager bfm, BMLBlockManager bbm);

    String getType();
}

class TextEngineFactory implements SpeechEngineFactory
{
    @Override
    public Engine createEngine(FeedbackManager bfm, BMLBlockManager bbm)
    {
        PlanManager<TimedSpeechTextUnit> planManager = new PlanManager<TimedSpeechTextUnit>();
        Player player = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedSpeechTextUnit>(bfm, planManager));
        TextPlanner planner = new TextPlanner(bfm, new StdoutTextOutput(), planManager);
        return new DefaultEngine<TimedSpeechTextUnit>(planner, player, planManager);
    }

    @Override
    public String getType()
    {
        return "TextPlanner";
    }
}

class TTSEngineFactory implements SpeechEngineFactory
{
    private final TimedTTSUnitFactory ttsUnitFactory;

    private final TTSBindingFactory ttsBindFac;

    public TTSEngineFactory(TimedTTSUnitFactory ttsUFac, TTSBindingFactory bindingFac, SoundManager soundManager)
    {
        ttsUnitFactory = ttsUFac;
        ttsBindFac = bindingFac;
    }

    @Override
    public Engine createEngine(FeedbackManager bfm, BMLBlockManager bbm)
    {
        PlanManager<TimedTTSUnit> planManager = new PlanManager<TimedTTSUnit>();
        Player player = new DefaultPlayer(new MultiThreadedPlanPlayer<TimedTTSUnit>(bfm, planManager));
        TTSPlanner planner = new TTSPlanner(bfm, ttsUnitFactory, ttsBindFac.createBinding(), planManager);
        return new DefaultEngine<TimedTTSUnit>(planner, player, planManager);
    }

    @Override
    public String getType()
    {
        return ttsUnitFactory.getClass().getName() + ", " + ttsBindFac.getClass().getName();
    }

}

/**
 * Parameterized version of Elckerlyc's scheduler, tests it in combination with all permutations of
 * TTSGenerators, VerbalPlanners and TTSUnitFactories.
 */
@RunWith(LabelledParameterized.class)
public class SchedulerParameterizedIntegrationTest extends SchedulerIntegrationTestCases
{
    static BMLBlockManager bbm = new BMLBlockManager();
    static FeedbackManager bfm = new FeedbackManagerImpl(bbm, "character1");
    protected static final SoundManager soundManager = new LWJGLJoalSoundManager();

    @Before
    public void setup()
    {

    }

    @BeforeClass
    public static void oneTimeSetUp()
    {
        soundManager.init();
        Odejava.init();
    }

    @AfterClass
    public static void oneTimeCleanup()
    {
        Odejava.close();
        soundManager.shutdown();
    }

    @Parameters
    public static Collection<Object[]> configs() throws Exception
    {
        bbm = new BMLBlockManager();
        bfm = new FeedbackManagerImpl(bbm, "character1");

        ArrayList<SpeechEngineFactory> speechEngineFactories = new ArrayList<SpeechEngineFactory>();

        if (OS.equalsOS(OS.WINDOWS))
        {
            speechEngineFactories.add(new TTSEngineFactory(new DirectTTSUnitFactory(bfm), new SAPITTSBindingFactory(), soundManager));
            speechEngineFactories.add(new TTSEngineFactory(new WavTTSUnitFactory(bfm, soundManager), new SAPITTSBindingFactory(),
                    soundManager));
        }

        speechEngineFactories.add(new TTSEngineFactory(new WavTTSUnitFactory(bfm, soundManager), new MaryTTSBindingFactory(
                new NullPhonemeToVisemeMapping()), soundManager));
        speechEngineFactories.add(new TextEngineFactory());

        Collection<Object[]> objs = new ArrayList<Object[]>();

        for (SpeechEngineFactory sp : speechEngineFactories)
        {
            Object obj[] = new Object[2];

            obj[0] = "SpeechPlanner = " + sp.getType();
            obj[1] = sp;
            objs.add(obj);
        }
        return objs;
    }

    public SchedulerParameterizedIntegrationTest(String label, SpeechEngineFactory sv) throws IOException
    {
        Engine speechEngine = sv.createEngine(bfm, bbm);
        VJoint human = HanimBody.getLOA1HanimBody();

        ArrayList<MixedSystem> m = new ArrayList<MixedSystem>();
        float g[] = { 0, 0, 0 };
        OdeHumanoid p = new OdeHumanoid("phuman", null, null);
        m.add(new MixedSystem(g, p));

        Resources gres = new Resources("");
        GestureBinding gestureBinding = new GestureBinding(gres, bfm);
        gestureBinding.readXML(gres.getReader("Humanoids/shared/gesturebinding/gesturebinding.xml"));

        SpeechBinding speechBinding = new SpeechBinding(gres);
        speechBinding.readXML(gres.getReader("Humanoids/shared/speechbinding/disneyspeechbinding.xml"));

        PlanManager<TimedAnimationUnit> animationPlanManager = new PlanManager<>();

        RestPose pose = new SkeletonPoseRestPose();
        RestGaze gaze = new ForwardRestGaze(GazeInfluence.WAIST);
        AnimationPlanPlayer animationPlanPlayer = new AnimationPlanPlayer(pose, gaze, bfm, animationPlanManager,
                new DefaultTimedPlanUnitPlayer(), pegBoard);
        AnimationPlayer aPlayer = new AnimationPlayer(human, human, human, m, 0.001f, animationPlanPlayer);
        pose.setAnimationPlayer(aPlayer);
        gaze.setAnimationPlayer(aPlayer);

        Hns hns = new Hns();
        hns.readXML(gres.getReader("Humanoids/shared/hns/hns.xml"));
        HnsHandshape HnsHandshape = new HnsHandshape("Humanoids/shared/handshapes");
        AnimationPlanner ap = new AnimationPlanner(bfm, aPlayer, gestureBinding, hns, HnsHandshape, animationPlanManager, pegBoard);
        Engine animationEngine = new DefaultEngine<TimedAnimationUnit>(ap, aPlayer, animationPlanManager);

        SystemClock clock = new SystemClock();
        clock.setMediaSeconds(0.3);

        PlanManager<TimedAbstractAudioUnit> audioPlanManager = new PlanManager<TimedAbstractAudioUnit>();
        Player audioPlayer = new DefaultPlayer(new MultiThreadedPlanPlayer<TimedAbstractAudioUnit>(bfm, audioPlanManager));
        AudioPlanner aup = new AudioPlanner(bfm, new Resources(""), audioPlanManager, soundManager);
        Engine auEngine = new DefaultEngine<TimedAbstractAudioUnit>(aup, audioPlayer, audioPlanManager);

        PlanManager<TimedWaitUnit> waitPlanManager = new PlanManager<TimedWaitUnit>();
        Player waitPlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedWaitUnit>(bfm, waitPlanManager));
        WaitPlanner wp = new WaitPlanner(bfm, waitPlanManager);
        Engine waitEngine = new DefaultEngine<TimedWaitUnit>(wp, waitPlayer, waitPlanManager);

        PlanManager<TimedParameterValueChangeUnit> pvcpPlanManager = new PlanManager<TimedParameterValueChangeUnit>();
        Player pvcpPlayer = new DefaultPlayer(new SingleThreadedPlanPlayer<TimedParameterValueChangeUnit>(bfm, pvcpPlanManager));
        ParameterValueChangePlanner pvcp = new ParameterValueChangePlanner(bfm, new TrajectoryBinding(), pvcpPlanManager);
        Engine pvpcEngine = new DefaultEngine<TimedParameterValueChangeUnit>(pvcp, pvcpPlayer, pvcpPlanManager);

        BMLParser parser = new BMLParser(new ImmutableSet.Builder<Class<? extends BMLBehaviorAttributeExtension>>().add(
                BMLABMLBehaviorAttributes.class).build());

        realizer = new AsapRealizer("avatar1", parser, bfm, clock, bbm, pegBoard, animationEngine, speechEngine, auEngine, waitEngine,
                pvpcEngine);

        setupRealizer();
    }
}
