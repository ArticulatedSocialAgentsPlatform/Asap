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
package asap.animationengine.procanimation;

import static hmi.testutil.math.Quat4fTestUtil.assertQuat4fRotationEquivalent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.testutil.animation.HanimBody;

import org.junit.Before;
import org.junit.Test;
import org.lsmp.djep.xjep.XJep;

/**
 * Unit tests for Keyframes in ProcAnimation
 * @author Herwin van Welbergen
 *
 */
public class KeyframesTest
{
    private VJoint human;
    private float goal[]=new float[4];        
    private float q[]=new float[4];
    private float q1[]=new float[4];
    private float q2[]=new float[4];
    private VJoint rWristJoint;
    private VJoint animationRoot;
    private Keyframes kfs; 
    @Before
    public void loadDaeHuman()
    {
        human = HanimBody.getLOA1HanimBody();
        animationRoot = new VJoint();        
        animationRoot.addChild(human);
        rWristJoint = human.getPart(Hanim.r_wrist);
        kfs = new Keyframes(new XJep());
    }
    
    @Test
    public void testEvaluate1Rotation()
    {
        //1 rotation test
        String str = "<Keyframes target=\"r_wrist\" encoding=\"quaternion\">";        
        str = str + "<Keyframe time=\"0.0\" local=\"true\" value=\"0.7071068;-0.7071068;0.0;0.0\"/>";
        str = str + "</Keyframes>";
                
        kfs.readXML(str);
        kfs.setJoint(rWristJoint);
        
        assertEquals(kfs.getTarget(),"r_wrist");
        assertEquals(kfs.getEncoding(),"quaternion");
        Quat4f.set(q,0.7071068f,-0.7071068f,0.0f,0.0f);
        kfs.evaluate(0, goal, human);
        assertTrue(Quat4f.epsilonEquals(goal, q, 0.000001f));
        
        kfs.evaluate(0.8f, goal, human);
        assertTrue(Quat4f.epsilonEquals(goal, q, 0.000001f));
        
        kfs.evaluate(1, goal, human);
        assertTrue(Quat4f.epsilonEquals(goal, q, 0.000001f));        
    }
    
    @Test
    public void testEvaluate2Rotations()    
    {
        //2 rotations, 1 world rotation
        String str = "<Keyframes target=\"r_wrist\" encoding=\"quaternion\">";        
        str = str + "<Keyframe time=\"0.0\" local=\"true\" value=\"0.7071068;-0.7071068;0.0;0.0\"/>";
        str = str + "<Keyframe time=\"0.08383378\" local=\"false\" value=\"0.7071068;-0.7071068;0.0;0.0\"/>";
        str = str + "</Keyframes>";
        kfs.readXML(str);
        kfs.setJoint(rWristJoint);
        
        assertTrue(kfs.getTarget().equals("r_wrist"));
        assertTrue(kfs.getEncoding().equals("quaternion"));
        Quat4f.set(q,0.7071068f,-0.7071068f,0.0f,0.0f);
        kfs.evaluate(0, goal, human);
        assertTrue(Quat4f.epsilonEquals(goal, q, 0.000001f));
        
        kfs.evaluate(0.8f, goal, human);
        assertTrue(Quat4f.epsilonEquals(goal, q, 0.000001f));
        
        kfs.evaluate(1, goal, human);
        assertTrue(Quat4f.epsilonEquals(goal, q, 0.000001f));
    }
    
    @Test
    public void testEvaluate2WorldRotations()
    {
        String str = "<Keyframes target=\"r_wrist\" encoding=\"quaternion\">";        
        str = str + "<Keyframe time=\"0.0\" local=\"false\" value=\"0.7071068;-0.7071068;0.0;0.0\"/>";
        str = str + "<Keyframe time=\"1.0\" local=\"false\" value=\"1.0;0.0;0.0;0.0\"/>";
        str = str + "</Keyframes>";        
        kfs.readXML(str);
        kfs.setJoint(rWristJoint);
        
        assertTrue(kfs.getTarget().equals("r_wrist"));
        assertTrue(kfs.getEncoding().equals("quaternion"));
        
        Quat4f.set(q,0.7071068f,-0.7071068f,0.0f,0.0f);
        kfs.evaluate(0, goal, human);
        assertTrue(Quat4f.epsilonEquals(goal, q, 0.000001f));
        
        kfs.evaluate(1f, goal, human);
        Quat4f.set(q,1.0f,0.0f,0.0f,0.0f);
        assertTrue(Quat4f.epsilonEquals(goal, q, 0.000001f));
        
        Quat4f.set(q1,0.7071068f,-0.7071068f,0.0f,0.0f);
        Quat4f.set(q2,1,0,0,0);
        Quat4f.interpolate(q, q1,q2, 0.7f);
        kfs.evaluate(0.7, goal, human);
        assertTrue(Quat4f.epsilonEquals(goal, q, 0.000001f));
        
        animationRoot.calculateMatrices();
        kfs.evaluate(0.7, goal, human);
        rWristJoint.setRotation(goal);
        animationRoot.calculateMatrices();
        rWristJoint.getPathRotation(human, q1);
        assertTrue(Quat4f.epsilonEquals(q1, q, 0.000001f));
    }
    
