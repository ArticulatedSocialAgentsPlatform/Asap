/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the TimedMotionUnitSpec
 * @author hvanwelbergen
 *
 */
public class TimedMotionUnitSpecTest
{
    @Test
    public void test()
    {
        //@formatter:off
        String xmlString=
        "<TimedMotionUnitSpec type=\"gesture\">"+
        "<constraints>"+
        "  <constraint name=\"lexeme\" value=\"wave\"/>"+
        "</constraints>"+
        "<parametermap>"+
        "</parametermap>"+
        "<parameterdefaults>"+
        "</parameterdefaults>"+
        "<TimedMotionUnit type=\"MURML\">"+
        "<test> </test>"+
        "</TimedMotionUnit>"+
        "</TimedMotionUnitSpec>";
        //@formatter:on
        TimedMotionUnitSpec spec = new TimedMotionUnitSpec();
        spec.readXML(xmlString);
        assertEquals("gesture",spec.getType());
        assertEquals("MURML",spec.getTimedMotionUnitConstructionInfo().getType());
        assertEquals("<test> </test>",spec.getTimedMotionUnitConstructionInfo().getContent());
        
    }
}
