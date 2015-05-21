/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.facebinding;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import hmi.faceanimation.FaceController;

import org.junit.Test;
import org.mockito.AdditionalMatchers;

import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.MURMLKeyframeMorphFU;
import asap.motionunit.MUPlayException;

/**
 * Unit test cases for the MURMLFUBuilder
 * @author hvanwelbergen
 * 
 */
public class MURMLFUBuilderTest
{
    FaceController mockFc = mock(FaceController.class);

    @Test
    public void testSingleFrame() throws MUPlayException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)</posture></frame></phase></keyframing></dynamic></murml-description>";
        FaceUnit fu = MURMLFUBuilder.setup(murmlString);
        assertThat(fu, instanceOf(MURMLKeyframeMorphFU.class));
        MURMLKeyframeMorphFU kfu = (MURMLKeyframeMorphFU) fu;
        kfu.setFaceController(mockFc);
        kfu.play(0);

        final String[] expectedTargets = new String[] { "dB_Smile" };
        final float[] expectedValues = new float[] { 0.7f };
        verify(mockFc, times(1)).addMorphTargets(AdditionalMatchers.aryEq(expectedTargets), AdditionalMatchers.aryEq(expectedValues));
    }

    @Test
    public void testTwoFrames() throws MUPlayException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase>" + "<frame ftime=\"0\"><posture>Humanoid " + "(dB_Smile 3 70 0 0)</posture></frame>"
                + "<frame ftime=\"1\"><posture>Humanoid " + "(dB_Smile 3 80 0 0)</posture></frame>"
                + "</phase></keyframing></dynamic></murml-description>";
        FaceUnit fu = MURMLFUBuilder.setup(murmlString);
        assertThat(fu, instanceOf(MURMLKeyframeMorphFU.class));
        MURMLKeyframeMorphFU kfu = (MURMLKeyframeMorphFU) fu;
        kfu.setFaceController(mockFc);
        kfu.play(1);

        final String[] expectedTargets = new String[] { "dB_Smile" };
        final float[] expectedValues = new float[] { 0.8f };
        verify(mockFc, times(1)).addMorphTargets(AdditionalMatchers.aryEq(expectedTargets), AdditionalMatchers.aryEq(expectedValues));
    }

    @Test
    public void testTwoFramesTwoTargets() throws MUPlayException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\"><dynamic><keyframing><phase>"
                + "<frame ftime=\"0\"><posture>Humanoid " + "(dB_Smile 3 70 0 0)(dB_Dummy 1 30)</posture></frame>"
                + "<frame ftime=\"1\"><posture>Humanoid " + "(dB_Smile 3 80 0 0)(dB_Dummy 1 20)</posture></frame>"
                + "</phase></keyframing></dynamic></murml-description>";
        FaceUnit fu = MURMLFUBuilder.setup(murmlString);
        assertThat(fu, instanceOf(MURMLKeyframeMorphFU.class));
        MURMLKeyframeMorphFU kfu = (MURMLKeyframeMorphFU) fu;
        kfu.setFaceController(mockFc);
        kfu.play(1);

        final String[] expectedTargets = new String[] { "dB_Smile", "dB_Dummy" };
        final float[] expectedValues = new float[] { 0.8f, 0.2f };
        verify(mockFc, times(1)).addMorphTargets(AdditionalMatchers.aryEq(expectedTargets), AdditionalMatchers.aryEq(expectedValues));
    }

    @Test
    public void testUnification() throws MUPlayException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase>" + "<frame ftime=\"1\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)(dB_Dummy 1 30)</posture></frame>" + "<frame ftime=\"4\"><posture>Humanoid "
                + "(dB_Smile 3 80 0 0)(dB_Dummy 1 20)</posture></frame>" + "</phase></keyframing></dynamic></murml-description>";
        FaceUnit fu = MURMLFUBuilder.setup(murmlString);
        assertThat(fu, instanceOf(MURMLKeyframeMorphFU.class));
        MURMLKeyframeMorphFU kfu = (MURMLKeyframeMorphFU) fu;
        kfu.setFaceController(mockFc);
        kfu.play(1);

        final String[] expectedTargets = new String[] { "dB_Smile", "dB_Dummy" };
        final float[] expectedValues = new float[] { 0.8f, 0.2f };
        verify(mockFc, times(1)).addMorphTargets(AdditionalMatchers.aryEq(expectedTargets), AdditionalMatchers.aryEq(expectedValues));
    }

    @Test
    public void testFlexibleStartAtStart() throws MUPlayException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase>" + "<frame ftime=\"1\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)(dB_Dummy 1 30)</posture></frame>" + "<frame ftime=\"4\"><posture>Humanoid "
                + "(dB_Smile 3 80 0 0)(dB_Dummy 1 20)</posture></frame>" + "</phase></keyframing></dynamic></murml-description>";
        FaceUnit fu = MURMLFUBuilder.setup(murmlString);
        assertThat(fu, instanceOf(MURMLKeyframeMorphFU.class));
        MURMLKeyframeMorphFU kfu = (MURMLKeyframeMorphFU) fu;
        kfu.setFaceController(mockFc);
        kfu.startUnit(0);
        kfu.play(0);

        final String[] expectedTargets = new String[] { "dB_Smile", "dB_Dummy" };
        final float[] expectedValues = new float[] { 0.0f, 0.0f };
        verify(mockFc, times(1)).addMorphTargets(AdditionalMatchers.aryEq(expectedTargets), AdditionalMatchers.aryEq(expectedValues));
    }

    @Test
    public void testFlexibleStartAtEnd() throws MUPlayException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase>" + "<frame ftime=\"1\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)(dB_Dummy 1 30)</posture></frame>" + "<frame ftime=\"4\"><posture>Humanoid "
                + "(dB_Smile 3 80 0 0)(dB_Dummy 1 20)</posture></frame>" + "</phase></keyframing></dynamic></murml-description>";
        FaceUnit fu = MURMLFUBuilder.setup(murmlString);
        assertThat(fu, instanceOf(MURMLKeyframeMorphFU.class));
        MURMLKeyframeMorphFU kfu = (MURMLKeyframeMorphFU) fu;
        kfu.setFaceController(mockFc);
        kfu.startUnit(0);
        kfu.play(1);

        final String[] expectedTargets = new String[] { "dB_Smile", "dB_Dummy" };
        final float[] expectedValues = new float[] { 0.8f, 0.2f };
        verify(mockFc, times(1)).addMorphTargets(AdditionalMatchers.aryEq(expectedTargets), AdditionalMatchers.aryEq(expectedValues));
    }

    @Test
    public void testFlexibleStartHalfWay() throws MUPlayException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing mode=\"linear\"><phase>" + "<frame ftime=\"3\"><posture>Humanoid "
                + "(dB_Smile 3 70 0 0)(dB_Dummy 1 30)</posture></frame>" + "</phase></keyframing></dynamic></murml-description>";
        FaceUnit fu = MURMLFUBuilder.setup(murmlString);
        assertThat(fu, instanceOf(MURMLKeyframeMorphFU.class));
        MURMLKeyframeMorphFU kfu = (MURMLKeyframeMorphFU) fu;
        kfu.setFaceController(mockFc);
        kfu.startUnit(0);
        kfu.play(0.5);

        final String[] expectedTargets = new String[] { "dB_Smile", "dB_Dummy" };
        final float[] expectedValues = new float[] { 0.35f, 0.15f };
        verify(mockFc, times(1)).addMorphTargets(AdditionalMatchers.aryEq(expectedTargets), AdditionalMatchers.aryEq(expectedValues));
    }
}
