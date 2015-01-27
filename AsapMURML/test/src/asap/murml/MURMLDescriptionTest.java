/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import hmi.xml.XMLScanException;

import org.junit.Ignore;
import org.junit.Test;

import asap.murml.DynamicElement.Type;
import asap.murml.testutil.MURMLTestUtil;

/**
 * Unit tests for the MURMLDescription
 * @author hvanwelbergen
 * 
 */
public class MURMLDescriptionTest
{
    private static final double PRECISION = 0.001;
    private MURMLDescription desc = new MURMLDescription();

    @Test
    public void testReadKeyframeGesture()
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
    public void testWriteKeyframeGesture()
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
        MURMLDescription descIn = new MURMLDescription();
        descIn.readXML(murmlScript);
        StringBuilder buf = new StringBuilder();
        descIn.appendXML(buf);
        MURMLDescription descOut = new MURMLDescription();
        descOut.readXML(buf.toString());

        Dynamic dynamic = descOut.getDynamic();
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
    public void testHandLocationCurved()
    {
        // @formatter:off
            String murmlScript =
            "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" id=\"voila_center\" scope=\"hand\">"+                
                  "<dynamic slot=\"HandLocation\" scope=\"right_arm\">"+
                    "<dynamicElement type=\"curve\" scope=\"right_arm\">"+
                      "<value type=\"start\" name=\"LocLowerChest LocCCenter LocNorm\"/>"+
                      "<value type=\"end\" name=\"LocStomach LocCenterRight LocFFar\"/>"+
                    "</dynamicElement>"+
                  "</dynamic>"+                                               
            "</murml-description>";
       // @formatter:on
        desc.readXML(murmlScript);

        Dynamic handLoc = desc.getDynamic();
        assertEquals(Slot.HandLocation, handLoc.getSlot());
        assertEquals("right_arm", handLoc.getScope());
        DynamicElement dynElem = handLoc.getDynamicElements().get(0);
        assertEquals("LocLowerChest LocCCenter LocNorm", dynElem.getName("start"));
        assertEquals("LocStomach LocCenterRight LocFFar", dynElem.getName("end"));
        assertEquals(Type.CURVE, dynElem.getType());
    }

    @Test
    public void testWriteHandLocationCurved()
    {
        // @formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" id=\"voila_center\" scope=\"hand\">"+                
              "<dynamic slot=\"HandLocation\" scope=\"right_arm\">"+
                "<dynamicElement type=\"curve\" scope=\"right_arm\">"+
                  "<value type=\"start\" name=\"LocLowerChest LocCCenter LocNorm\"/>"+
                  "<value type=\"end\" name=\"LocStomach LocCenterRight LocFFar\"/>"+
                "</dynamicElement>"+
              "</dynamic>"+                                               
        "</murml-description>";
        // @formatter:on
        MURMLDescription descIn = new MURMLDescription();
        descIn.readXML(murmlScript);
        StringBuilder buf = new StringBuilder();
        descIn.appendXML(buf);
        MURMLDescription descOut = new MURMLDescription();
        descOut.readXML(buf.toString());

        Dynamic handLoc = descOut.getDynamic();
        assertEquals(Slot.HandLocation, handLoc.getSlot());
        assertEquals("right_arm", handLoc.getScope());
        DynamicElement dynElem = handLoc.getDynamicElements().get(0);
        assertEquals("LocLowerChest LocCCenter LocNorm", dynElem.getName("start"));
        assertEquals("LocStomach LocCenterRight LocFFar", dynElem.getName("end"));
        assertEquals(Type.CURVE, dynElem.getType());
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
        desc.readXML(murmlScript);
        Parallel p = desc.getParallel();
        assertEquals(4, p.getDynamics().size());

        Dynamic handLoc = MURMLTestUtil.getDynamic(Slot.HandLocation, p.getDynamics());
        assertEquals(Slot.HandLocation, handLoc.getSlot());
        assertEquals("right_arm", handLoc.getScope());
        DynamicElement dynElem = handLoc.getDynamicElements().get(0);
        assertEquals("LocLowerChest LocCCenter LocNorm", dynElem.getName("start"));
        assertEquals("LocStomach LocCenterRight LocFFar", dynElem.getName("end"));
    }

