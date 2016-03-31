/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.fest.reflect.core.Reflection.field;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import java.util.Set;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.AnimationPlayerMock;
import asap.animationengine.ace.GuidingSequence;
import asap.animationengine.ace.lmp.LMPHandMove;
import asap.animationengine.ace.lmp.LMPParallel;
import asap.animationengine.ace.lmp.LMPPoRot;
import asap.animationengine.ace.lmp.LMPSequence;
import asap.animationengine.ace.lmp.LMPWristPos;
import asap.animationengine.ace.lmp.LMPWristRot;
import asap.animationengine.ace.lmp.MotorControlProgram;
import asap.animationengine.keyframe.MURMLKeyframeMU;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TMUSetupException;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.hns.Hns;
import asap.hns.ShapeSymbols;
import asap.motionunit.MUPlayException;
import asap.motionunit.TimedMotionUnit;
import asap.murml.MURMLDescription;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.feedback.FeedbackManagerImpl;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.scheduler.BMLBlockManager;

import com.google.common.collect.ObjectArrays;

/**
 * Testcases for the MURML MotionUnit builder
 * @author Herwin
 */
public class MURMLMUBuilderTest
{

    private AnimationUnit mockMu = mock(AnimationUnit.class);
    private VJoint vNext = HanimBody.getLOA2HanimBody();
    private VJoint vCurr = HanimBody.getLOA2HanimBody();
    private BMLBlockManager bbManager = new BMLBlockManager();
    private FeedbackManager fbManager = new FeedbackManagerImpl(bbManager, "characterx");
    private AnimationPlayer mockAnimationPlayer = AnimationPlayerMock.createAnimationPlayerMock(vCurr, vNext);

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

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_INFERRED", justification = "Return value ignored, should throw exception anyways.")
    @Test(expected = MUSetupException.class)
    public void testKeyframeGestureMissingJoint() throws MUSetupException
    {
        // @formatter:off
        String murmlString =  "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
                                "<dynamic>"+
                                    "<keyframing mode=\"spline\" easescale=\"1.0\" easeturningpoint=\"0.5\">"+
                                    "<phase>"+
                                        "<frame ftime=\"0.50\"><posture>Humanoid (missingjoint 3 0.0 0.0 -5.0)</posture></frame>"+
                                    "</phase>"+
                                    "</keyframing>"+
                                "</dynamic>"+
                                "</murml-description>";
        // @formatter:on
        AnimationUnit au = murmlMuBuilder.setup(murmlString);
        assertThat(au, instanceOf(MURMLKeyframeMU.class));
        MURMLKeyframeMU kfmu = (MURMLKeyframeMU) au;

        kfmu.copy(mockAnimationPlayer);
    }

    @Test(expected = TMUSetupException.class)
    public void testKeyframeTMUMissingJoint() throws TMUSetupException
    {
        // @formatter:off
        String murmlString =  "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"+
                                "<dynamic>"+
                                    "<keyframing mode=\"spline\" easescale=\"1.0\" easeturningpoint=\"0.5\">"+
                                    "<phase>"+
                                        "<frame ftime=\"0.50\"><posture>Humanoid (missingjoint 3 0.0 0.0 -5.0)</posture></frame>"+
                                    "</phase>"+
                                    "</keyframing>"+
                                "</dynamic>"+
                                "</murml-description>";
        // @formatter:on
        MURMLDescription desc = new MURMLDescription();
        desc.readXML(murmlString);
        murmlMuBuilder.setupTMU(desc, fbManager, BMLBlockPeg.GLOBALPEG, "bml1", "beh1", pb, mockAnimationPlayer);        
    }

