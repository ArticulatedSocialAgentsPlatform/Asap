/*******************************************************************************
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.animationengine.gesturebinding;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animation.VJoint;
import hmi.physics.PhysicalHumanoid;
import hmi.testutil.animation.HanimBody;
import hmi.util.Resources;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import saiba.bml.core.Behaviour;
import saiba.bml.core.GazeBehaviour;
import saiba.bml.core.GestureBehaviour;
import saiba.bml.core.HeadBehaviour;
import saiba.bml.core.PointingBehaviour;
import saiba.bml.core.PostureBehaviour;
import saiba.bml.core.PostureShiftBehaviour;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.restpose.SkeletonPoseRestPose;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.bml.ext.bmlt.BMLTKeyframeBehaviour;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.ParameterException;

/**
 * Test cases for GestureBinding.
 * 
 * @author Herwin van Welbergen
 */
public class GestureBindingTest
{
    private AnimationPlayer mockAniPlayer = mock(AnimationPlayer.class);
    private FeedbackManager mockFeedbackManager = mock(FeedbackManager.class);
    private PhysicalHumanoid mockPHuman = mock(PhysicalHumanoid.class);
    private PegBoard pegBoard = new PegBoard();

    private VJoint human;
    private GestureBinding gestureBinding;
    private static final double PARAMETER_PRECISION = 0.0001;

    @Before
    public void setup()
    {
        human = HanimBody.getLOA1HanimBody();
        Resources r = new Resources("");
        BMLTInfo.init();
        gestureBinding = new GestureBinding(r, mockFeedbackManager);
        String s = "<gesturebinding>" 
                + "<MotionUnitSpec type=\"head\">" + "<constraints>" 
                + "<constraint name=\"lexeme\" value=\"NOD\"/>"
                + "</constraints>" 
                + "<parametermap>" + "<parameter src=\"amount\" dst=\"a\"/>"
                + "<parameter src=\"repetition\" dst=\"r\"/>" 
                + "</parametermap>"
                + "<MotionUnit type=\"ProcAnimation\" file=\"Humanoids/shared/procanimation/smartbody/nod.xml\"/>" + "</MotionUnitSpec>"

                + "<MotionUnitSpec type=\"head\">" 
                + "<constraints>" 
                + "<constraint name=\"lexeme\" value=\"SHAKE\"/>" 
                + "</constraints>"
                + "<parametermap>" 
                + "<parameter src=\"amount\" dst=\"a\"/>" 
                + "<parameter src=\"repetition\" dst=\"r\"/>"
                + "</parametermap>" 
                + "<MotionUnit type=\"ProcAnimation\" file=\"Humanoids/shared/procanimation/smartbody/shake.xml\"/>"
                + "</MotionUnitSpec>" 
                
                + "<MotionUnitSpec type=\"keyframe\" namespace=\"http://hmi.ewi.utwente.nl/bmlt\">" 
                + "<constraints>"
                + "<constraint name=\"name\" value=\"vlakte\"/>" 
                + "</constraints>"
                + "<MotionUnit type=\"Keyframe\" file=\"Humanoids/shared/keyframe/clench_fists.xml\"/>" 
                + "</MotionUnitSpec>"
                
                + "<MotionUnitSpec type=\"gesture\">" 
                + "<constraints>" 
                + "<constraint name=\"lexeme\" value=\"BEAT\"/>"
                + "<constraint name=\"mode\" value=\"RIGHT_HAND\"/>" 
                + "</constraints>" 
                + "<parametermap/>" 
                + "<parameterdefaults>"
                + "<parameterdefault name=\"file\" value=\"Humanoids/shared/procanimation/greta/beat1right_norest.xml\"/>"
                + "<parameterdefault name=\"postStrokeHoldDuration\" value=\"0.2\"/>" 
                + "</parameterdefaults>"
                + "<MotionUnit type=\"Gesture\" class=\"asap.animationengine.procanimation.ProcAnimationGestureMU\"/>"
                + "</MotionUnitSpec>" 
                
                + "<MotionUnitSpec type=\"pointing\">" 
                + "<parametermap>"
                + "<parameter src=\"target\" dst=\"target\"/>" 
                + "<parameter src=\"mode\" dst=\"hand\"/>" 
                + "</parametermap>"
                + "<MotionUnit type=\"class\" class=\"asap.animationengine.pointing.PointingMU\"/>" 
                + "</MotionUnitSpec>"
                
                + "<MotionUnitSpec type=\"posture\">" 
                + "<constraints>" 
                + "<constraint name=\"stance\" value=\"STANDING\"/>"
                + "<constraint name=\"LEGS\" value=\"LEGS_OPEN\"/>" 
                + "</constraints>" 
                + "<parametermap>"
                + "<parameter src=\"priority\" dst=\"priority\"/>" 
                + "</parametermap>" 
                + "<parameterdefaults>"
                + "<parameterdefault name=\"pelvisheight\" value=\"1.7\"/>"
                + "<parameterdefault name=\"replacementgroup\" value=\"posture\"/>" 
                + "</parameterdefaults>"
                + "<MotionUnit type=\"PhysicalController\" class=\"hmi.physics.controller.BalanceController\"/>" 
                + "</MotionUnitSpec>"
                
                + "<RestPoseSpec>" 
                + "<constraints>" 
                + "<constraint name=\"stance\" value=\"SITTING\"/>"
                + "<constraint name=\"LEGS\" value=\"LEGS_OPEN\"/>" 
                + "</constraints>"
                + "<RestPose type=\"SkeletonPose\" file=\"Humanoids/armandia/restposes/sitting.xml\"/>" 
                + "</RestPoseSpec>"
                
                + "<MotionUnitSpec type=\"gaze\">"
                + "<constraints>"
                + "<constraint name=\"influence\" value=\"NECK\"/>"
                + "<constraint namespace=\"http://hmi.ewi.utwente.nl/bmlt\" name=\"dynamic\" value=\"true\"/>"                            
                + "</constraints>"
                + "<parametermap>"
                + "<parameter src=\"target\" dst=\"target\"/>"          
                + "<parameter src=\"offsetAngle\" dst=\"offsetangle\"/>"
                + "<parameter src=\"offsetDirection\" dst=\"offsetdirection\"/>"
                + "</parametermap>"
                + "<MotionUnit type=\"class\" class=\"asap.animationengine.gaze.DynamicGazeMU\"/>"
                + "</MotionUnitSpec>"
                + "<MotionUnitSpec type=\"gaze\">"
                + "<constraints>"
                +    "<constraint name=\"influence\" value=\"WAIST\"/>"                
                + "</constraints>"
                + "<parametermap>"
                +    "<parameter src=\"target\" dst=\"target\"/>"          
                +    "<parameter src=\"offsetAngle\" dst=\"offsetangle\"/>"
                +    "<parameter src=\"offsetDirection\" dst=\"offsetdirection\"/>"
                +    "<parameter src=\"influence\" dst=\"influence\"/>"
                + "</parametermap>"
                + "<MotionUnit type=\"class\" class=\"asap.animationengine.gaze.DynamicTorsoGazeMU\"/>"
                + "</MotionUnitSpec>"
                + "</gesturebinding>";
        gestureBinding.readXML(s);
        when(mockAniPlayer.getVNext()).thenReturn(human);
        when(mockAniPlayer.getVCurr()).thenReturn(human);
        when(mockAniPlayer.getPHuman()).thenReturn(mockPHuman);
    }
    