    @Test
    public void testWriteProceduralGesture()
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
        MURMLDescription descIn = new MURMLDescription();
        descIn.readXML(murmlScript);
        StringBuilder buf = new StringBuilder();
        descIn.appendXML(buf);
        MURMLDescription descOut = new MURMLDescription();
        descOut.readXML(buf.toString());

        Parallel p = descOut.getParallel();
        assertEquals(4, p.getDynamics().size());

        Dynamic handLoc = MURMLTestUtil.getDynamic(Slot.HandLocation, p.getDynamics());
        assertEquals(Slot.HandLocation, handLoc.getSlot());
        assertEquals("right_arm", handLoc.getScope());
        DynamicElement dynElem = handLoc.getDynamicElements().get(0);
        assertEquals("LocLowerChest LocCCenter LocNorm", dynElem.getName("start"));
        assertEquals("LocStomach LocCenterRight LocFFar", dynElem.getName("end"));
    }

    @Test
    public void testStatic()
    {
        //@formatter:off
        String murmlScript = 
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" scope=\"hand\">"+
            "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"right_arm\"/>"+           
        "</murml-description>";
        // @formatter:on
        desc.readXML(murmlScript);
        Static stat = desc.getStaticElement();
        assertEquals(Slot.HandShape, stat.getSlot());
        assertEquals("BSfist (ThExt)", stat.getValue());
        assertEquals("right_arm", stat.getScope());
    }

    @Test
    public void testWriteStatic()
    {
        //@formatter:off
        String murmlScript = 
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" scope=\"hand\">"+
            "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"right_arm\"/>"+           
        "</murml-description>";
        // @formatter:on
        MURMLDescription descIn = new MURMLDescription();
        descIn.readXML(murmlScript);
        StringBuilder buf = new StringBuilder();
        descIn.appendXML(buf);
        MURMLDescription descOut = new MURMLDescription();
        descOut.readXML(buf.toString());
        
        Static stat = descOut.getStaticElement();
        assertEquals(Slot.HandShape, stat.getSlot());
        assertEquals("BSfist (ThExt)", stat.getValue());
        assertEquals("right_arm", stat.getScope());
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
        desc.readXML(murmlScript);
        Symmetrical sym = desc.getSymmetrical();
        assertNotNull(sym);
        Parallel par = sym.getParallel();
        assertNotNull(par);
        Static stat = MURMLTestUtil.getStatic(Slot.PalmOrientation, par.getStatics());
        assertEquals("DirL", stat.getValue());
        assertEquals(Slot.PalmOrientation, stat.getSlot());
    }

    @Test
    public void testSymmetricalDynamic()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
            "<dynamic slot=\"HandLocation\">"+
              "<dynamicElement type=\"linear\" scope=\"right_arm\">"+
                "<value type=\"start\" name=\"LocLowerChest LocCCenter LocNorm\"/>"+
                "<value type=\"end\" name=\"LocStomach LocCenterRight LocFFar\"/>"+
              "</dynamicElement>"+
            "</dynamic>"+
          "</symmetrical>"+
        "</murml-description>";
        //@formatter:on        
        desc.readXML(murmlScript);
        
        Parallel par = desc.getParallel();
        assertNotNull(par);
        assertEquals(2, par.getDynamics().size());
        Dynamic dynRight = par.getDynamics().get(0);
        assertEquals("right_arm", dynRight.getScope());

        Dynamic dynLeft = par.getDynamics().get(1);
        assertEquals("left_arm", dynLeft.getScope());
        assertEquals(Symmetry.SymMS, dynLeft.getSymmetryTransform());
    }

    @Ignore //FIXME: writeback of symmetricals
    @Test
    public void testWriteSymmetricalDynamic()
    {
      //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
            "<dynamic slot=\"HandLocation\">"+
              "<dynamicElement type=\"linear\" scope=\"right_arm\">"+
                "<value type=\"start\" name=\"LocLowerChest LocCCenter LocNorm\"/>"+
                "<value type=\"end\" name=\"LocStomach LocCenterRight LocFFar\"/>"+
              "</dynamicElement>"+
            "</dynamic>"+
          "</symmetrical>"+
        "</murml-description>";
        //@formatter:on
        
        MURMLDescription descIn = new MURMLDescription();
        descIn.readXML(murmlScript);
        StringBuilder buf = new StringBuilder();
        descIn.appendXML(buf);
        System.out.println(buf.toString());
        MURMLDescription descOut = new MURMLDescription();
        descOut.readXML(buf.toString());
        
        
        Parallel par = descOut.getParallel();
        assertNotNull(par);
        assertEquals(2, par.getDynamics().size());
        Dynamic dynRight = par.getDynamics().get(0);
        assertEquals("right_arm", dynRight.getScope());

        Dynamic dynLeft = par.getDynamics().get(1);
        assertEquals("left_arm", dynLeft.getScope());
        assertEquals(Symmetry.SymMS, dynLeft.getSymmetryTransform());
    }
    
    @Test
    public void testSymmetricalStatic()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
              "<static slot=\"PalmOrientation\" value=\"DirL\"/>"+ 
          "</symmetrical>"+
        "</murml-description>";
        //@formatter:on

        desc.readXML(murmlScript);
        Parallel par = desc.getParallel();
        assertNotNull(par);
        assertEquals(2, par.getStatics().size());
        Static sRight = par.getStatics().get(0);
        assertEquals("right_arm", sRight.getScope());

        Static sLeft = par.getStatics().get(1);
        assertEquals("left_arm", sLeft.getScope());
        assertEquals(Symmetry.SymMS, sLeft.getSymmetryTransform());
        assertEquals(Slot.PalmOrientation, sLeft.getSlot());
        assertEquals("DirL", sLeft.getValue());
    }

    @Test
    public void testSymmetricalSequence()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
              "<sequence>"+
                  "<static slot=\"PalmOrientation\" value=\"DirLTL\"/>"+
                  "<dynamic slot=\"PalmOrientation\">"+
                      "<dynamicElement>"+
                        "<value type=\"start\" name=\"PalmLU\"/>"+
                        "<value type=\"end\" name=\"PalmU\"/>"+
                      "</dynamicElement>"+
                  "</dynamic>"+
              "</sequence>"+
          "</symmetrical>"+
        "</murml-description>";
        //@formatter:on        
        desc.readXML(murmlScript);
        Parallel par = desc.getParallel();
        assertNotNull(par);
        assertEquals(1, par.getSequences().size());
        Sequence seq = par.getSequences().get(0);
        assertEquals(2, seq.getSequence().size());
        assertThat(seq.getSequence().get(0), instanceOf(Parallel.class));
        assertThat(seq.getSequence().get(1), instanceOf(Parallel.class));

        Parallel p1 = (Parallel) (seq.getSequence().get(0));
        assertEquals(2, p1.getStatics().size());
        Static sRight = p1.getStatics().get(0);
        assertEquals("right_arm", sRight.getScope());
        Static sLeft = p1.getStatics().get(1);
        assertEquals("left_arm", sLeft.getScope());

        Parallel p2 = (Parallel) (seq.getSequence().get(1));
        assertEquals(2, p2.getDynamics().size());
    }

    @Test
    public void testSymmetricalParallel()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
              "<parallel>"+
                  "<static slot=\"PalmOrientation\" value=\"DirLTL\"/>"+
                  "<dynamic slot=\"PalmOrientation\">"+
                      "<dynamicElement>"+
                        "<value type=\"start\" name=\"PalmLU\"/>"+
                        "<value type=\"end\" name=\"PalmU\"/>"+
                      "</dynamicElement>"+
                  "</dynamic>"+
              "</parallel>"+
          "</symmetrical>"+
        "</murml-description>";
        //@formatter:on        
        desc.readXML(murmlScript);
        Parallel par = desc.getParallel();
        assertNotNull(par);
        assertEquals(2, par.getStatics().size());
        assertEquals(2, par.getDynamics().size());
    }

    @Test
    public void testInnerSymmetricalInParallel()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<parallel>"+
              "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
                  "<static slot=\"PalmOrientation\" value=\"DirLTL\"/>"+                  
              "</symmetrical>"+
              "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"right_arm\"/>"+
          "</parallel>"+
        "</murml-description>";
        //@formatter:on        
        desc.readXML(murmlScript);
        Parallel par = desc.getParallel();
        assertNotNull(par);
        assertEquals(3, par.getStatics().size());
        assertEquals("right_arm", par.getStatics().get(0).getScope());
        assertEquals("right_arm", par.getStatics().get(1).getScope());
        assertEquals("left_arm", par.getStatics().get(2).getScope());
    }

    @Test
    public void testInnerSymmetricalInSequence()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<sequence>"+
              "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
                  "<static slot=\"PalmOrientation\" value=\"DirLTL\"/>"+                  
              "</symmetrical>"+
              "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"right_arm\"/>"+
          "</sequence>"+
        "</murml-description>";
        //@formatter:on        
        desc.readXML(murmlScript);
        Sequence seq = desc.getSequence();
        assertEquals(2, seq.getSequence().size());

        assertThat(seq.getSequence().get(0), instanceOf(Parallel.class));
        assertThat(seq.getSequence().get(1), instanceOf(Static.class));
        Parallel par = (Parallel) (seq.getSequence().get(0));
        assertEquals(2, par.getStatics().size());
        assertEquals("right_arm", par.getStatics().get(0).getScope());
        assertEquals("left_arm", par.getStatics().get(1).getScope());
    }

    @Test
    public void testInnerSymmetricalInParallelInSequence()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<sequence>"+
              "<parallel>"+    
                  "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
                      "<static slot=\"PalmOrientation\" value=\"DirLTL\"/>"+                  
                  "</symmetrical>"+
                  "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"left_arm\"/>"+
              "</parallel>"+
              "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"right_arm\"/>"+
          "</sequence>"+
        "</murml-description>";
        //@formatter:on        
        desc.readXML(murmlScript);
        Sequence seq = desc.getSequence();
        assertEquals(2, seq.getSequence().size());

        assertThat(seq.getSequence().get(0), instanceOf(Parallel.class));
        assertThat(seq.getSequence().get(1), instanceOf(Static.class));

        Parallel par = (Parallel) (seq.getSequence().get(0));
        assertEquals(3, par.getStatics().size());
    }

    @Test
    public void testInnerSymmetricalInSequenceInParallel()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<parallel>"+
              "<sequence>"+    
                  "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
                      "<static slot=\"PalmOrientation\" value=\"DirLTL\"/>"+                  
                  "</symmetrical>"+
                  "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"left_arm\"/>"+
              "</sequence>"+
              "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"right_arm\"/>"+
          "</parallel>"+
        "</murml-description>";
        //@formatter:on        
        desc.readXML(murmlScript);
        Parallel par = desc.getParallel();
        assertNotNull(par);
        assertEquals(1, par.getSequences().size());
        assertEquals(1, par.getStatics().size());
        Sequence seq = par.getSequences().get(0);
        assertEquals(2, seq.getSequence().size());

        assertThat(seq.getSequence().get(0), instanceOf(Parallel.class));
    }

    @Test
    public void testSymmetricalInSequenceInParallel()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
            "<parallel>"+
              "<sequence>"+
                 "<static slot=\"PalmOrientation\" value=\"DirLTL\"/>"+
                 "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"left_arm\"/>"+
              "</sequence>"+
              "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"right_arm\"/>"+
            "</parallel>"+
          "</symmetrical>"+
        "</murml-description>";
        //@formatter:on

        desc.readXML(murmlScript);
        Parallel par = desc.getParallel();
        assertNotNull(par);
        assertEquals(1, par.getSequences().size());
        assertEquals(2, par.getStatics().size());
        Sequence seq = par.getSequences().get(0);
        assertEquals(2, seq.getSequence().size());
        assertThat(seq.getSequence().get(0), instanceOf(Parallel.class));
        assertThat(seq.getSequence().get(1), instanceOf(Parallel.class));
    }

    @Test
    public void testSymmetricalInParallelInSequence()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
            "<sequence>"+
              "<parallel>"+
                 "<static slot=\"PalmOrientation\" value=\"DirLTL\"/>"+
                 "<static slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
              "</parallel>"+
              "<static slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
            "</sequence>"+
          "</symmetrical>"+
        "</murml-description>";
        //@formatter:on

        desc.readXML(murmlScript);
        Parallel par = desc.getParallel();
        assertNotNull(par);
        assertEquals(1, par.getSequences().size());
        Sequence seq = par.getSequences().get(0);
        assertEquals(2, seq.getSequence().size());
        assertThat(seq.getSequence().get(0), instanceOf(Parallel.class));
        assertThat(seq.getSequence().get(1), instanceOf(Parallel.class));
        Parallel parInner = (Parallel) seq.getSequence().get(0);
        assertEquals(4, parInner.getStatics().size());
    }

    @Test(expected = XMLScanException.class)
    public void testSymmetricalInParallelInSymmetrical()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
            "<parallel>"+
                "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
                     "<static slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
                 "</symmetrical>"+
                 "<static slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
            "</parallel>"+
          "</symmetrical>"+
        "</murml-description>";
        //@formatter:on
        desc.readXML(murmlScript);
    }

    @Test(expected = XMLScanException.class)
    public void testSymmetricalInSequenceInSymmetrical()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
            "<sequence>"+
                "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
                     "<static slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
                 "</symmetrical>"+
                 "<static slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
            "</sequence>"+
          "</symmetrical>"+
        "</murml-description>";
        //@formatter:on
        desc.readXML(murmlScript);
    }

    @Test(expected = XMLScanException.class)
    public void testInnerSymmetricalInSequenceInSymmetrical()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<parallel>"+
          "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
            "<sequence>"+
                "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
                     "<static slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
                 "</symmetrical>"+
                 "<static slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
            "</sequence>"+
          "</symmetrical>"+
          "</parallel>"+
        "</murml-description>";
        //@formatter:on
        desc.readXML(murmlScript);
    }

    @Test(expected = XMLScanException.class)
    public void testParallelInParallel()
    {
        //@formatter:off
        String murmlScript =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
          "<parallel>"+
            "<parallel>"+
                "<symmetrical dominant=\"right_arm\" symmetry=\"SymMS\">"+
                     "<static slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
                 "</symmetrical>"+
                 "<static slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
            "</parallel>"+
            "<static slot=\"PalmOrientation\" value=\"DirLTL\"/>"+        
          "</parallel>"+
        "</murml-description>";
        //@formatter:on
        desc.readXML(murmlScript);
    }

    @Test
    public void testSequence()
    {
        //@formatter:off
        String murmlScript = 
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" scope=\"hand\">"+
          "<sequence>"+
            "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"right_arm\"/>"+
            "<static slot=\"ExtFingerOrientation\" value=\"DirU\" scope=\"right_arm\"/>"+
            "<static slot=\"PalmOrientation\" value=\"DirLTL\" scope=\"right_arm\"/>"+
            "<static slot=\"HandLocation\""+
                   "value=\"LocNeck LocCenterRRight LocNorm\""+
                   "scope=\"right_arm\"/>"+
            "<dynamic slot=\"PalmOrientation\" scope=\"right_arm\">"+
                   "<dynamicElement>"+
                     "<value type=\"start\" name=\"PalmLU\"/>"+
                     "<value type=\"end\" name=\"PalmU\"/>"+
                   "</dynamicElement>"+
            "</dynamic>"+
          "</sequence>"+
        "</murml-description>";
        // @formatter:on
        desc.readXML(murmlScript);
        Sequence seq = desc.getSequence();
        assertNotNull(seq);

        assertEquals(5, seq.getSequence().size());
        assertThat(seq.getSequence().get(4), instanceOf(Dynamic.class));
    }

    @Test
    public void testWriteSequence()
    {
        //@formatter:off
        String murmlScript = 
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" scope=\"hand\">"+
          "<sequence>"+
            "<static slot=\"HandShape\" value=\"BSfist (ThExt)\" scope=\"right_arm\"/>"+
            "<static slot=\"ExtFingerOrientation\" value=\"DirU\" scope=\"right_arm\"/>"+
            "<static slot=\"PalmOrientation\" value=\"DirLTL\" scope=\"right_arm\"/>"+
            "<static slot=\"HandLocation\""+
                   "value=\"LocNeck LocCenterRRight LocNorm\""+
                   "scope=\"right_arm\"/>"+
            "<dynamic slot=\"PalmOrientation\" scope=\"right_arm\">"+
                   "<dynamicElement>"+
                     "<value type=\"start\" name=\"PalmLU\"/>"+
                     "<value type=\"end\" name=\"PalmU\"/>"+
                   "</dynamicElement>"+
            "</dynamic>"+
          "</sequence>"+
        "</murml-description>";
        // @formatter:on

        MURMLDescription descIn = new MURMLDescription();
        descIn.readXML(murmlScript);
        StringBuilder buf = new StringBuilder();
        descIn.appendXML(buf);
        MURMLDescription descOut = new MURMLDescription();
        descOut.readXML(buf.toString());
        
        Sequence seq = descOut.getSequence();
        assertNotNull(seq);
        assertEquals(5, seq.getSequence().size());
        assertThat(seq.getSequence().get(4), instanceOf(Dynamic.class));
    }
}
