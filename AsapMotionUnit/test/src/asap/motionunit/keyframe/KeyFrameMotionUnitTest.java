/*******************************************************************************
 *******************************************************************************/
package asap.motionunit.keyframe;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Test;

import asap.motionunit.MUPlayException;

import com.google.common.collect.ImmutableList;

/**
 * Unit test cases for the KeyFrameMotionUnit
 * @author hvanwelbergen
 * 
 */
public class KeyFrameMotionUnitTest
{
    private Interpolator mockInterpolator = mock(Interpolator.class);
    private static final double TIME_PRECISION = 0.0001d;

    static class KeyFrameMotionUnitStub extends KeyFrameMotionUnit
    {

        public KeyFrameMotionUnitStub(Interpolator interp)
        {
            super(interp);
        }

        @Override
        public void startUnit(double t) throws MUPlayException
        {

        }

        @Override
        public double getPreferedDuration()
        {
            return 0;
        }

        @Override
        public void applyKeyFrame(KeyFrame kf)
        {
        }

        @Override
        public KeyFrame getStartKeyFrame()
        {
            return null;
        }

    }

    @Test
    public void testUnify()
    {
        KeyFrameMotionUnitStub stubKMU = new KeyFrameMotionUnitStub(mockInterpolator);
        float val1[]={0};
        float val2[]={1};
        float val3[]={3};        
        List<KeyFrame> kfList = ImmutableList.of(new KeyFrame(0,val1), new KeyFrame(1,val2), new KeyFrame(4,val3));
        assertEquals(4,stubKMU.unifyKeyFrames(kfList), TIME_PRECISION);
        assertEquals(0, kfList.get(0).getFrameTime(),TIME_PRECISION);
        assertEquals(0.25, kfList.get(1).getFrameTime(),TIME_PRECISION);
        assertEquals(1, kfList.get(2).getFrameTime(),TIME_PRECISION);
    }
    
    @Test
    public void testUnifyFlexibleStart()
    {
        KeyFrameMotionUnitStub stubKMU = new KeyFrameMotionUnitStub(mockInterpolator);
        float val1[]={0};
        float val2[]={1};
        float val3[]={3};        
        List<KeyFrame> kfList = ImmutableList.of(new KeyFrame(1,val1), new KeyFrame(2,val2), new KeyFrame(4,val3));
        assertEquals(4,stubKMU.unifyKeyFrames(kfList), TIME_PRECISION);
        assertEquals(0.25, kfList.get(0).getFrameTime(),TIME_PRECISION);
        assertEquals(0.5, kfList.get(1).getFrameTime(),TIME_PRECISION);
        assertEquals(1, kfList.get(2).getFrameTime(),TIME_PRECISION);
    }
    
    @Test
    public void testUnifyFlexibleStartOneFrame()
    {
        KeyFrameMotionUnitStub stubKMU = new KeyFrameMotionUnitStub(mockInterpolator);
        float val1[]={0};
        List<KeyFrame> kfList = ImmutableList.of(new KeyFrame(2,val1));
        assertEquals(2,stubKMU.unifyKeyFrames(kfList), TIME_PRECISION);
        assertEquals(1, kfList.get(0).getFrameTime(),TIME_PRECISION);        
    }
}