    public GazeBehaviour createGazeBehaviour(String bmlId, String bml)throws IOException
    {
        return new GazeBehaviour(bmlId, new XMLTokenizer(bml));
    }
    
    public PostureShiftBehaviour createPostureShiftBehaviour(String bmlId, String bml) throws IOException
    {
        return new PostureShiftBehaviour(bmlId, new XMLTokenizer(bml));
    }

    public PostureBehaviour createPostureBehaviour(String bmlId, String bml) throws IOException
    {
        return new PostureBehaviour(bmlId, new XMLTokenizer(bml));
    }

    public PointingBehaviour createPointingBehaviour(String bmlId, String bml) throws IOException
    {
        return new PointingBehaviour(bmlId, new XMLTokenizer(bml));
    }

    public GestureBehaviour createGestureBehaviour(String bmlId, String bml) throws IOException
    {
        return new GestureBehaviour(bmlId, new XMLTokenizer(bml));
    }

    public HeadBehaviour createHeadBehaviour(String bmlId, String bml) throws IOException
    {
        return new HeadBehaviour(bmlId, new XMLTokenizer(bml));
    }

    public BMLTKeyframeBehaviour createKeyFrameBehaviour(String bmlId, String bml) throws IOException
    {
        return new BMLTKeyframeBehaviour(bmlId, new XMLTokenizer(bml));
    }
    
    @Test
    public void testGetDynamicTorsoGaze() throws IOException, ParameterException
    {
        GazeBehaviour b = createGazeBehaviour("bml1", "<gaze xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"" +
                "id=\"gaze1\" influence=\"WAIST\" target=\"greensphere\"/>");
        List<TimedAnimationMotionUnit> m = gestureBinding.getMotionUnit(BMLBlockPeg.GLOBALPEG, b, mockAniPlayer, pegBoard);
        assertEquals(1, m.size());
    }
    
