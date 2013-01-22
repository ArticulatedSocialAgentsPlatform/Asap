package asap.animationengine.gesturebinding;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.fest.reflect.core.Reflection.field;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.Hanim;
import hmi.animation.SkeletonPose;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.testutil.animation.HanimBody;
import hmi.testutil.math.Quat4fTestUtil;

import java.util.List;
import java.util.Set;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.ace.GuidingSequence;
import asap.animationengine.ace.lmp.LMPPoRot;
import asap.animationengine.ace.lmp.LMPWristPos;
import asap.animationengine.ace.lmp.MotorControlProgram;
import asap.animationengine.keyframe.MURMLKeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.hns.Hns;
import asap.hns.ShapeSymbols;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.BMLBlockManager;

/**
 * Testcases for the MURML MotionUnit builder
 * @author Herwin
 */
public class MURMLMUBuilderTest
{
    private AnimationPlayer mockAnimationPlayer = mock(AnimationPlayer.class);
    private AnimationUnit mockMu = mock(AnimationUnit.class);
    private VJoint vNext = HanimBody.getLOA2HanimBody();
    private VJoint vCurr = HanimBody.getLOA2HanimBody();
    private static final float ROT_PRECISION = 0.001f;
    private Hns mockHns = mock(Hns.class);
    private SkeletonPose mockSkeletonPose = mock(SkeletonPose.class);
    private HnsHandshape mockHnsHandshapes = mock(HnsHandshape.class);
    private MURMLMUBuilder murmlMuBuilder = new MURMLMUBuilder(mockHns, mockHnsHandshapes);
    private PegBoard pb = new PegBoard();
    private static final float POSITION_PRECISION = 0.001f;

    @SuppressWarnings("unchecked")
    @Before
    public void setup()
    {
        when(mockAnimationPlayer.getVNext()).thenReturn(vNext);
        when(mockAnimationPlayer.getVCurr()).thenReturn(vCurr);
        when(mockAnimationPlayer.createTransitionToRest(any(Set.class))).thenReturn(mockMu);
        when(mockHns.getHandLocation(anyString(), any(float[].class))).thenReturn(true);
        when(mockHns.getAbsoluteDirection(startsWith("Palm"), any(float[].class))).thenReturn(false);
        when(mockHns.getAbsoluteDirection(startsWith("Dir"), any(float[].class))).thenReturn(true);
        when(mockHns.isPalmOrientation(startsWith("Palm"))).thenReturn(true);
        when(mockHns.getElementShape(anyString())).thenReturn(ShapeSymbols.LeftC);
        when(mockHnsHandshapes.getHNSHandShape(anyString())).thenReturn(mockSkeletonPose);
    }

    @Test
    public void testSingleFrame() throws MUPlayException, MUSetupException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(l_shoulder 3 100 0 0)</posture></frame></phase></keyframing></dynamic></murml-description>";
        AnimationUnit au = murmlMuBuilder.setup(murmlString);
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
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(l_shoulder 3 100 0 0)</posture></frame><frame ftime=\"1\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)</posture></frame></phase></keyframing></dynamic></murml-description>";
        AnimationUnit au = murmlMuBuilder.setup(murmlString);
        assertThat(au, instanceOf(MURMLKeyframeMU.class));
        MURMLKeyframeMU mu = (MURMLKeyframeMU) au;
        mu = mu.copy(mockAnimationPlayer);
        mu.setupRelaxUnit();
        mu.play(mu.getKeyPosition("relax").time);

