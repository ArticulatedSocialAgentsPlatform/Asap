/*******************************************************************************
 *******************************************************************************/
package asap.timemanipulator;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Abstract class to test the contract of all TimeManipulators
 * @author hvanwelbergen
 */
public abstract class AbstractTimeManipulatorTest
{
    protected TimeManipulator manip;
    protected static final double MANIP_PRECISION = 0.02;
    
    protected abstract TimeManipulator getManipulator();
    
    @Before
    public void setup()
    {
        manip = getManipulator();
    }
    
    @Test
    public void testNull()
    {
        assertEquals(0,manip.manip(0),MANIP_PRECISION);
    }
    
    @Test
    public void testOne()
    {
        assertEquals(1,manip.manip(1),MANIP_PRECISION);
    }
    
    
}
