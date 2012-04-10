package asap.animationengine.gesturebinding;

import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.testutil.animation.HanimBody;
import hmi.testutil.math.Quat4fTestUtil;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.keyframe.MURMLKeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.motionunit.MUPlayException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testcases for the MURML MotionUnit builder
 * @author Herwin
 */
public class MURMLMUBuilderTest
{
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private VJoint vNext = HanimBody.getLOA1HanimBody();
    private static final float ROT_PRECISION = 0.001f;

    @Before
    public void setup()
    {
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
    }

    @Test
    public void testSingleFrame() throws MUPlayException, MUSetupException
    {
        String murmlString = "<definition><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(l_shoulder 3 100 0 0)</posture></frame></phase></keyframing></definition>";
        AnimationUnit au = MURMLMUBuilder.setup(murmlString);
        assertThat(au, instanceOf(MURMLKeyframeMU.class));
        MURMLKeyframeMU mu = (MURMLKeyframeMU) au;
        au = mu.copy(mockAnimationPlayer);
        au.play(0);

        float[] qExp = Quat4f.getQuat4f();
        Quat4f.setFromRollPitchYawDegrees(qExp, 100, 0, 0);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExp, q, ROT_PRECISION);
    }

    @Test
    public void testTwoFrames() throws MUPlayException, MUSetupException
    {
        String murmlString = "<definition><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(l_shoulder 3 100 0 0)</posture></frame><frame ftime=\"1\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)</posture></frame></phase></keyframing></definition>";
        AnimationUnit au = MURMLMUBuilder.setup(murmlString);
        assertThat(au, instanceOf(MURMLKeyframeMU.class));
        MURMLKeyframeMU mu = (MURMLKeyframeMU) au;
        au = mu.copy(mockAnimationPlayer);
        au.play(1);

        float[] qExp = Quat4f.getQuat4f();
        Quat4f.setFromRollPitchYawDegrees(qExp, 0, 80, 0);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExp, q, ROT_PRECISION);
    }

    @Test
    public void testTwoFramesTwoTargets() throws MUPlayException, MUSetupException
    {
        String murmlString = "<definition><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(l_shoulder 3 100 0 0)(r_shoulder 3 0 0 100)</posture></frame><frame ftime=\"1\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)(r_shoulder 3 80 0 0)</posture></frame></phase></keyframing></definition>";
        AnimationUnit au = MURMLMUBuilder.setup(murmlString);
        assertThat(au, instanceOf(MURMLKeyframeMU.class));
        MURMLKeyframeMU mu = (MURMLKeyframeMU) au;
        au = mu.copy(mockAnimationPlayer);
        au.play(1);

        float[] qExp = Quat4f.getQuat4f();
        Quat4f.setFromRollPitchYawDegrees(qExp, 0, 80, 0);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExp, q, ROT_PRECISION);

        Quat4f.setFromRollPitchYawDegrees(qExp, 80, 0, 0);
        vNext.getPart("r_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExp, q, ROT_PRECISION);
    }

    @Test
    public void testUnification() throws MUPlayException, MUSetupException
    {
        String murmlString = "<definition><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(l_shoulder 3 100 0 0)</posture></frame><frame ftime=\"4\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)</posture></frame></phase></keyframing></definition>";
        AnimationUnit au = MURMLMUBuilder.setup(murmlString);
        assertThat(au, instanceOf(MURMLKeyframeMU.class));
        MURMLKeyframeMU mu = (MURMLKeyframeMU) au;
        au = mu.copy(mockAnimationPlayer);
        au.play(1);

        float[] qExp = Quat4f.getQuat4f();
        Quat4f.setFromRollPitchYawDegrees(qExp, 0, 80, 0);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExp, q, ROT_PRECISION);
    }

    @Test
    public void testFlexibleStartAtStart() throws MUPlayException, MUSetupException
    {
        String murmlString = "<definition><keyframing><phase>" + "<frame ftime=\"4\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)</posture></frame></phase></keyframing></definition>";
        AnimationUnit au = MURMLMUBuilder.setup(murmlString);
        au = au.copy(mockAnimationPlayer);

        float[] qRefStart = Quat4f.getQuat4f();
        Quat4f.setFromAxisAngle4f(qRefStart, 0, 0, 1, (float) Math.PI);
        vNext.getPart("l_shoulder").setRotation(qRefStart);
        au.startUnit(0);
        au.play(0);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qRefStart, q, ROT_PRECISION);
    }

    @Test
    public void testFlexibleStartAtEnd() throws MUPlayException, MUSetupException
    {
        String murmlString = "<definition><keyframing><phase>" + "<frame ftime=\"4\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)</posture></frame></phase></keyframing></definition>";
        AnimationUnit au = MURMLMUBuilder.setup(murmlString);
        au = au.copy(mockAnimationPlayer);

        float[] qRefStart = Quat4f.getQuat4f();
        Quat4f.setFromAxisAngle4f(qRefStart, 0, 0, 1, (float) Math.PI);
        vNext.getPart("l_shoulder").setRotation(qRefStart);
        au.startUnit(0);
        au.play(1);
        float[] qExp = Quat4f.getQuat4f();
        Quat4f.setFromRollPitchYawDegrees(qExp, 0, 80, 0);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExp, q, ROT_PRECISION);
    }

    @Test
    public void testFlexibleStartHalfWay() throws MUPlayException, MUSetupException
    {
        String murmlString = "<definition><keyframing><phase>" + "<frame ftime=\"4\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)</posture></frame></phase></keyframing></definition>";
        AnimationUnit au = MURMLMUBuilder.setup(murmlString);
        au = au.copy(mockAnimationPlayer);

        float[] qRefStart = Quat4f.getQuat4f();
        Quat4f.setFromAxisAngle4f(qRefStart, 0, 0, 1, (float) Math.PI);
        vNext.getPart("l_shoulder").setRotation(qRefStart);
        au.startUnit(0);
        au.play(0.5);
        float[] qRefEnd = Quat4f.getQuat4f();
        Quat4f.setFromRollPitchYawDegrees(qRefEnd, 0, 80, 0);
        float[] qExp = Quat4f.getQuat4f();
        Quat4f.interpolate(qExp, qRefStart, qRefEnd, 0.5f);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExp, q, ROT_PRECISION);
    }
}