        float[] qExp = Quat4f.getQuat4f();
        Quat4f.setFromRollPitchYawDegrees(qExp, 0, 80, 0);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExp, q, ROT_PRECISION);
    }

    @Test
    public void testTwoFramesTwoTargets() throws MUPlayException, MUSetupException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(l_shoulder 3 100 0 0)(r_shoulder 3 0 0 100)</posture></frame><frame ftime=\"1\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)(r_shoulder 3 80 0 0)</posture></frame></phase></keyframing></dynamic></murml-description>";
        AnimationUnit au = murmlMuBuilder.setup(murmlString);
        assertThat(au, instanceOf(MURMLKeyframeMU.class));
        MURMLKeyframeMU mu = (MURMLKeyframeMU) au;
        mu = mu.copy(mockAnimationPlayer);
        mu.setupRelaxUnit();
        mu.play(mu.getKeyPosition("relax").time);

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
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(l_shoulder 3 100 0 0)</posture></frame><frame ftime=\"4\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)</posture></frame></phase></keyframing></dynamic></murml-description>";
        AnimationUnit au = murmlMuBuilder.setup(murmlString);
        assertThat(au, instanceOf(MURMLKeyframeMU.class));
        MURMLKeyframeMU mu = (MURMLKeyframeMU) au;
        au = mu.copy(mockAnimationPlayer);
        au.play(mu.getKeyPosition("relax").time);

        float[] qExp = Quat4f.getQuat4f();
        Quat4f.setFromRollPitchYawDegrees(qExp, 0, 80, 0);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExp, q, ROT_PRECISION);
    }

    @Test
    public void testFlexibleStartAtStart() throws MUPlayException, MUSetupException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase>" + "<frame ftime=\"4\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)</posture></frame></phase></keyframing></dynamic></murml-description>";
        AnimationUnit au = murmlMuBuilder.setup(murmlString);
        au = au.copy(mockAnimationPlayer);

        float[] qRefStart = Quat4f.getQuat4f();
        Quat4f.setFromAxisAngle4f(qRefStart, 0, 0, 1, (float) Math.PI);
        vCurr.getPart("l_shoulder").setRotation(qRefStart);
        au.startUnit(0);
        au.play(0);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qRefStart, q, ROT_PRECISION);
    }

    @Test
    public void testFlexibleStartAtEnd() throws MUPlayException, MUSetupException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase>" + "<frame ftime=\"4\"><posture>Humanoid "
                + "(l_shoulder 3 0 80 0)</posture></frame></phase></keyframing></dynamic></murml-description>";
        AnimationUnit au = murmlMuBuilder.setup(murmlString);
        au = au.copy(mockAnimationPlayer);

        float[] qRefStart = Quat4f.getQuat4f();
        Quat4f.setFromAxisAngle4f(qRefStart, 0, 0, 1, (float) Math.PI);
        vNext.getPart("l_shoulder").setRotation(qRefStart);
        au.startUnit(0);
        au.play(au.getKeyPosition("relax").time);
        float[] qExp = Quat4f.getQuat4f();
        Quat4f.setFromRollPitchYawDegrees(qExp, 0, 80, 0);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExp, q, ROT_PRECISION);
    }

    @Test
    public void testFlexibleStartHalfWay() throws MUPlayException, MUSetupException
    {
        // @formatter:off
        String murmlString = 
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" + 
                        "<dynamic><keyframing><phase>" + 
                            "<frame ftime=\"4\"><posture>Humanoid " + 
                                "(l_shoulder 3 0 80 0)</posture>" +
                            "</frame>"+
                        "</phase></keyframing></dynamic>" +
                "</murml-description>";
        // @formatter:on

        AnimationUnit au = murmlMuBuilder.setup(murmlString);
        au = au.copy(mockAnimationPlayer);

        float[] qRefStart = Quat4f.getQuat4f();
        Quat4f.setFromAxisAngle4f(qRefStart, 0, 0, 1, (float) Math.PI);
        vCurr.getPart("l_shoulder").setRotation(qRefStart);
        au.startUnit(0);
        au.play(0.5 * au.getKeyPosition("relax").time);
        float[] qRefEnd = Quat4f.getQuat4f();
        Quat4f.setFromRollPitchYawDegrees(qRefEnd, 0, 80, 0);
        float[] qExp = Quat4f.getQuat4f();
        Quat4f.interpolate(qExp, qRefStart, qRefEnd, 0.5f);
        float q[] = Quat4f.getQuat4f();
        vNext.getPart("l_shoulder").getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExp, q, ROT_PRECISION);
    }

    @Test
    public void setupTMUStaticHandLocation() throws TMUSetupException
    {
        //@formatter:off
        String murmlString =
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                "<static slot=\"HandLocation\" value=\"LocShoulder LocCenterRight LocNorm\" scope=\"left_arm\"/>"+
                "</murml-description>";
        // @formatter:on

        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.l_shoulder, Hanim.l_elbow));
    }

    @Test
    public void setupTMUHandLocation() throws TMUSetupException
    {
        //@formatter:off
        String murmlString =
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<dynamic slot=\"HandLocation\" scope=\"left_arm\">"+
                        "<dynamicElement type=\"curve\">"+        
                            "<value type=\"start\" name=\"LocShoulder LocCenterLeft LocFar\"/>"+
                            "<value type=\"end\" name=\"LocShoulder LocCenterLeft LocNorm\"/>"+
                            "<value type=\"normal\" name=\"DirU\"/>"+
                            "<value type=\"shape\" name=\"LeftC\"/>"+
                            "<value type=\"extension\" name=\"0.6\"/>"+                                                        
                        "</dynamicElement>"+
                        "<dynamicElement type=\"curve\">"+
                            "<value type=\"start\" name=\"LocShoulder LocCenterLeft LocNorm\"/>"+
                            "<value type=\"end\" name=\"LocShoulder LocCenterLeft LocFar\"/>"+
                            "<value type=\"normal\" name=\"DirU\"/>"+
                            "<value type=\"shape\" name=\"LeftC\"/>"+
                            "<value type=\"extension\" name=\"0.6\"/>"+
                        "</dynamicElement>"+
                        "<dynamicElement type=\"curve\">"+
                            "<value type=\"start\" name=\"LocShoulder LocCenterLeft LocFar\"/>"+
                            "<value type=\"end\" name=\"LocShoulder LocCenterLeft LocNorm\"/>"+
                            "<value type=\"normal\" name=\"DirU\"/>"+
                            "<value type=\"shape\" name=\"LeftC\"/>"+
                            "<value type=\"extension\" name=\"0.6\"/>"+
                        "</dynamicElement>"+
                        "<dynamicElement type=\"curve\">"+
                            "<value type=\"start\" name=\"LocShoulder LocCenterLeft LocNorm\"/>"+
                            "<value type=\"end\" name=\"LocShoulder LocCenterLeft LocFar\"/>"+
                            "<value type=\"normal\" name=\"DirU\"/>"+
                            "<value type=\"shape\" name=\"LeftC\"/>"+
                            "<value type=\"extension\" name=\"0.6\"/>"+
                       "</dynamicElement>"+
                        "</dynamic>"+
                "</murml-description>";
        // @formatter:on
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.l_shoulder, Hanim.l_elbow));

        @SuppressWarnings("unchecked")
        List<TimedAnimationUnit> lmps = field("lmpQueue").ofType(List.class).in(tau).get();
        assertEquals(1, lmps.size());
        assertThat(lmps.get(0), instanceOf(LMPWristPos.class));
        LMPWristPos pos = (LMPWristPos) lmps.get(0);
        assertThat(pos.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.l_shoulder, Hanim.l_elbow));
        GuidingSequence gSeq = field("gSeq").ofType(GuidingSequence.class).in(pos).get();
        assertEquals(5, gSeq.size());
    }

    @Test
    public void setupTMUHandLocationLinear() throws TMUSetupException
    {
        //@formatter:off
        String murmlString =
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<dynamic slot=\"HandLocation\" scope=\"left_arm\">"+
                        "<dynamicElement type=\"linear\" scope=\"left_arm\">"+
                        "<value type=\"start\" name=\"0.2 1.8 0.2\"/>"+
                        "<value type=\"end\" name=\"0.5 1.8 0.2\"/>"+
                        "</dynamicElement>"+
                        "<dynamicElement type=\"linear\" scope=\"left_arm\">"+
                        "<value type=\"end\" name=\"0.2 1.5 0.2\"/>"+
                        "</dynamicElement>" +
                        "<dynamicElement type=\"linear\" scope=\"left_arm\">"+
                        "<value type=\"end\" name=\"0.2 1.5 0.5\"/>"+
                        "</dynamicElement>" +
                        "</dynamic>"+
                "</murml-description>";
        // @formatter:on
        MURMLMUBuilder murmlMuBuilder = new MURMLMUBuilder(new Hns(), mockHnsHandshapes);
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.l_shoulder, Hanim.l_elbow));

        @SuppressWarnings("unchecked")
        List<TimedAnimationUnit> lmps = field("lmpQueue").ofType(List.class).in(tau).get();
        assertEquals(1, lmps.size());
        assertThat(lmps.get(0), instanceOf(LMPWristPos.class));
        LMPWristPos pos = (LMPWristPos) lmps.get(0);
        assertThat(pos.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.l_shoulder, Hanim.l_elbow));
        GuidingSequence gSeq = field("gSeq").ofType(GuidingSequence.class).in(pos).get();
        assertEquals(4, gSeq.size());
        assertVec3fEquals(0.2f, 1.8f, 0.2f, gSeq.getStroke(0).getEndPos(), POSITION_PRECISION);
        assertVec3fEquals(0.5f, 1.8f, 0.2f, gSeq.getStroke(1).getEndPos(), POSITION_PRECISION);
        assertVec3fEquals(0.2f, 1.5f, 0.2f, gSeq.getStroke(2).getEndPos(), POSITION_PRECISION);
        assertVec3fEquals(0.2f, 1.5f, 0.5f, gSeq.getStroke(3).getEndPos(), POSITION_PRECISION);
    }

    @Test
    public void setupTMUPalmOrientation() throws TMUSetupException
    {
        //@formatter:off
        String murmlString = 
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<dynamic slot=\"PalmOrientation\" scope=\"right_arm\">"+
                        "<dynamicElement>"+
                              "<value type=\"start\" name=\"DirLU\"/>"+
                              "<value type=\"end\" name=\"DirU\"/>"+
                        "</dynamicElement>"+
                        "</dynamic>"+
                "</murml-description>";
        // @formatter:on
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.r_wrist));

        @SuppressWarnings("unchecked")
        List<TimedAnimationUnit> lmps = field("lmpQueue").ofType(List.class).in(tau).get();
        assertEquals(1, lmps.size());
    }

    @Test
    public void setupTMURelativeDynamicPalmOrientation() throws TMUSetupException
    {
        //@formatter:off
        String murmlString = 
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<dynamic slot=\"PalmOrientation\" scope=\"right_arm\">"+
                        "<dynamicElement>"+
                              "<value type=\"start\" name=\"PalmU\"/>"+
                              "<value type=\"end\" name=\"PalmU\"/>"+
                        "</dynamicElement>"+
                        "</dynamic>"+
                "</murml-description>";
        // @formatter:on
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.r_wrist));

        @SuppressWarnings("unchecked")
        List<TimedAnimationUnit> lmps = field("lmpQueue").ofType(List.class).in(tau).get();
        assertEquals(1, lmps.size());
        assertThat(lmps.get(0), instanceOf(LMPPoRot.class));
    }

    @Test
    public void setupTMUStaticPalmOrientation() throws TMUSetupException
    {
        //@formatter:off
        String murmlString = 
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<static slot=\"PalmOrientation\" scope=\"right_arm\" value=\"DirU\"/>"+
                "</murml-description>";
        // @formatter:on
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.r_wrist));

        @SuppressWarnings("unchecked")
        List<TimedAnimationUnit> lmps = field("lmpQueue").ofType(List.class).in(tau).get();
        assertEquals(1, lmps.size());
    }

    @Test
    public void setupTMURelativeStaticPalmOrientation() throws TMUSetupException
    {
        //@formatter:off
        String murmlString =
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<static slot=\"PalmOrientation\" scope=\"right_arm\" value=\"PalmU\"/>"+
                "</murml-description>";
        // @formatter:on
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.r_wrist));

        @SuppressWarnings("unchecked")
        List<TimedAnimationUnit> lmps = field("lmpQueue").ofType(List.class).in(tau).get();
        assertEquals(1, lmps.size());
        assertThat(lmps.get(0), instanceOf(LMPPoRot.class));
    }

    @Test
    public void setupTMUStaticPalmAndFingerOrientation() throws TMUSetupException
    {
        //@formatter:off
        String murmlString = 
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<parallel>"+
                        "<static slot=\"PalmOrientation\" scope=\"right_arm\" value=\"DirA\"/>"+
                        "<static slot=\"ExtFingerOrientation\" scope=\"right_arm\" value=\"DirU\"/>"+
                        "</parallel>"+
                "</murml-description>";
        // @formatter:on
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.r_wrist));

        @SuppressWarnings("unchecked")
        List<TimedAnimationUnit> lmps = field("lmpQueue").ofType(List.class).in(tau).get();
        assertEquals(1, lmps.size());
    }

    @Test
    public void setupTMUHandLocation2() throws TMUSetupException
    {
        //@formatter:off
        String murmlString =
        "  <murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
        "  <dynamic slot=\"HandLocation\" scope=\"right_arm\">"+
        "    <dynamicElement type=\"curve\">"+
        "      <value type=\"start\"  name=\"LocShoulder LocCenterLeft LocFar\"/>"+
        "      <value type=\"end\"    name=\"LocShoulder LocCenterLeft LocNorm\"/>"+
        "      <value type=\"normal\" name=\"DirU\"/>"+
        "      <value type=\"shape\"  name=\"LeftC\"/>"+
        "      <value type=\"extension\" name=\"0.6\"/>"+
        "    </dynamicElement>"+
        "  </dynamic>"+
        "  </murml-description>";
        //@formatter:on

        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "gesture1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.r_elbow, Hanim.r_shoulder));

        @SuppressWarnings("unchecked")
        List<TimedAnimationUnit> lmps = field("lmpQueue").ofType(List.class).in(tau).get();
        assertEquals(1, lmps.size());
    }

    @Test
    public void setupStaticHandshape() throws TMUSetupException
    {
        //@formatter:off
        String murmlString =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
        "  <static scope=\"left_arm\" slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
        "</murml-description>";
        //@formatter:on
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "gesture1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        @SuppressWarnings("unchecked")
        List<TimedAnimationUnit> lmps = field("lmpQueue").ofType(List.class).in(tau).get();

        assertEquals(1, lmps.size());

        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.LEFTHAND_JOINTS));
    }

    @Test
    public void setupParallelHandshapeAndPalmOrientation() throws TMUSetupException
    {
      //@formatter:off
        String murmlString = 
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" scope=\"hand\">"+
        "<parallel>"+
          "<static scope=\"left_arm\" slot=\"HandShape\" value=\"ASL5\"/>"+
          "<static scope=\"left_arm\" slot=\"PalmOrientation\" value=\"DirR\"/>"+
        "</parallel>"+
        "</murml-description>";
        //@formatter:on
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "gesture1", pb, mockAnimationPlayer);        
    }
}