    @Test
    public void testNonLocalRotation() 
    {
        String str = "<Keyframes target=\"r_wrist\" encoding=\"quaternion\">";        
        str = str + "<Keyframe time=\"0.0\" local=\"false\" value=\"0.7071068;-0.7071068;0.0;0.0\"/>";
        str = str + "<Keyframe time=\"1.0\" local=\"false\" value=\"1.0;0.0;0.0;0.0\"/>";
        str = str + "</Keyframes>";
        
        //test non-local rotation spec 1
        kfs.readXML(str);
        kfs.setJoint(rWristJoint);
        human.getPart(Hanim.r_shoulder).setRollPitchYawDegrees(10, 20, 30);
        Quat4f.set(q1,0.7071068f,-0.7071068f,0.0f,0.0f);
        Quat4f.set(q2,1,0,0,0);
        
        animationRoot.calculateMatrices();
        kfs.evaluate(0.0, goal, human);
        rWristJoint.setRotation(goal);
        animationRoot.calculateMatrices();
        rWristJoint.getPathRotation(human, q);
        assertTrue(Quat4f.epsilonEquals(q1, q, 0.000001f));        
    }
    
    @Test
    public void testNonLocalRotation2()
    {
        String str = "<Keyframes target=\"r_wrist\" encoding=\"quaternion\">";        
        str = str + "<Keyframe time=\"0.0\" local=\"false\" value=\"0.7071068;-0.7071068;0.0;0.0\"/>";
        str = str + "<Keyframe time=\"1.0\" local=\"false\" value=\"1.0;0.0;0.0;0.0\"/>";
        str = str + "</Keyframes>";
        
        //test non-local rotation spec 2
        kfs.readXML(str);
        kfs.setJoint(rWristJoint);
        human.getPart(Hanim.r_shoulder).setRollPitchYawDegrees(10, 20, 30);
        Quat4f.set(q1,0.7071068f,-0.7071068f,0.0f,0.0f);
        Quat4f.set(q2,1,0,0,0);
        animationRoot.calculateMatrices();
        
        kfs.evaluate(0.7, goal, human);
        rWristJoint.setRotation(goal);
        animationRoot.calculateMatrices();
        Quat4f.interpolate(q, q1,q2, 0.7f);
        rWristJoint.getPathRotation(human, q1);
        
        assertQuat4fRotationEquivalent(q1, q, 0.001f);                
    }
    
    @Test
    public void testWackOrdering()
    {   
        String str = "<Keyframes target=\"r_wrist\" encoding=\"quaternion\">";        
        str = str + "<Keyframe time=\"1.0\" local=\"true\" value=\"1.0;0.0;0.0;0.0\"/>";
        str = str + "<Keyframe time=\"0.0\" local=\"true\" value=\"0.7071068;-0.7071068;0.0;0.0\"/>";
        str = str + "<Keyframe time=\"0.5\" local=\"true\" value=\"-0.7071068;0.0;0.0;0.7071068\"/>";
        str = str + "</Keyframes>";        
        kfs.readXML(str);
        kfs.setJoint(rWristJoint);
        
        assertEquals(kfs.getTarget(),"r_wrist");
        assertEquals(kfs.getEncoding(),"quaternion");
        
        kfs.evaluate(0.25, goal, human);
        Quat4f.set(q1, 0.7071068f,-0.7071068f,0.0f,0.0f);
        Quat4f.set(q2, -0.7071068f,0.0f,0.0f,0.7071068f);
        Quat4f.interpolate(q, q1,q2, 0.5f);
        assertTrue(Quat4f.epsilonEquals(q, goal, 0.000001f));
        
        kfs.evaluate(0.75, goal, human);        
        Quat4f.set(q1, -0.7071068f,0.0f,0.0f,0.7071068f);
        Quat4f.set(q2, 1,0,0,0);
        Quat4f.interpolate(q, q1,q2, 0.5f);
        assertTrue(Quat4f.epsilonEquals(q, goal, 0.000001f));
    }
    
    @Test
    public void testStartFrame()
    {   
        String str = "<Keyframes target=\"r_wrist\" encoding=\"quaternion\">";        
        str = str + "<Keyframe time=\"0.0\" local=\"true\" value=\"0.7071068;-0.7071068;0.0;0.0\"/>";
        str = str + "<Keyframe time=\"0.5\" local=\"true\" value=\"-0.7071068;0.0;0.0;0.7071068\"/>";        
        str = str + "<Keyframe time=\"1.0\" local=\"true\" value=\"1.0;0.0;0.0;0.0\"/>";        
        str = str + "</Keyframes>";        
        kfs.readXML(str);
        kfs.setJoint(rWristJoint);        
        
        kfs.evaluate(0, goal, human);
        Quat4f.set(q, 0.7071068f,-0.7071068f,0.0f,0.0f);
        assertQuat4fRotationEquivalent(q, goal, 0.000001f);
    }
    
    @Test
    public void testEndFrame()
    {   
        String str = "<Keyframes target=\"r_wrist\" encoding=\"quaternion\">";        
        str = str + "<Keyframe time=\"0.0\" local=\"true\" value=\"0.7071068;-0.7071068;0.0;0.0\"/>";
        str = str + "<Keyframe time=\"0.5\" local=\"true\" value=\"-0.7071068;0.0;0.0;0.7071068\"/>";        
        str = str + "<Keyframe time=\"1.0\" local=\"true\" value=\"1.0;0.0;0.0;0.0\"/>";        
        str = str + "</Keyframes>";        
        kfs.readXML(str);
        kfs.setJoint(rWristJoint);        
        
        kfs.evaluate(1, goal, human);
        Quat4f.set(q, 1,0,0,0);
        assertQuat4fRotationEquivalent(q, goal, 0.000001f);
    }
}
