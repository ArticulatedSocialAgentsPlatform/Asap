/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for ease in ease out manipulator
 * @author hvanwelbergen
 * 
 */
public class EaseInEaseOutManipulatorTest extends AbstractTimeManipulatorTest
{
    @Override
    protected TimeManipulator getManipulator()
    {
        return new EaseInEaseOutManipulator(2,0.5);
    }

    @Test
    public void testLinear()
    {
        EaseInEaseOutManipulator m = new EaseInEaseOutManipulator(0,0);
        assertEquals(1, m.manip(1), MANIP_PRECISION);
        assertEquals(0, m.manip(0), MANIP_PRECISION);
        assertEquals(0.5, m.manip(0.5), MANIP_PRECISION);
    }

}
