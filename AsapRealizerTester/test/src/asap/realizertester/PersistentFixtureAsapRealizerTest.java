/*******************************************************************************
 *******************************************************************************/
package asap.realizertester;

import hmi.animation.VJoint;
import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.Environment;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment.RenderStyle;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObjectEnvironment;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.realizer.anticipator.Anticipator;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizerport.RealizerPort;

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

        public DummyAnticipator(String id, PegBoard pb, double absoluteTime1, double absoluteTime2)
        {
            super(id, pb);
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
        WorldObjectEnvironment we = new WorldObjectEnvironment();
        OdePhysicsEnvironment ope = new OdePhysicsEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        staticEnvironment = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("LJWGL_JOAL");

        mainUI = new JFrame("Asap BML Realizer Tester");
        mainUI.setSize(1000, 600);

        hre.init(); // canvas does not exist until init was called
        we.init();
        ope.init();
        aue.init();
        mae.init(ope);
        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(hre);
        environments.add(we);
        environments.add(ope);
        environments.add(mae);
        environments.add(aue);
        environments.add(staticEnvironment);

        staticEnvironment.init(environments, ope.getPhysicsClock()); // if no physics, just use renderclock here!

        // this clock method drives the engines in ee. if no physics, then register ee as a listener at the render clock!
        ope.addPrePhysicsCopyListener(staticEnvironment);

        java.awt.Component canvas = hre.getAWTComponent(); // after init, get canvas and add to window
        mainUI.add(canvas);
        mainUI.setVisible(true);

        // add worldobject "camera" that we can use to look at user :)
        VJoint camera = hre.getCameraTarget();
        we.getWorldObjectManager().addWorldObject("camera", new VJointWorldObject(camera));

        try
        {
            // vHuman = staticEnvironment.loadVirtualHuman("blueguy", "Humanoids/blueguy",
            // "blueguy_asaploader_mary_hudson.xml", "blueguy - test avatar");
            // vHuman = staticEnvironment.loadVirtualHuman("Humanoids/armandia", "vhloadermaryttsasaprealizertester.xml", "TestAvatar");
            vHuman = staticEnvironment.loadVirtualHuman("armandia", "AsapRealizerTester", "armandia_asaploader_mary_hudson.xml",
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
        we.getWorldObjectManager().addWorldObject("bluebox", new VJointWorldObject(boxJoint));

        hre.startRenderClock();
        ope.startPhysicsClock();
        logger.debug("Finished setup");
    }

    @Before
    public void setup() throws InterruptedException
    {
        // env = staticEnvironment;
        RealizerPort realizerPort = vHuman.getRealizerPort();
        realizerPort.removeAllListeners();
        realizerPort.addListeners(this);
        realizerHandler.setRealizerTestPort(new AsapRealizerPort(realizerPort));

        anticipator = new DummyAnticipator("dummyanticipator", vHuman.getPegBoard(), 1000000d, 2000000d);
        realizerHandler.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"replacesetup\" composition=\"REPLACE\"/>");
        realizerHandler.waitForBMLEndFeedback("replacesetup");
        realizerHandler.clearFeedbackLists();
    }

    @Ignore
    // TODO: predictedEnd is no longer available in BML1
    @Test
    public void testTemporaryInvalidTimingInAppend() throws InterruptedException, IOException
    {
        String bmlString1 = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"bml1\" xmlns:bmla=\"http://www.asap-project.org/bmla\" "
                + "bmla:preplan=\"true\"><speech id=\"sp1\"><text>Hello</text></speech></bml>";
        String bmlString2 = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" " + "id=\"bml2\" composition=\"APPEND\">"
                + "<wait id=\"w1\" start=\"0\" end=\"anticipators:dummyanticipator:dummy1\"/></bml>";
        String bmlString3 = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" " + "id=\"bml3\" composition=\"APPEND\">"
                + "<speech id=\"sp1\"><text>Hello hello hello hello hello hello </text></speech></bml>";
        String bmlString4 = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" " + "id=\"bml4\" composition=\"APPEND\">"
                + "<wait id=\"w1\" start=\"0\" end=\"anticipators:dummyanticipator:dummy2\"/></bml>";
        String bmlString5 = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" " + "id=\"bml5\" composition=\"APPEND\">"
                + "<speech id=\"sp1\"><text>Hello hello hello hello hello hello </text></speech></bml>";
        String bmlString6 = "<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"bml6\" xmlns:bmla=\"http://www.asap-project.org/bmla\" bmla:onStart=\"bml1\"></bml>";
        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString2);
        realizerHandler.performBML(bmlString3);
        realizerHandler.performBML(bmlString4);
        realizerHandler.performBML(bmlString5);
        realizerHandler.performBML(bmlString6);
        realizerHandler.waitForBMLStartFeedback("bml1");

        anticipator.setTime1(realizerHandler.getBMLPerformanceStartFeedback("bml1").predictedEnd + 10);
        realizerHandler.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bmlx\"/>");
        realizerHandler.waitForBMLStartFeedback("bml3");
        realizerHandler.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bmly\"/>");
        anticipator.setTime2(realizerHandler.getBMLPerformanceStartFeedback("bml3").predictedEnd + 10);
        realizerHandler.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"bmlz\"/>");
        realizerHandler.waitForBMLEndFeedback("bml5");

        realizerHandler.assertNoDuplicateFeedbacks();
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
    }

    //@AfterClass
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
