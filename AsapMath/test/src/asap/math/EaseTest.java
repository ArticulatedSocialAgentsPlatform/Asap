package asap.math;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for ease in ease out
 * @author hvanwelbergen
 * 
 */
public class EaseTest
{
    private static final double EASE_PRECISION = 0.001;

    @Test
    public void testNull()
    {
        assertEquals(0,Ease.ease(0,1,1),EASE_PRECISION);
    }
    
    @Test
    @Ignore
    public void testOne()
    {
        assertEquals(1,Ease.ease(1,1,1),EASE_PRECISION);
    }
}
