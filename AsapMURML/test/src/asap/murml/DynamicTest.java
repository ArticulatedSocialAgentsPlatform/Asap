package asap.murml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import asap.murml.Keyframing.Mode;

/**
 * Unit tests for Dynamic
 * @author hvanwelbergen
 *
 */
public class DynamicTest
{
    private static final double PARAMETER_PRECISION = 0.0001;

    @Test
    public void testReadDynamicWithKeyframe()
    {
        Dynamic dynamic = new Dynamic();
        dynamic.readXML("<dynamic xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\"><keyframing mode=\"spline\" priority=\"100\" easescale=\"10\">"
                + "<phase>"
                + "<frame ftime=\"0.1\"><posture>Humanoid (dB_Smile 3 70 0 0) (dB_OpenMouthWOOQ 3 0 0 0) "
                + "(dB_OpenMouthL 3 0 0 0) (dB_OpenMouthE 3 0 0 0)</posture>"
                + "</frame>"
                + "<frame ftime=\"0.2\"><posture>Humanoid (dB_Smile 3 80 0 0) (dB_OpenMouthWOOQ 3 1 0 0) "
                + "(dB_OpenMouthL 3 0 1 0) (dB_OpenMouthE 3 0 0 1)</posture>" + "</frame>" + "</phase></keyframing></dynamic>");
        assertNotNull(dynamic.getKeyframing());
        Keyframing kf = dynamic.getKeyframing();
        assertEquals(Mode.SPLINE, kf.getMode());
        assertEquals(100, kf.getPriority());
        assertEquals(10, kf.getEasescale(), PARAMETER_PRECISION);
    }
    
    @Test
    public void testWriteDynamicWithKeyframe()
    {
        Dynamic dynamicIn = new Dynamic();
        dynamicIn.readXML("<dynamic xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\"><keyframing mode=\"spline\" priority=\"100\" easescale=\"10\">"
                + "<phase>"
                + "<frame ftime=\"0.1\"><posture>Humanoid (dB_Smile 3 70 0 0) (dB_OpenMouthWOOQ 3 0 0 0) "
                + "(dB_OpenMouthL 3 0 0 0) (dB_OpenMouthE 3 0 0 0)</posture>"
                + "</frame>"
                + "<frame ftime=\"0.2\"><posture>Humanoid (dB_Smile 3 80 0 0) (dB_OpenMouthWOOQ 3 1 0 0) "
                + "(dB_OpenMouthL 3 0 1 0) (dB_OpenMouthE 3 0 0 1)</posture>" + "</frame>" + "</phase></keyframing></dynamic>");
        StringBuilder buf = new StringBuilder();
        dynamicIn.appendXML(buf);
        
        Dynamic dynamicOut = new Dynamic();
        dynamicOut.readXML(buf.toString());
        assertNotNull(dynamicOut.getKeyframing());
        Keyframing kf = dynamicOut.getKeyframing();
        assertEquals(Mode.SPLINE, kf.getMode());
        assertEquals(100, kf.getPriority());
        assertEquals(10, kf.getEasescale(), PARAMETER_PRECISION);        
    }
}
