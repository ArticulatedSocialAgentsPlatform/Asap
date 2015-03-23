/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the ErfManipulator
 * @author Herwin
 *
 */
public class ErfManipulatorTest extends AbstractTimeManipulatorTest
{
    @Override
    protected ErfManipulator getManipulator()
    {
        return new ErfManipulator(20);
    }
    
    @Test
    public void testHalf()
    {
        ErfManipulator m = getManipulator();
        assertEquals(0.5, m.manip(0.5), MANIP_PRECISION);
    }
    
    @Test
    public void testQuarter()
    {
        ErfManipulator m = getManipulator();
        assertEquals(0, m.manip(0.25), MANIP_PRECISION);
    }
    
    @Test
    public void testThreeQuarter()
    {
        ErfManipulator m = getManipulator();
        assertEquals(1, m.manip(0.75), MANIP_PRECISION);
    }
}
