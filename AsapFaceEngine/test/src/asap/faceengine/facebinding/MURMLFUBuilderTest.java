package asap.faceengine.facebinding;

import hmi.faceanimation.FaceController;

import org.junit.Test;
import org.mockito.AdditionalMatchers;

import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.KeyframeMorphFU;
import asap.motionunit.MUPlayException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cases for the MURMLFUBuilder
 * @author hvanwelbergen
 * 
 */
public class MURMLFUBuilderTest
{
    FaceController mockFc = mock(FaceController.class);

    @Test
    public void test() throws MUPlayException
    {
        String murmlString = "<definition><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)</posture></frame></phase></keyframing></definition>";
        FaceUnit fu = MURMLFUBuilder.setup(murmlString);
        assertThat(fu, instanceOf(KeyframeMorphFU.class));
        KeyframeMorphFU kfu = (KeyframeMorphFU) fu;
        kfu.setFaceController(mockFc);
        kfu.play(0);

        final String[] expectedTargets = new String[] { "dB_Smile" };
        final float[] expectedValues = new float[] {70};
        verify(mockFc, times(1)).addMorphTargets(AdditionalMatchers.aryEq(expectedTargets), AdditionalMatchers.aryEq(expectedValues));
    }
}
