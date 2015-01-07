/*******************************************************************************
 *******************************************************************************/
package asap.realizerintegrationtestiss;

import hmi.animation.VJoint;
import hmi.audioenvironment.LWJGLJoalSoundManager;
import hmi.audioenvironment.SoundManager;
import hmi.physics.mixed.MixedSystem;
import hmi.physics.ode.OdeHumanoid;
import hmi.testutil.animation.HanimBody;
import hmi.tts.util.NullPhonemeToVisemeMapping;
import hmi.util.Resources;
import hmi.util.SystemClock;
import inpro.apps.SimpleMonitor;
import inpro.audio.DispatchStream;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
import asap.incrementalspeechengine.IncrementalTTSPlanner;
import asap.incrementalspeechengine.IncrementalTTSUnit;
import asap.incrementalspeechengine.PhraseIUManager;
import asap.incrementalspeechengine.SingleThreadedPlanPlayeriSS;
import asap.realizer.AsapRealizer;
import asap.realizer.DefaultEngine;
import asap.realizer.DefaultPlayer;
import asap.realizer.Engine;
import asap.realizer.Player;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.lipsync.IncrementalLipSynchProvider;
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

import com.google.common.collect.ImmutableSet;

public class ScheduleriSSIntegrationTest extends SchedulerIntegrationTestCases
{
    private BMLBlockManager bbm = new BMLBlockManager();
    private FeedbackManager bfm = new FeedbackManagerImpl(bbm, "character1");
    private static final SoundManager soundManager = new LWJGLJoalSoundManager();
    private static DispatchStream dispatcher;

    @BeforeClass
    public static void oneTimeSetUp()
    {
        System.setProperty("mary.base", System.getProperty("shared.project.root")
                + "/asapresource/MARYTTSIncremental/resource/MARYTTSIncremental");
        dispatcher = SimpleMonitor.setupDispatcher(new Resources("").getURL("sphinx-config.xml"));
        soundManager.init();
        Odejava.init();
    }

    @AfterClass
    public static void oneTimeCleanup() throws IOException
    {
        Odejava.close();
        soundManager.shutdown();      
        dispatcher.waitUntilDone();
        dispatcher.close();
    }

    @After
    public void after() throws IOException
    {
        
    }
    
    @Before
    public void before() throws IOException
    {
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

        realizer = new AsapRealizer("avatar1", parser, bfm, clock, bbm, pegBoard, animationEngine, auEngine, waitEngine,
                pvpcEngine);
        
        PlanManager<IncrementalTTSUnit> planManager = new PlanManager<IncrementalTTSUnit>();
        IncrementalTTSPlanner planner = new IncrementalTTSPlanner(bfm, planManager, new PhraseIUManager(dispatcher, "", realizer.getScheduler()),
                new NullPhonemeToVisemeMapping(), new ArrayList<IncrementalLipSynchProvider>());
        Engine speechEngine = new DefaultEngine<IncrementalTTSUnit>(planner, new DefaultPlayer(
                new SingleThreadedPlanPlayeriSS<IncrementalTTSUnit>(planManager)), planManager);
        realizer.addEngine(speechEngine);
        
        setupRealizer();
    }
}
