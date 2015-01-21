/*******************************************************************************
 *******************************************************************************/
package asap.asaprealizertesterincrementalspeech;

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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.realizerport.RealizerPort;
import asap.realizertester.AbstractASAPRealizerTest;
import asap.realizertester.AsapRealizerPort;

/**
 * RealizerTester with incremental TTS configuration
 * @author hvanwelbergen
 * 
 */
public class AsapRealizerTesterIncrementalSpeech extends AbstractASAPRealizerTest
{
    private static AsapEnvironment staticEnvironment;
    private static AsapVirtualHuman vHuman;

    private static JFrame mainUI = null;

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
            vHuman = staticEnvironment.loadVirtualHuman("armandia", "AsapRealizerTester",
                    "armandia_asaploader_mary_hudson_incremental.xml", "TestAvatar");
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
        
        ope.startPhysicsClock();
        hre.startRenderClock();        
        
        logger.debug("Finished setup");
    }

    @Before
    public void setup() throws InterruptedException
    {
        RealizerPort realizerPort = vHuman.getRealizerPort();
        realizerPort.removeAllListeners();
        realizerPort.addListeners(this);
        realizerHandler.setRealizerTestPort(new AsapRealizerPort(realizerPort));

        realizerHandler.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"replacesetup\" composition=\"REPLACE\"/>");
        realizerHandler.waitForBMLEndFeedback("replacesetup");
        realizerHandler.clearFeedbackLists();
    }

    @After
    public void teardownEnvironment() throws InterruptedException
    {
        // realizerHandler.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" " + "id=\"cleanup\" composition=\"REPLACE\"/>");
        // realizerHandler.waitForBMLEndFeedback("cleanup");
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

    // has interruption
    @Ignore
    @Override
    public void testPreplan()
    {

    }

    // has interruption
    @Ignore
    @Override
    public void testInterruptBehaviour2()
    {

    }

    // has interruption
    @Ignore
    @Override
    public void testInterruptBehaviour()
    {

    }

    // has interruption
    @Ignore
    @Override
    public void testInterruptBehaviourRestart()
    {

    }

    @Test
    public void testHesitatedSpeech() throws IOException
    {
        String bmlString = readTestFile("bmlis/filler.xml");
        realizerHandler.performBML(bmlString);
        realizerHandler.waitForBMLEndFeedback("bml1");
        
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertNoDuplicateFeedbacks();
        
        realizerHandler.assertRelativeSyncTime("bml1", "speech1", "start", 0);
        realizerHandler.assertSyncAfterSync("bml1", "speech1", "end", "bml1", "speech1", "relax");        
    }
    
    @Test
    public void testChunkSpeech() throws IOException, InterruptedException
    {
        String bmlString1 = readTestFile("asap/chunkspeech/firstchunk.xml");
        String bmlString2 = readTestFile("asap/chunkspeech/secondchunk.xml");

        realizerHandler.performBML(bmlString1);
        realizerHandler.performBML(bmlString2);

        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.waitForBMLEndFeedback("bml2");
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertBlockStartAndStopFeedbacks("bml1", "bml2");
        realizerHandler.assertNoDuplicateFeedbacks();
        
        realizerHandler.assertLinkedSyncs("bml1", "speech1", "relax", "bml2", "speech1", "start");
    }
    
    @Test
    public void testSpeechRetime() throws IOException
    {
        String bmlString = readTestFile("bmlis/speech_retime.xml");
        realizerHandler.performBML(bmlString);
        realizerHandler.waitForBMLEndFeedback("bml1");
        realizerHandler.assertNoExceptions();
        realizerHandler.assertNoWarnings();
        realizerHandler.assertNoDuplicateFeedbacks();
        realizerHandler.assertRelativeSyncTime("bml1", "speech1", "start", 0.5);
        realizerHandler.assertRelativeSyncTime("bml1", "speech1", "s1", 1);
        realizerHandler.assertRelativeSyncTime("bml1", "speech1", "end", 2);
    }
}
