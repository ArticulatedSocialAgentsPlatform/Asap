package asap.murml;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import org.hamcrest.collection.*;
import static asap.murml.testutil.MURMLTestUtil.createJointValue;
/**
 * Unit test cases for posture parsing
 * @author hvanwelbergen
 */
public class PostureTest
{
    
    
    @Test
    public void testParseEmptyPosture()
    {
        Posture p = new Posture();
        p.readXML("<posture/>");
        assertTrue(p.getJointValues().isEmpty());
    }
    
    @Test
    public void testParsePosture()
    {
        Posture p = new Posture();
        p.readXML("<posture>Humanoid (dB_Smile 3 70 0 0) (dB_OpenMouthWOOQ 3 0 0 0) (dB_OpenMouthL 3 0 0 0) (dB_OpenMouthE 3 0 0 0)</posture>");
        assertThat(p.getJointValues(),IsIterableContainingInAnyOrder.containsInAnyOrder(createJointValue("dB_Smile",70,0,0), createJointValue("dB_OpenMouthWOOQ",0,0,0),
        createJointValue("dB_OpenMouthL", 0, 0, 0), createJointValue("dB_OpenMouthE",0,0,0)));        
    }
}