    @Test
    public void testGetDynamicGaze() throws IOException, ParameterException
    {
        GazeBehaviour b = createGazeBehaviour("bml1", "<gaze xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"" +
                "xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\" bmlt:dynamic=\"true\" id=\"gaze1\" influence=\"NECK\" target=\"greensphere\"/>");
        List<TimedAnimationMotionUnit> m = gestureBinding.getMotionUnit(BMLBlockPeg.GLOBALPEG, b, mockAniPlayer, pegBoard);
        assertEquals(1, m.size());
    }
    
    
    @Test
    public void testGetHeadNodWithRepeats2() throws IOException, ParameterException
    {
        HeadBehaviour b = createHeadBehaviour("bml1", "<head xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" id=\"head1\" "
                + "lexeme=\"NOD\" repetition=\"2\"/>");

        List<TimedAnimationMotionUnit> m = gestureBinding.getMotionUnit(BMLBlockPeg.GLOBALPEG, b, mockAniPlayer, pegBoard);
        assertEquals(1, m.size());
        assertTrue(Float.parseFloat(m.get(0).getMotionUnit().getParameterValue("r")) == 2.0);
        assertEquals(0.5, Float.parseFloat(m.get(0).getMotionUnit().getParameterValue("a")), PARAMETER_PRECISION);// BML default
        assertEquals(m.get(0).getBMLId(), "bml1");
        assertEquals(m.get(0).getId(), "head1");
    }

    @Test
    public void testGetHeadNodWithAmount2() throws IOException, ParameterException
    {
        HeadBehaviour b = createHeadBehaviour("bml1", "<head xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"head1\" lexeme=\"SHAKE\" amount=\"2\"/>");

        List<TimedAnimationMotionUnit> m = gestureBinding.getMotionUnit(BMLBlockPeg.GLOBALPEG, b, mockAniPlayer, pegBoard);
        assertEquals(1, m.size());
        assertEquals(1, Float.parseFloat(m.get(0).getMotionUnit().getParameterValue("r")), PARAMETER_PRECISION);
        assertEquals(2, Float.parseFloat(m.get(0).getMotionUnit().getParameterValue("a")), PARAMETER_PRECISION);
        assertEquals(m.get(0).getBMLId(), "bml1");
        assertEquals(m.get(0).getId(), "head1");
    }

    @Test
    public void testReadKeyframe() throws IOException
    {
        String kfString = "<keyframe xmlns=\"http://hmi.ewi.utwente.nl/bmlt\" id=\"v1\" name=\"vlakte\">"
                + "     <parameter name=\"joints\" value=\"r_shoulder r_elbow r_wrist\"/>" + "</keyframe>";
        Behaviour b = createKeyFrameBehaviour("bml1", kfString);

        List<TimedAnimationMotionUnit> m = gestureBinding.getMotionUnit(BMLBlockPeg.GLOBALPEG, b, mockAniPlayer, pegBoard);
        assertEquals(1, m.size());
        assertEquals("bml1", m.get(0).getBMLId());
        assertEquals("v1", m.get(0).getId());
    }

    @Test
    public void testReadGesture() throws IOException
    {
        GestureBehaviour beh = createGestureBehaviour("bml1", "<gesture xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"g1\" mode=\"RIGHT_HAND\" lexeme=\"BEAT\"/>");
        List<TimedAnimationMotionUnit> m = gestureBinding.getMotionUnit(BMLBlockPeg.GLOBALPEG, beh, mockAniPlayer, pegBoard);
        assertEquals(1, m.size());
        assertEquals("bml1", m.get(0).getBMLId());
        assertEquals("g1", m.get(0).getId());
    }

    @Test
    public void testReadPointing() throws IOException
    {
        PointingBehaviour beh = createPointingBehaviour("bml1", "<pointing xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"point1\" mode=\"RIGHT_HAND\" target=\"bluebox\"/>");
        List<TimedAnimationMotionUnit> m = gestureBinding.getMotionUnit(BMLBlockPeg.GLOBALPEG, beh, mockAniPlayer, pegBoard);
        assertEquals(1, m.size());
        assertEquals("bml1", m.get(0).getBMLId());
        assertEquals("point1", m.get(0).getId());
    }

    @Test
    public void testReadPosture() throws IOException
    {
        String str = "<posture xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"posture1\"><stance type=\"STANDING\"/><pose part=\"LEGS\" lexeme=\"LEGS_OPEN\"/></posture>";
        PostureBehaviour beh = createPostureBehaviour("bml1", str);
        List<TimedAnimationMotionUnit> m = gestureBinding.getMotionUnit(BMLBlockPeg.GLOBALPEG, beh, mockAniPlayer, pegBoard);
        assertEquals(1, m.size());
        assertEquals("bml1", m.get(0).getBMLId());
        assertEquals("posture1", m.get(0).getId());
    }

    @Test
    public void testReadPostureShift() throws IOException
    {
        String str = "<postureShift xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" "
                + "id=\"posture1\"><stance type=\"SITTING\"/><pose part=\"LEGS\" lexeme=\"LEGS_OPEN\"/></postureShift>";
        PostureShiftBehaviour beh = createPostureShiftBehaviour("bml1", str);
        RestPose rp = gestureBinding.getRestPose(beh, mockAniPlayer);
        assertThat(rp, instanceOf(SkeletonPoseRestPose.class));
    }
}
