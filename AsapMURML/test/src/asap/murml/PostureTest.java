/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import static asap.murml.testutil.MURMLTestUtil.createJointValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

/**
 * Unit test cases for posture parsing
 * @author hvanwelbergen
 */
public class PostureTest
{
    private Posture p = new Posture();
    
    @Test
    public void testParseEmptyPosture()
    {
        p.readXML("<posture xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\"/>");
        assertTrue(p.getJointValues().isEmpty());
    }

    @Test
    public void testParsePosture3Dof()
    {
        p.readXML("<posture xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "Humanoid (dB_Smile 3 70 0 0) (dB_OpenMouthWOOQ 3 0 0 0) (dB_OpenMouthL 3 0 0 0) (dB_OpenMouthE 3 0 0 0)</posture>");
        assertThat(
                p.getJointValues(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(createJointValue("dB_Smile", 70, 0, 0),
                        createJointValue("dB_OpenMouthWOOQ", 0, 0, 0), createJointValue("dB_OpenMouthL", 0, 0, 0),
                        createJointValue("dB_OpenMouthE", 0, 0, 0)));
    }
    
    @Test
    public void testParsePosture1Dof()
    {
        p.readXML("<posture xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">(MUSCLE_KIEFER_AUF 0.0) (MUSCLE_BRAUE_LINKS -0.4) (MUSCLE_BRAUE_RECHTS -0.4) </posture>");
        assertThat(
                p.getJointValues(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(createJointValue("MUSCLE_KIEFER_AUF", 0),
                        createJointValue("MUSCLE_BRAUE_LINKS", -0.4f), createJointValue("MUSCLE_BRAUE_RECHTS", -0.4f)));
    }
    
    @Test
    public void testWritePosture1Dof()
    {
        p.readXML("<posture xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">(MUSCLE_KIEFER_AUF 0.0) (MUSCLE_BRAUE_LINKS -0.4) (MUSCLE_BRAUE_RECHTS -0.4) </posture>");
        StringBuilder buf = new StringBuilder();
        p.appendXML(buf);
        
        Posture pOut = new Posture();
        pOut.readXML(buf.toString());
        assertThat(
                pOut.getJointValues(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(createJointValue("MUSCLE_KIEFER_AUF", 0),
                        createJointValue("MUSCLE_BRAUE_LINKS", -0.4f), createJointValue("MUSCLE_BRAUE_RECHTS", -0.4f)));
    }
    
    @Test
    public void testWritePosture3Dof()
    {
        p.readXML("<posture xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "Humanoid (dB_Smile 3 70 0 0) (dB_OpenMouthWOOQ 3 0 0 0) (dB_OpenMouthL 3 0 0 0) (dB_OpenMouthE 3 0 0 0)</posture>");
        StringBuilder buf = new StringBuilder();
        p.appendXML(buf);
        
        Posture pOut = new Posture();
        pOut.readXML(buf.toString());
        assertThat(
                pOut.getJointValues(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(createJointValue("dB_Smile", 70, 0, 0),
                        createJointValue("dB_OpenMouthWOOQ", 0, 0, 0), createJointValue("dB_OpenMouthL", 0, 0, 0),
                        createJointValue("dB_OpenMouthE", 0, 0, 0)));
    }
}
