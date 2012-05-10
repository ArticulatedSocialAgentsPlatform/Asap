package asap.realizertester;

import hmi.animation.VJoint;
import hmi.audioenvironment.AudioEnvironment;
import hmi.bml.bridge.RealizerPort;
import hmi.bml.ext.bmlt.BMLTInfo;
import hmi.elckerlyc.anticipator.Anticipator;
import hmi.elckerlyc.pegboard.BMLBlockPeg;
import hmi.elckerlyc.pegboard.TimePeg;
import hmi.elckerlyc.world.WorldObject;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment.RenderStyle;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
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
        BMLTInfo.init();
        HmiRenderEnvironment hre = new HmiRenderEnvironment();
        OdePhysicsEnvironment ope = new OdePhysicsEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        staticEnvironment = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("LJWGL_JOAL");

        mainUI = new JFrame("Asap BML Realizer Tester");
        mainUI.setSize(1000, 600);

        hre.init(); // canvas does not exist until init was called
        ope.init();
        aue.init();
        mae.init(ope);
        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(hre);
        environments.add(ope);
        environments.add(mae);
        environments.add(aue);
        environments.add(staticEnvironment);

        staticEnvironment.init(environments, ope.getPhysicsSchedulingClock()); // if no physics, just use renderclock here!
        
        // this clock method drives the engines in ee. if no physics, then register ee as a listener at the render clock!
        ope.addPrePhysicsCopyListener(staticEnvironment); 

        java.awt.Component canvas = hre.getAWTComponent(); // after init, get canvas and add to window
        mainUI.add(canvas);
        mainUI.setVisible(true);

        hre.startRenderClock();
        ope.startPhysicsClock();

        // add worldobject "camera" that we can use to look at user :)
        VJoint camera = hre.getCameraTarget();
        staticEnvironment.getWorldObjectManager().addWorldObject("camera", new WorldObject(camera));

        try
        {
            // vHuman = staticEnvironment.loadVirtualHuman("blueguy", "Humanoids/blueguy",
            //"blueguy_asaploader_mary_hudson.xml", "blueguy - test avatar");
            // vHuman = staticEnvironment.loadVirtualHuman("Humanoids/armandia", "vhloadermaryttsasaprealizertester.xml", "TestAvatar");
            vHuman = staticEnvironment.loadVirtualHuman("armandia", "Humanoids/armandia", "armandia_asaploader_mary_hudson.xml",
                    "TestAvatar");
        }
        catch (IOException ex)
        {
            System.out.println("Cannot load VH");
        }

        hre.loadBox("bluebox", new float[] { 0.05f, 0.05f, 0.05f }, RenderStyle.LINE, new float[] { 0.2f, 0.2f, 1, 1 }, new float[] { 0.2f,
                0.2f, 1, 1 }, new float[] { 0.2f, 0.2f, 1, 0 }, new float[] { 0.2f, 0.2f, 1, 1 });
        VJoint boxJoint = hre.getObjectRootJoint("bluebox");
        boxJoint.setTranslation(-0.25f, 1.45f, 0.3f);
        staticEnvironment.getWorldObjectManager().addWorldObject("bluebox", new WorldObject(boxJoint));
        logger.debug("Finished setup");
    }

    @Before
    public void setup() throws InterruptedException
    {
        //env = staticEnvironment;
        RealizerPort realizerPort = vHuman.getRealizerPort();
        realizerPort.removeAllListeners();  
        realizerPort.addListeners(this);
        realizerHandler.setRealizerTestPort(new AsapRealizerPort(realizerPort));
        
        anticipator = new DummyAnticipator(1000000d, 2000000d);
        vHuman.getElckerlycRealizer().getScheduler().addAnticipator("dummyanticipator", anticipator);
        realizerHandler.performBML("<bml id=\"replacesetup\" composition=\"REPLACE\"/>");
        realizerHandler.waitForBMLEndFeedback("replacesetup");
        realizerHandler.clearFeedbackLists();
    }

    @Override
    @Ignore
    public void testTransition() // transition behaviors are now deprecated
    {

    }

    @Test
    public void testTemporaryInvalidTimingInAppend() throws InterruptedException, IOException
    {
        String bmlString1 = "<bml id=\"bml1\" xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" "
                + "bmlt:preplan=\"true\"><speech id=\"sp1\"><text>Hello</text></speech></bml>";
        String bmlString2 = "<bml id=\"bml2\" composition=\"APPEND\">"
                + "<wait id=\"w1\" start=\"0\" end=\"anticipators:dummyanticipator:dummy1\"/></bml>";
        String bmlString3 = "<bml id=\"bml3\" composition=\"APPEND\">"
                + "<speech id=\"sp1\"><text>Hello hello hello hello hello hello </text></speech></bml>";
        String bmlString4 = "<bml id=\"bml4\" composition=\"APPEND\">"
                + "<wait id=\"w1\" start=\"0\" end=\"anticipators:dummyanticipator:dummy2\"/></bml>";
        String bmlString5 = "<bml id=\"bml5\" composition=\"APPEND\">"
                + "<speech id=\"sp1\"><text>Hello hello hello hello hello hello </text></speech></bml>";
        String bmlString6 = "<bml id=\"bml6\" xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" bmlt:onStart=\"bml1\"></bml>";
        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString2);
        realizerHandler.performBML(bmlString3);
        realizerHandler.performBML(bmlString4);
        realizerHandler.performBML(bmlString5);
        realizerHandler.performBML(bmlString6);
        realizerHandler.waitForBMLStartFeedback("bml1");

        anticipator.setTime1(realizerHandler.getBMLPerformanceStartFeedback("bml1").predictedEnd + 10);
        realizerHandler.performBML("<bml id=\"bmlx\"/>");
        realizerHandler.waitForBMLStartFeedback("bml3");
        realizerHandler.performBML("<bml id=\"bmly\"/>");
        anticipator.setTime2(realizerHandler.getBMLPerformanceStartFeedback("bml3").predictedEnd + 10);
        realizerHandler.performBML("<bml id=\"bmlz\"/>");
        realizerHandler.waitForBMLEndFeedback("bml5");

        realizerHandler.assertNoDuplicateFeedbacks();
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
    }

    @After
    public void teardownEnvironment() throws InterruptedException
    {
        realizerHandler.performBML("<bml id=\"cleanup\" composition=\"REPLACE\"/>");
        realizerHandler.waitForBMLEndFeedback("cleanup");
    }

    @AfterClass
    public static void cleanup()
    {
        staticEnvironment.requestShutdown();
        // no need to pull the plug on the JFrame as the application will end now?
        while (!staticEnvironment.isShutdown())
        {
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
