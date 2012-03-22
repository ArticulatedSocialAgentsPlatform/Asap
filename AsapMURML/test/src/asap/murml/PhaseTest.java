package asap.murml;

import static asap.murml.testutil.MURMLTestUtil.createJointValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

/**
 * Unit tests for phase element
 * @author hvanwelbergen
 * 
 */
public class PhaseTest
{
    @Test
    public void testRead()
    {
        Phase ph = new Phase();
        ph.readXML("<phase>" + "<frame ftime=\"0.1\"><posture>Humanoid (dB_Smile 3 70 0 0) "
                + "(dB_OpenMouthWOOQ 3 0 0 0) (dB_OpenMouthL 3 0 0 0) (dB_OpenMouthE 3 0 0 0)</posture>" + "</frame>"
                + "<frame ftime=\"0.2\"><posture>Humanoid (dB_Smile 3 80 0 0) "
                + "(dB_OpenMouthWOOQ 3 1 0 0) (dB_OpenMouthL 3 0 1 0) (dB_OpenMouthE 3 0 0 1)</posture>" + "</frame>" + "</phase>");
        Frame f0 = ph.getFrames().get(0);

        assertThat(
                f0.getPosture().getJointValues(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(createJointValue("dB_Smile", 70, 0, 0),
                        createJointValue("dB_OpenMouthWOOQ", 0, 0, 0), createJointValue("dB_OpenMouthL", 0, 0, 0),
                        createJointValue("dB_OpenMouthE", 0, 0, 0)));
        assertEquals(0.1, f0.getFtime(), 0.001);

        Frame f1 = ph.getFrames().get(1);
        assertThat(
                f1.getPosture().getJointValues(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(createJointValue("dB_Smile", 80, 0, 0),
                        createJointValue("dB_OpenMouthWOOQ", 1, 0, 0), createJointValue("dB_OpenMouthL", 0, 1, 0),
                        createJointValue("dB_OpenMouthE", 0, 0, 1)));
        assertEquals(0.2, f1.getFtime(), 0.001);

    }
}
