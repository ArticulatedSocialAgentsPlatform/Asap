package asap.murml;

import static asap.murml.testutil.MURMLTestUtil.createJointValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

/**
 * Unit testcases for the MURML definition element
 * @author hvanwelbergen
 * 
 */
public class DefinitionTest
{
    private static final double PARAMETER_PRECISION = 0.0001;

    @Test
    public void testEmptyDefinition()
    {
        Definition d = new Definition();
        d.readXML("<definition xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\"/>");
        assertNull(d.getPosture());
        assertNull(d.getKeyframing());
    }

    @Test
    public void testPostureDefinition()
    {
        Definition d = new Definition();
        d.readXML("<definition xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<posture>Humanoid (dB_Smile 3 70 0 0) (dB_OpenMouthWOOQ 3 0 0 0) "
                + "(dB_OpenMouthL 3 0 0 0) (dB_OpenMouthE 3 0 0 0)</posture></definition>");
        assertThat(
                d.getPosture().getJointValues(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(createJointValue("dB_Smile", 70, 0, 0),
                        createJointValue("dB_OpenMouthWOOQ", 0, 0, 0), createJointValue("dB_OpenMouthL", 0, 0, 0),
                        createJointValue("dB_OpenMouthE", 0, 0, 0)));
        assertNull(d.getKeyframing());
    }

    @Test
    public void testKeyframingDefinition()
    {
        Definition d = new Definition();
        d.readXML("<definition xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\"><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)</posture></frame></phase></keyframing></definition>");
        assertNull(d.getPosture());
        assertEquals(0, d.getKeyframing().getPhases().get(0).getFrames().get(0).getFtime(), PARAMETER_PRECISION);
    }
}
