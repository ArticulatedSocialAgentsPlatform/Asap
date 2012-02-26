package asap.realizer;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import hmi.animation.VJoint;
import hmi.audioenvironment.AudioEnvironment;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.TimePeg;
import hmi.elckerlyc.anticipator.Anticipator;
import hmi.elckerlyc.world.WorldObject;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment.RenderStyle;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.realizertester.AbstractASAPRealizerTest;
import asap.utils.Environment;

/**
 * Integration test cases for the AsapRealizer
 * @author hvanwelbergen
 *
 */
public class PersistentFixtureAsapRealizerTest extends AbstractASAPRealizerTest
{
    private static AsapEnvironment staticEnvironment;

    private static final Logger logger = LoggerFactory.getLogger(PersistentFixtureAsapRealizerTest.class.getName());

    private static AsapVirtualHuman vHuman;

    private DummyAnticipator anticipator;

    private static JFrame mainUI = null;
    
    static class DummyAnticipator extends Anticipator
    {
        private TimePeg sp1, sp2;

        public DummyAnticipator(double absoluteTime1, double absoluteTime2)
        {
            sp1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
            sp1.setGlobalValue(absoluteTime1);
            sp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
            sp2.setGlobalValue(absoluteTime2);
            addSynchronisationPoint("dummy1", sp1);
            addSynchronisationPoint("dummy2", sp2);
        }

        public void setTime1(double time)
        {
            setSynchronisationPoint("dummy1", time);

        }

        public void setTime2(double time)
        {
            setSynchronisationPoint("dummy2", time);
        }
    }

    @BeforeClass
    public static void setupEnvironment() throws Exception
    {
        logger.debug("Started setup");
        HmiRenderEnvironment hre = new HmiRenderEnvironment();
        OdePhysicsEnvironment ope = new OdePhysicsEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        staticEnvironment = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("LJWGL_JOAL");
        
        mainUI = new JFrame("Asap BML Realizer Tester");
        mainUI.setSize(1000,600);
        
        hre.init(); //canvas does not exist until init was called
        ope.init();
        aue.init();
        mae.init(ope);
        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(hre);
        environments.add(ope);
        environments.add(mae);
        environments.add(aue);
        environments.add(staticEnvironment);
        
        staticEnvironment.init(environments, ope.getPhysicsSchedulingClock()); //if no physics, just use renderclock here!
        ope.addPrePhysicsCopyListener(staticEnvironment); // this clock method drives the engines in ee. if no physics, then register ee as a listener at the render clock!
        
        java.awt.Component canvas = hre.getAWTComponent(); //after init, get canvas and add to window
        mainUI.add(canvas);
        mainUI.setVisible(true);
        
        
        hre.startRenderClock();
        ope.startPhysicsClock();
        
        //add worldobject "camera" that we can use to look at user :)
        VJoint camera = hre.getCameraTarget(); 
        staticEnvironment.getWorldObjectManager().addWorldObject("camera", new WorldObject(camera));
        
        try
        {
            //vHuman = staticEnvironment.loadVirtualHuman("blueguy", "Humanoids/blueguy", "blueguy_asaploader_mary_hudson.xml", "blueguy - test avatar");
            //vHuman = staticEnvironment.loadVirtualHuman("Humanoids/armandia", "vhloadermaryttsasaprealizertester.xml", "TestAvatar");
            vHuman = staticEnvironment.loadVirtualHuman("armandia", "Humanoids/armandia", "armandia_asaploader_mary_hudson.xml", "TestAvatar");
        }
        catch (IOException ex)
        {
            System.out.println("Cannot load VH");
        }

        hre.loadBox("bluebox", new float[]{0.05f,0.05f,0.05f}, RenderStyle.LINE, 
               new float[]{ 0.2f, 0.2f, 1, 1 },  
               new float[]{ 0.2f, 0.2f, 1, 1 }, 
               new float[]{ 0.2f, 0.2f, 1, 0 }, 
               new float[]{ 0.2f, 0.2f, 1, 1 });
        VJoint boxJoint = hre.getObjectRootJoint("bluebox");
        boxJoint.setTranslation(-0.25f, 1.45f, 0.3f);
        staticEnvironment.getWorldObjectManager().addWorldObject("bluebox", new WorldObject(boxJoint));
        logger.debug("Finished setup");
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
//        logger.debug("Started setup");
//        staticEnvironment = new ElckerlycEnvironment()
//        {
//            @Override
//            protected void initQuickSettings()
//            {
//                frameTitle = "Elckerlyc - HMI BML Realizer Tester";
//                useVsync = true;
//                runphysics = true;
//                // laptop=false;
//                laptop = true;
//                collisionEnabled = false;
//                // debugJOGL = true;
//            }
//
//            public void init()
//            {
//                super.init();
//                initDemoScene();
//            }
//
//            private void initDemoScene()
//            {
//
//                // setup box
//                VGLNode boxNode = new VGLNode("box");
//                GLShape boxShape = new GLShape();
//                BoxGeometry boxGeometry = new BoxGeometry(0.05f, 0.05f, 0.05f);
//                boxShape.addGLGeometry(boxGeometry);
//                GLMaterial boxState = new GLMaterial();
//                /*
//                 * boxState.setDiffuseColor(boxDiffuse);
//                 * boxState.setSpecularColor(boxSpecular);
//                 * boxState.setAmbientColor(boxAmbient);
//                 * boxState.setEmissionColor(boxEmission);
//                 */
//                boxState.setShininess(15);
//                boxShape.addGLState(new GLFill());
//                boxShape.addGLState(new NoTexture2DState());
//                boxShape.addGLState(boxState);
//                boxNode.addGLShape(boxShape);
//                boxNode.getRoot().setTranslation(-0.25f, 1.45f, 0.3f);
//
//                VJoint boxAni = boxNode.getRoot().masterCopyTree("master-");
//                vjWorldRenderRoot.addChild(boxNode.getRoot());
//                vjWorldAnimationRoot.addChild(boxAni);
//                addVisualisation(boxNode);
//                ae.getWorldObjectManager().addWorldObject("bluebox", new WorldObject(boxAni));
//            }
//        };
//        staticEnvironment.init();
//        staticEnvironment.startAll();
//        vHuman = staticEnvironment.loadVirtualHuman("Humanoids/armandia", "vhloadermaryttsasaprealizertester.xml", "TestAvatar");
//
//        logger.debug("Finished setup");
    }

