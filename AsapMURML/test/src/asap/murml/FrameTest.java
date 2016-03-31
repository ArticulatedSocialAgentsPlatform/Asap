/*******************************************************************************
 *******************************************************************************/
package asap.murml;

import static asap.murml.testutil.MURMLTestUtil.createJointValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

/**
 * Unit tests for MURML frame
 * @author hvanwelbergen
 * 
 */
public class FrameTest
{
    private static final double PARAMETER_PRECISION = 0.0001;

    @Test
    public void testRead()
    {
        Frame f = new Frame();
        f.readXML("<frame xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" ftime=\"0.1\">"
                + "<posture>Humanoid (dB_Smile 3 70 0 0) (dB_OpenMouthWOOQ 3 0 0 0) "
                + "(dB_OpenMouthL 3 0 0 0) (dB_OpenMouthE 3 0 0 0)</posture></frame>");
        assertThat(
                f.getPosture().getJointValues(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(createJointValue("dB_Smile", 70, 0, 0),
                        createJointValue("dB_OpenMouthWOOQ", 0, 0, 0), createJointValue("dB_OpenMouthL", 0, 0, 0),
                        createJointValue("dB_OpenMouthE", 0, 0, 0)));
        assertEquals(0.1, f.getFtime(), PARAMETER_PRECISION);
    }
    
    @Test
    public void testWrite()
    {
        Frame fIn = new Frame();
        fIn.readXML("<frame xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" ftime=\"0.1\">"
                + "<posture>Humanoid (dB_Smile 3 70 0 0) (dB_OpenMouthWOOQ 3 0 0 0) "
                + "(dB_OpenMouthL 3 0 0 0) (dB_OpenMouthE 3 0 0 0)</posture></frame>");
        StringBuilder buf = new StringBuilder();
        fIn.appendXML(buf);
        
        Frame fOut = new Frame();
        fOut.readXML(buf.toString());        
        assertEquals(0.1, fOut.getFtime(), PARAMETER_PRECISION);
        assertNotNull(fOut.getPosture());
        assertThat(
                fOut.getPosture().getJointValues(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(createJointValue("dB_Smile", 70, 0, 0),
                        createJointValue("dB_OpenMouthWOOQ", 0, 0, 0), createJointValue("dB_OpenMouthL", 0, 0, 0),
                        createJointValue("dB_OpenMouthE", 0, 0, 0)));
        
    }
}