    @Test
    public void testPriorityKeyframing() throws MUPlayException, MUSetupException, TMUSetupException
    {
        String murmlString = "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">"
                + "<dynamic><keyframing><phase><frame ftime=\"0\"><posture>Humanoid "
                + "(l_shoulder 3 100 0 0)</posture></frame></phase></keyframing></dynamic></murml-description>";
        MURMLDescription def = new MURMLDescription();
        def.setPriority(34);
        def.readXML(murmlString);

        TimedAnimationUnit tmu = murmlMuBuilder.setupTMU(def, new FeedbackManagerImpl(new BMLBlockManager(), ""), BMLBlockPeg.GLOBALPEG,
                "bml1", "g1", pb, mockAnimationPlayer);
        assertEquals(34, tmu.getPriority());
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

    @Test(expected = TMUSetupException.class)
    public void setupInvalidTMUStaticHandLocation() throws TMUSetupException
    {
        when(mockHns.getHandLocation(anyString(), any(float[].class))).thenReturn(false);

        //@formatter:off
        String murmlString =
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                "<parallel>"+
                  "<static slot=\"HandShape\" value=\"ASL5\"/>"+
                  "<static slot=\"HandLocation\" value=\"LocShoulder LocCenterRight LocNorm\" scope=\"left_arm\"/>"+
                "</parallel>"+
                "</murml-description>";
        // @formatter:on
        murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""), BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb,
                mockAnimationPlayer);
    }

    @Test
    public void setupTMUStaticHandLocation() throws TMUSetupException
    {
        //@formatter:off
        String murmlString =
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                "<static slot=\"HandLocation\" value=\"invalid\" scope=\"left_arm\"/>"+
                "</murml-description>";
        // @formatter:on

        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.l_shoulder, Hanim.l_elbow));
    }

    @Test(expected = TMUSetupException.class)
    public void setupInvalidTMUDynamicHandLocation() throws TMUSetupException
    {
        when(mockHns.getHandLocation(anyString(), any(float[].class))).thenReturn(false);
        //@formatter:off
        String murmlString =
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<parallel>"+
                        "<static slot=\"HandShape\" value=\"ASL5\"/>"+
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
                        "</parallel>"+
                "</murml-description>";
        // @formatter:on
        murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""), BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb,
                mockAnimationPlayer);
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

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPWristPos.class));
        LMPWristPos pos = (LMPWristPos) lmp;
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

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPWristPos.class));
        LMPWristPos pos = (LMPWristPos) lmp;
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

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPWristRot.class));
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

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPPoRot.class));
    }

    @Test
    public void setupTMURelativeDynamicPalmOrientation2Elements() throws TMUSetupException
    {
        //@formatter:off
        String murmlString = 
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                  "<dynamic slot=\"PalmOrientation\" scope=\"right_arm\">"+
                        "<dynamicElement>"+
                              "<value type=\"start\" name=\"PalmU\"/>"+
                              "<value type=\"end\" name=\"PalmD\"/>"+
                        "</dynamicElement>"+
                        "<dynamicElement>"+
                            "<value type=\"start\" name=\"PalmU\"/>"+
                            "<value type=\"end\" name=\"PalmD\"/>"+
                        "</dynamicElement>"+
                  "</dynamic>"+
                "</murml-description>";
        // @formatter:on
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.r_wrist));

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertNotNull(lmp.getTimePeg("strokeStart"));
        assertNotNull(lmp.getTimePeg("stroke1"));
        assertNotNull(lmp.getTimePeg("stroke2"));
        assertNotNull(lmp.getTimePeg("strokeEnd"));
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
        assertEquals(100, tau.getPriority());

        assertThat(tau, instanceOf(MotorControlProgram.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.r_wrist));

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPWristRot.class));
    }

    @Test(expected = TMUSetupException.class)
    public void setupInvalidTMUDynamicPalmOrientation() throws TMUSetupException
    {
        when(mockHns.getAbsoluteDirection(startsWith("Palm"), any(float[].class))).thenReturn(false);
        when(mockHns.getAbsoluteDirection(startsWith("Dir"), any(float[].class))).thenReturn(false);
        when(mockHns.isPalmOrientation(startsWith("Palm"))).thenReturn(false);
        //@formatter:off
        String murmlString = 
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<parallel>"+
                        "<dynamic slot=\"PalmOrientation\" scope=\"right_arm\">"+
                        "<dynamicElement>"+
                              "<value type=\"start\" name=\"unknown\"/>"+
                              "<value type=\"end\" name=\"unknown\"/>"+
                        "</dynamicElement>"+                        
                        "</dynamic>"+
                        "<dynamic slot=\"PalmOrientation\" scope=\"left_arm\">"+
                        "<dynamicElement>"+
                              "<value type=\"start\" name=\"unknown\"/>"+
                              "<value type=\"end\" name=\"unknown\"/>"+
                        "</dynamicElement>"+                        
                        "</dynamic>"+
                        "</parallel>"+
                "</murml-description>";
        // @formatter:on
        murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""), BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb,
                mockAnimationPlayer);
    }

    @Test(expected = TMUSetupException.class)
    public void setupInvalidTMUDynamicExtFingerOrientation() throws TMUSetupException
    {
        when(mockHns.getAbsoluteDirection(startsWith("Dir"), any(float[].class))).thenReturn(false);
        //@formatter:off
        String murmlString = 
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<parallel>"+
                        "<dynamic slot=\"ExtFingerOrientation\" scope=\"right_arm\">"+
                        "<dynamicElement>"+
                              "<value type=\"start\" name=\"unknown\"/>"+
                              "<value type=\"end\" name=\"unknown\"/>"+
                        "</dynamicElement>"+                        
                        "</dynamic>"+
                        "<dynamic slot=\"ExtFingerOrientation\" scope=\"left_arm\">"+
                        "<dynamicElement>"+
                              "<value type=\"start\" name=\"unknown\"/>"+
                              "<value type=\"end\" name=\"unknown\"/>"+
                        "</dynamicElement>"+                        
                        "</dynamic>"+
                        "</parallel>"+
                "</murml-description>";
        // @formatter:on
        murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""), BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb,
                mockAnimationPlayer);
    }

    @Test(expected = TMUSetupException.class)
    public void setupInvalidTMUStaticPalmOrientation() throws TMUSetupException
    {
        when(mockHns.getAbsoluteDirection(startsWith("Palm"), any(float[].class))).thenReturn(false);
        when(mockHns.getAbsoluteDirection(startsWith("Dir"), any(float[].class))).thenReturn(false);
        when(mockHns.isPalmOrientation(startsWith("Palm"))).thenReturn(false);
        //@formatter:off
        String murmlString = 
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<parallel>"+
                        "<static slot=\"PalmOrientation\" scope=\"right_arm\" value=\"invalid\"/>"+
                        "<static slot=\"PalmOrientation\" scope=\"left_arm\" value=\"invalid\"/>"+
                        "</parallel>"+
                "</murml-description>";
        // @formatter:on
        murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""), BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb,
                mockAnimationPlayer);
    }

    @Test(expected = TMUSetupException.class)
    public void setupInvalidTMUStaticExtFingerOrientation() throws TMUSetupException
    {
        when(mockHns.getAbsoluteDirection(startsWith("Palm"), any(float[].class))).thenReturn(false);
        when(mockHns.getAbsoluteDirection(startsWith("Dir"), any(float[].class))).thenReturn(false);
        when(mockHns.isPalmOrientation(startsWith("Palm"))).thenReturn(false);
        //@formatter:off
        String murmlString = 
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<parallel>"+
                        "<static slot=\"ExtFingerOrientation\" scope=\"right_arm\" value=\"invalid\"/>"+
                        "<static slot=\"ExtFingerOrientation\" scope=\"left_arm\" value=\"invalid\"/>"+
                        "</parallel>"+
                "</murml-description>";
        // @formatter:on
        murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""), BMLBlockPeg.GLOBALPEG, "bml1", "g1", pb,
                mockAnimationPlayer);
    }

    @Test
    public void testPriorityOther() throws MUPlayException, MUSetupException, TMUSetupException
    {
        //@formatter:off
        String murmlString = 
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<static slot=\"PalmOrientation\" scope=\"right_arm\" value=\"DirU\"/>"+
                "</murml-description>";
        // @formatter:on
        MURMLDescription def = new MURMLDescription();
        def.readXML(murmlString);
        def.setPriority(34);
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(def, new FeedbackManagerImpl(new BMLBlockManager(), ""), BMLBlockPeg.GLOBALPEG,
                "bml1", "g1", pb, mockAnimationPlayer);
        assertEquals(34, tau.getPriority());
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

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPPoRot.class));
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

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPParallel.class));
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

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPWristPos.class));
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

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPHandMove.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.LEFTHAND_JOINTS));
    }

    @Test(expected = TMUSetupException.class)
    public void testUnknownStaticHandShape() throws TMUSetupException
    {
        when(mockHnsHandshapes.getHNSHandShape(anyString())).thenReturn(null);
        //@formatter:off
        String murmlString =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
        "  <static scope=\"left_arm\" slot=\"HandShape\" value=\"BSfist (ThExt)\"/>"+
        "</murml-description>";
        //@formatter:on
        murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""), BMLBlockPeg.GLOBALPEG, "bml1", "gesture1",
                pb, mockAnimationPlayer);
    }

    @Test(expected = TMUSetupException.class)
    public void testUnknownDynamicHandShape() throws TMUSetupException
    {
        when(mockHnsHandshapes.getHNSHandShape(anyString())).thenReturn(null);
        //@formatter:off
        String murmlString =
                "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
                        "<dynamic slot=\"HandShape\" scope=\"right_arm\">"+
                        "<dynamicElement>"+
                              "<value type=\"start\" name=\"BSfist (ThExt)\"/>"+
                              "<value type=\"end\" name=\"ASL5\"/>"+
                        "</dynamicElement>"+
                        "<dynamicElement>"+
                            "<value type=\"start\" name=\"BSfist (ThExt)\"/>"+
                            "<value type=\"end\" name=\"BSfist (ThExt)\"/>"+
                        "</dynamicElement>"+
                        "</dynamic>"+
                        "</murml-description>";
        //@formatter:on
        murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""), BMLBlockPeg.GLOBALPEG, "bml1", "gesture1",
                pb, mockAnimationPlayer);
    }

    @Test
    public void setupDynamicHandshape() throws TMUSetupException
    {
        //@formatter:off
        String murmlString =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
        "<dynamic slot=\"HandShape\" scope=\"right_arm\">"+
        "<dynamicElement>"+
              "<value type=\"start\" name=\"BSfist (ThExt)\"/>"+
              "<value type=\"end\" name=\"ASL5\"/>"+
        "</dynamicElement>"+
        "<dynamicElement>"+
            "<value type=\"start\" name=\"BSfist (ThExt)\"/>"+
            "<value type=\"end\" name=\"BSfist (ThExt)\"/>"+
        "</dynamicElement>"+
        "</dynamic>"+
        "</murml-description>";
        //@formatter:on
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "gesture1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPHandMove.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.RIGHTHAND_JOINTS));
        assertNotNull(tau.getTime("strokeStart"));
        assertNotNull(tau.getTime("stroke1"));
        assertNotNull(tau.getTime("stroke2"));
        assertNotNull(tau.getTime("strokeEnd"));
    }

    @Test
    public void setupDynamicHandshape2() throws TMUSetupException
    {
        //@formatter:off
        String murmlString =
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\">" +
        "<dynamic slot=\"HandShape\" scope=\"right_arm\">"+
        "<dynamicElement>"+
              "<value id=\"strokeStart\" name=\"BSfist (ThExt)\"/>"+
              "<value id=\"stroke1\" name=\"ASL5\"/>"+        
            "<value type=\"stroke2\" name=\"BSfist (ThExt)\"/>"+
            "<value type=\"strokeEnd\" name=\"BSfist (ThExt)\"/>"+
        "</dynamicElement>"+
        "</dynamic>"+
        "</murml-description>";
        //@formatter:on
        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "gesture1", pb, mockAnimationPlayer);

        assertThat(tau, instanceOf(MotorControlProgram.class));

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPHandMove.class));
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.RIGHTHAND_JOINTS));
        assertNotNull(tau.getTime("strokeStart"));
        assertNotNull(tau.getTime("stroke1"));
        assertNotNull(tau.getTime("stroke2"));
        assertNotNull(tau.getTime("strokeEnd"));
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

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPParallel.class));
        assertThat(tau.getKinematicJoints(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(ObjectArrays.concat(Hanim.l_wrist, Hanim.LEFTHAND_JOINTS)));
    }

    @Test
    public void setupSequentialHandshapeAndPalmOrientation() throws TMUSetupException
    {
        //@formatter:off
        String murmlString = 
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" scope=\"hand\">"+
        "<sequence>"+
          "<static scope=\"left_arm\" slot=\"HandShape\" value=\"ASL5\"/>"+
          "<static scope=\"left_arm\" slot=\"PalmOrientation\" value=\"DirR\"/>"+
        "</sequence>"+
        "</murml-description>";
        //@formatter:on

        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "gesture1", pb, mockAnimationPlayer);
        assertThat(tau.getKinematicJoints(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(ObjectArrays.concat(Hanim.l_wrist, Hanim.LEFTHAND_JOINTS)));

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPSequence.class));
    }

    @Test
    public void testParallelInSequence() throws TMUSetupException
    {
        //@formatter:off
        String murmlString = 
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" scope=\"hand\">"+
        "<sequence>"+
          "<static scope=\"left_arm\" slot=\"HandShape\" value=\"ASL5\"/>"+
          "<parallel>"+
              "<static scope=\"left_arm\" slot=\"PalmOrientation\" value=\"DirR\"/>"+
              "  <dynamic slot=\"HandLocation\" scope=\"right_arm\">"+
              "    <dynamicElement type=\"curve\">"+
              "      <value type=\"start\"  name=\"LocShoulder LocCenterLeft LocFar\"/>"+
              "      <value type=\"end\"    name=\"LocShoulder LocCenterLeft LocNorm\"/>"+
              "      <value type=\"normal\" name=\"DirU\"/>"+
              "      <value type=\"shape\"  name=\"LeftC\"/>"+
              "      <value type=\"extension\" name=\"0.6\"/>"+
              "    </dynamicElement>"+
              "  </dynamic>"+
          "</parallel>"+
        "</sequence>"+
        "</murml-description>";
        //@formatter:on

        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "gesture1", pb, mockAnimationPlayer);
        String strs[] = ObjectArrays.concat(Hanim.l_wrist, Hanim.LEFTHAND_JOINTS);
        strs = ObjectArrays.concat(Hanim.r_elbow, strs);
        strs = ObjectArrays.concat(Hanim.r_shoulder, strs);
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(strs));

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPSequence.class));
    }

    @Test
    public void testSequenceInParallel() throws TMUSetupException
    {
        //@formatter:off
        String murmlString = 
        "<murml-description xmlns=\"http://www.techfak.uni-bielefeld.de/ags/soa/murml\" scope=\"hand\">"+
        "<parallel>"+
          "<static scope=\"left_arm\" slot=\"HandShape\" value=\"ASL5\"/>"+
          "<sequence>"+
              "<static scope=\"left_arm\" slot=\"PalmOrientation\" value=\"DirR\"/>"+
              "  <dynamic slot=\"HandLocation\" scope=\"right_arm\">"+
              "    <dynamicElement type=\"curve\">"+
              "      <value type=\"start\"  name=\"LocShoulder LocCenterLeft LocFar\"/>"+
              "      <value type=\"end\"    name=\"LocShoulder LocCenterLeft LocNorm\"/>"+
              "      <value type=\"normal\" name=\"DirU\"/>"+
              "      <value type=\"shape\"  name=\"LeftC\"/>"+
              "      <value type=\"extension\" name=\"0.6\"/>"+
              "    </dynamicElement>"+
              "  </dynamic>"+
          "</sequence>"+
        "</parallel>"+
        "</murml-description>";
        //@formatter:on

        TimedAnimationUnit tau = murmlMuBuilder.setupTMU(murmlString, new FeedbackManagerImpl(new BMLBlockManager(), ""),
                BMLBlockPeg.GLOBALPEG, "bml1", "gesture1", pb, mockAnimationPlayer);
        String strs[] = ObjectArrays.concat(Hanim.l_wrist, Hanim.LEFTHAND_JOINTS);
        strs = ObjectArrays.concat(Hanim.r_elbow, strs);
        strs = ObjectArrays.concat(Hanim.r_shoulder, strs);
        assertThat(tau.getKinematicJoints(), IsIterableContainingInAnyOrder.containsInAnyOrder(strs));

        TimedAnimationUnit lmp = field("lmp").ofType(TimedAnimationUnit.class).in(tau).get();
        assertThat(lmp, instanceOf(LMPParallel.class));
    }
}
