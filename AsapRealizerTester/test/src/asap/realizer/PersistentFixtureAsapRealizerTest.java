package asap.realizer;

import java.io.IOException;

import hmi.animation.VJoint;
import hmi.elckerlyc.BMLBlockPeg;
import hmi.elckerlyc.TimePeg;
import hmi.elckerlyc.anticipator.Anticipator;
import hmi.elckerlyc.world.WorldObject;
import hmi.environment.vhloader.*;
import hmi.graphics.opengl.GLShape;
import hmi.graphics.opengl.geometry.BoxGeometry;
import hmi.graphics.opengl.scenegraph.VGLNode;
import hmi.graphics.opengl.state.GLFill;
import hmi.graphics.opengl.state.GLMaterial;
import hmi.graphics.opengl.state.NoTexture2DState;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.realizertester.AbstractASAPRealizerTest;
import hmi.environment.*;

/**
 * Integration test cases for the AsapRealizer
 * @author hvanwelbergen
 *
 */
public class PersistentFixtureAsapRealizerTest extends AbstractASAPRealizerTest
{
    private static ElckerlycEnvironment staticEnvironment;

    private static final Logger logger = LoggerFactory.getLogger(PersistentFixtureAsapRealizerTest.class.getName());

    private static ElckerlycVirtualHuman vHuman;

    private DummyAnticipator anticipator;

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
        staticEnvironment = new ElckerlycEnvironment()
        {
            @Override
            protected void initQuickSettings()
            {
                frameTitle = "Elckerlyc - HMI BML Realizer Tester";
                useVsync = true;
                runphysics = true;
                // laptop=false;
                laptop = true;
                collisionEnabled = false;
                // debugJOGL = true;
            }

            public void init()
            {
                super.init();
                initDemoScene();
            }

            private void initDemoScene()
            {

                // setup box
                VGLNode boxNode = new VGLNode("box");
                GLShape boxShape = new GLShape();
                BoxGeometry boxGeometry = new BoxGeometry(0.05f, 0.05f, 0.05f);
                boxShape.addGLGeometry(boxGeometry);
                GLMaterial boxState = new GLMaterial();
                /*
                 * boxState.setDiffuseColor(boxDiffuse);
                 * boxState.setSpecularColor(boxSpecular);
                 * boxState.setAmbientColor(boxAmbient);
                 * boxState.setEmissionColor(boxEmission);
                 */
                boxState.setShininess(15);
                boxShape.addGLState(new GLFill());
                boxShape.addGLState(new NoTexture2DState());
                boxShape.addGLState(boxState);
                boxNode.addGLShape(boxShape);
                boxNode.getRoot().setTranslation(-0.25f, 1.45f, 0.3f);

                VJoint boxAni = boxNode.getRoot().masterCopyTree("master-");
                vjWorldRenderRoot.addChild(boxNode.getRoot());
                vjWorldAnimationRoot.addChild(boxAni);
                addVisualisation(boxNode);
                ae.getWorldObjectManager().addWorldObject("bluebox", new WorldObject(boxAni));
            }
        };
        staticEnvironment.init();

        staticEnvironment.startAll();

        // Resources("Humanoids/blueguy"),"spec_blueguy_hmi_mary.xml","TestAvatar");
        // vHuman = staticEnvironment.loadVirtualHuman(new Resources("Humanoids/armandia"), "spec_enterface.xml", "TestAvatar");
        // vHuman = staticEnvironment.loadVirtualHuman(new Resources("Humanoids/blueguy"),"spec_blueguy_hmi_mary.xml","TestAvatar");
        // vHuman = staticEnvironment.loadVirtualHuman(new Resources("Humanoids/blueguy"), "spec_blueguy_hmi_sapi_WAVTTS.xml", "TestAvatar");
        // vHuman = staticEnvironment.loadVirtualHuman("Humanoids/blueguy", "blueguyvhloader_mary.xml", "TestAvatar");
        // vHuman = staticEnvironment.loadVirtualHuman("Humanoids/blueguy", "blueguyvhloader_sapi_testing.xml", "TestAvatar");

        // vHuman = staticEnvironment.loadVirtualHuman("Humanoids/armandia", "armandiavhloader_sapi_testing.xml", "TestAvatar");
        vHuman = staticEnvironment.loadVirtualHuman("Humanoids/armandia", "vhloadermaryttsasaprealizertester.xml", "TestAvatar");

        logger.debug("Finished setup");
    }

    @Before
    public void setup() throws InterruptedException
    {
        env = staticEnvironment;
        realizerPort = vHuman.getElckerlycRealizerLoader().getRealizerBridge();
        realizerPort.removeAllListeners();
        realizerPort.addListeners(this);
        anticipator = new DummyAnticipator(1000000d, 2000000d);
        vHuman.getElckerlycRealizerLoader().getElckerlycRealizer().getScheduler().addAnticipator("dummyanticipator", anticipator);
        realizerPort.performBML("<bml id=\"replacesetup\" scheduling=\"replace\"/>");
        waitForBMLEndFeedback("replacesetup");
        clearFeedbackLists();
    }

    @Test
    public void testTemporaryInvalidTimingInAppend() throws InterruptedException, IOException
    {
        String bmlString1 = "<bml id=\"bml1\" xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" bmlt:preplan=\"true\"><speech id=\"sp1\"><text>Hello</text></speech></bml>";
        String bmlString2 = "<bml id=\"bml2\" scheduling=\"append\"><wait id=\"w1\" start=\"0\" end=\"anticipators:dummyanticipator:dummy1\"/></bml>";
        String bmlString3 = "<bml id=\"bml3\" scheduling=\"append\"><speech id=\"sp1\"><text>Hello hello hello hello hello hello </text></speech></bml>";
        String bmlString4 = "<bml id=\"bml4\" scheduling=\"append\"><wait id=\"w1\" start=\"0\" end=\"anticipators:dummyanticipator:dummy2\"/></bml>";
        String bmlString5 = "<bml id=\"bml5\" scheduling=\"append\"><speech id=\"sp1\"><text>Hello hello hello hello hello hello </text></speech></bml>";
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
        realizerPort.performBML("<bml id=\"cleanup\" scheduling=\"replace\"/>");
        waitForBMLEndFeedback("cleanup");
    }

    @AfterClass
    public static void cleanup()
    {
        staticEnvironment.prepareDispose();
    }
}
