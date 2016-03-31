/*******************************************************************************
 *******************************************************************************/
package asap.motionunit;

import static org.mockito.Matchers.doubleThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hamcrest.number.IsCloseTo;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Unit tests for the MUSequence
 * @author hvanwelbergen
 * 
 */
public class MUSequenceTest
{
    private MotionUnit mockMU1 = mock(MotionUnit.class);
    private MotionUnit mockMU2 = mock(MotionUnit.class);
    private MotionUnit mockMU3 = mock(MotionUnit.class);
    private MUSequence muSequence;
    private static final double MU1_DURATION = 1;
    private static final double MU2_DURATION = 2;
    private static final double MU3_DURATION = 3;
    private static final double EPSILON = 0.001;
    @Before
    public void setup()
    {
        when(mockMU1.getPreferedDuration()).thenReturn(MU1_DURATION);
        when(mockMU2.getPreferedDuration()).thenReturn(MU2_DURATION);
        when(mockMU3.getPreferedDuration()).thenReturn(MU3_DURATION);
        muSequence = new MUSequence(ImmutableList.of(mockMU1, mockMU2, mockMU3));
    }

    @Test
    public void testPlay0() throws MUPlayException
    {
        muSequence.play(0);
        verify(mockMU1).play(0);
    }
    
    @Test
    public void testPlay1() throws MUPlayException
    {
        muSequence.play(0.9999);
        verify(mockMU3).play(doubleThat(IsCloseTo.closeTo(1,EPSILON)));
    }
    
    @Test
    public void testPlay0_5() throws MUPlayException
    {
        muSequence.play(0.5);
        verify(mockMU3).play(0);
    }
}