    @Before
    public void setup() throws InterruptedException
    {
        env = staticEnvironment;
        realizerPort = vHuman.getRealizerPort();
        realizerPort.removeAllListeners();
        realizerPort.addListeners(this);
        anticipator = new DummyAnticipator(1000000d, 2000000d);
        vHuman.getElckerlycRealizer().getScheduler().addAnticipator("dummyanticipator", anticipator);
        realizerPort.performBML("<bml id=\"replacesetup\" scheduling=\"replace\"/>");
        waitForBMLEndFeedback("replacesetup");
        clearFeedbackLists();
    }

    @Override
    @Ignore
    public void testTransition()    //transition behaviors are now deprecated
    {
        
    }
    
    @Test
    public void testTemporaryInvalidTimingInAppend() throws InterruptedException, IOException
    {
        String bmlString1 = "<bml id=\"bml1\" xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" bmlt:preplan=\"true\"><speech id=\"sp1\"><text>Hello</text></speech></bml>";
        String bmlString2 = "<bml id=\"bml2\" composition=\"append\"><wait id=\"w1\" start=\"0\" end=\"anticipators:dummyanticipator:dummy1\"/></bml>";
        String bmlString3 = "<bml id=\"bml3\" composition=\"append\"><speech id=\"sp1\"><text>Hello hello hello hello hello hello </text></speech></bml>";
        String bmlString4 = "<bml id=\"bml4\" composition=\"append\"><wait id=\"w1\" start=\"0\" end=\"anticipators:dummyanticipator:dummy2\"/></bml>";
        String bmlString5 = "<bml id=\"bml5\" composition=\"append\"><speech id=\"sp1\"><text>Hello hello hello hello hello hello </text></speech></bml>";
        String bmlString6 = "<bml id=\"bml6\" xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" bmlt:onStart=\"bml1\"></bml>";
        realizerPort.performBML(bmlString1);
        realizerPort.performBML(bmlString2);
        realizerPort.performBML(bmlString3);
        realizerPort.performBML(bmlString4);
        realizerPort.performBML(bmlString5);
        realizerPort.performBML(bmlString6);
        waitForBMLStartFeedback("bml1");

        anticipator.setTime1(getBMLPerformanceStartFeedback("bml1").predictedEnd + 10);
        realizerPort.performBML("<bml id=\"bmlx\"/>");
        waitForBMLStartFeedback("bml3");
        realizerPort.performBML("<bml id=\"bmly\"/>");
        anticipator.setTime2(getBMLPerformanceStartFeedback("bml3").predictedEnd + 10);
        realizerPort.performBML("<bml id=\"bmlz\"/>");
        waitForBMLEndFeedback("bml5");

        assertNoDuplicateFeedbacks();
        assertNoExceptions();
        assertNoWarnings();

    }

    @After
    public void teardownEnvironment() throws InterruptedException
    {
        realizerPort.performBML("<bml id=\"cleanup\" composition=\"replace\"/>");
        waitForBMLEndFeedback("cleanup");
    }

    @AfterClass
    public static void cleanup()
    {
        staticEnvironment.requestShutdown();
        //no need to pull the plug on the JFrame as the application will end now?
        while (!staticEnvironment.isShutdown()){
            logger.debug("wait for shutdown");
            try
            {
                Thread.sleep(1000);
            }
            catch (Exception ex)
            {
            
            }
        }
        
        System.out.println("AsapEnvironment completely shut down.");
        System.out.println("Closing main GUI now.");
        // method to programatically close the frame, from
        // http://stackoverflow.com/questions
        // /1234912/how-to-programmatically-close-a-jframe
        //
        mainUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        WindowEvent wev = new WindowEvent(mainUI, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
    }
}
