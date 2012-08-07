package asap.murml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import asap.murml.testutil.MURMLTestUtil;

/**
 * Unit tests for the MURMLDescription
 * @author hvanwelbergen
 * 
 */
public class MURMLDescriptionTest
{
    private static final double PRECISION = 0.001;

    @Test
    public void testKeyframeGesture()
    {
        // @formatter:off
            String murmlScript = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" id=\"head-tilt\" scope=\"head\">"+
                                    "<dynamic slot=\"Neck\" scope=\"neck\">"+
                                    "<keyframing mode=\"spline\" easescale=\"7.0\" easeturningpoint=\"0.5\" priority=\"50\">"+
                                    "<phase>"+
                                    "<frame ftime=\"0.2\">"+
                                    "<posture> Humanoid (vc2 3 3 0 0) (vc4 3 2 0 0) </posture>"+
                                    "</frame>"+
                                    "<frame ftime=\"0.6\">"+
                                    "<posture> Humanoid (vc2 3 3 0 0) (vc4 3 4 0 0) </posture>"+
                                    "</frame>"+
                                    "<frame ftime=\"0.8\">"+
                                    "<posture> Humanoid (vc2 3 0 0 0) (vc4 3 0 0 0) </posture>"+
                                    "</frame>"+
                                    "</phase>"+
                                    "</keyframing>"+
                                    "</dynamic>"+                                    
                                  "</murml-description>";
            // @formatter:on
        MURMLDescription desc = new MURMLDescription();
        desc.readXML(murmlScript);
        Dynamic dynamic = desc.getDynamic();
        assertEquals(Slot.Neck, dynamic.getSlot());
        assertEquals("neck", dynamic.getScope());
        Keyframing kf = dynamic.getKeyframing();
        assertEquals(Keyframing.Mode.SPLINE, kf.getMode());
        assertEquals(7.0, kf.getEasescale(), PRECISION);
        assertEquals(0.5, kf.getEaseturningpoint(), PRECISION);
        assertEquals(50, kf.getPriority());
        assertEquals(1, kf.getPhases().size());
        Phase ph = kf.getPhases().get(0);
        assertEquals(3, ph.getFrames().size());
        Frame frame1 = ph.getFrames().get(0);
        Frame frame2 = ph.getFrames().get(1);
        Frame frame3 = ph.getFrames().get(2);
        assertEquals(0.2, frame1.getFtime(), PRECISION);
        assertEquals(0.6, frame2.getFtime(), PRECISION);
        assertEquals(0.8, frame3.getFtime(), PRECISION);
        assertEquals("vc2", frame1.getPosture().getJointValues().get(0).getJointSid());
    }

    @Test
    public void testProceduralGesture()
    {
        // @formatter:off
            String murmlScript =
            "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" id=\"voila_center\" scope=\"hand\">"+
                "<parallel>"+
                  "<dynamic slot=\"HandLocation\" scope=\"right_arm\">"+
                    "<dynamicElement type=\"linear\" scope=\"right_arm\">"+
                      "<value type=\"start\" name=\"LocLowerChest LocCCenter LocNorm\"/>"+
                      "<value type=\"end\" name=\"LocStomach LocCenterRight LocFFar\"/>"+
                    "</dynamicElement>"+
                  "</dynamic>"+
                  "<dynamic slot=\"HandShape\" scope=\"right_arm\">"+
                    "<dynamicElement>"+
                      "<value type=\"start\" name=\"BSneutral\"/>"+
                      "<value type=\"end\" name=\"BSflat\"/>"+
                    "</dynamicElement>"+
                  "</dynamic>"+
                  "<dynamic slot=\"ExtFingerOrientation\" scope=\"right_arm\">"+
                    "<dynamicElement>"+
                      "<value type=\"start\" name=\"DirLA\"/>"+
                      "<value type=\"end\" name=\"DirARAA\"/>"+
                    "</dynamicElement>"+
                  "</dynamic>"+
                  "<dynamic slot=\"PalmOrientation\" scope=\"right_arm\">"+
                    "<dynamicElement>"+
                      "<value type=\"start\" name=\"PalmLU\"/>"+
                      "<value type=\"end\" name=\"PalmU\"/>"+
                    "</dynamicElement>"+
                  "</dynamic>"+
                "</parallel>"+              
            "</murml-description>";
       // @formatter:on
        MURMLDescription desc = new MURMLDescription();
        desc.readXML(murmlScript);
        Parallel p = desc.getParallel();
        assertEquals(4, p.getDynamics().size());

        Dynamic handLoc = MURMLTestUtil.getDynamic(Slot.HandLocation, p.getDynamics());
        assertEquals(Slot.HandLocation, handLoc.getSlot());
        assertEquals("right_arm", handLoc.getScope());
        DynamicElement dynElem = handLoc.getDynamicElement();
        assertThat(dynElem.getNames("start"), IsIterableContainingInOrder.contains("LocLowerChest", "LocCCenter", "LocNorm"));
        assertThat(dynElem.getNames("end"), IsIterableContainingInOrder.contains("LocStomach", "LocCenterRight", "LocFFar"));
    }

    @Test
    public void testParallelStatic()
    {
        //@formatter:off
        String murmlScript = 
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" scope=\"hand\">"+
          "<parallel>"+
            "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"right_arm\"/>"+
            "<static slot=\"ExtFingerOrientation\" value=\"DirU\" scope=\"right_arm\"/>"+
            "<static slot=\"PalmOrientation\" value=\"DirLTL\" scope=\"right_arm\"/>"+
            "<static slot=\"HandLocation\""+
                   "value=\"LocNeck LocCenterRRight LocNorm\""+
                   "scope=\"right_arm\"/>"+
          "</parallel>"+
        "</murml-description>";
        // @formatter:on
        MURMLDescription desc = new MURMLDescription();
        desc.readXML(murmlScript);
        Parallel par = desc.getParallel();
        assertNotNull(par);
        Static stat = MURMLTestUtil.getStatic(Slot.HandShape, par.getStatics());
        assertEquals(Slot.HandShape, stat.getSlot());
        assertEquals("BSfist (ThExt)", stat.getValue());
        assertEquals("right_arm", stat.getScope());
    }

    @Test
    public void testSymmetrical()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" scope=\"hand\">"+
        "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
           "<parallel>"+
             "<static slot=\"HandLocation\" value=\"LocChest LocExtremePeripheryRight LocNorm\"/>"+
             "<static slot=\"ExtFingerOrientation\" value=\"DirA\"/>"+
             "<static slot=\"PalmOrientation\" value=\"DirL\"/>"+             
           "</parallel>"+
         "</symmetrical>"+
        "</murml-description>";
        //@formatter:on
        MURMLDescription desc = new MURMLDescription();
        desc.readXML(murmlScript);
        Symmetrical sym = desc.getSymetrical();
        assertNotNull(sym);
        Parallel par = sym.getParallel();
        assertNotNull(par);
        Static stat = MURMLTestUtil.getStatic(Slot.PalmOrientation, par.getStatics());
        assertEquals("DirL", stat.getValue());
        assertEquals(Slot.PalmOrientation, stat.getSlot());
    }
}
