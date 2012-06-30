package asap.murml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;
import static asap.murml.testutil.MURMLTestUtil.createJointValue;

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
}
