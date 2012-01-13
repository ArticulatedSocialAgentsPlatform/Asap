package asap.animationengine.controller;

import static org.junit.Assert.*;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.controller.ControllerParameterNotFoundException;
import hmi.physics.controller.PhysicalController;

import org.junit.Before;
import org.junit.Test;

import asap.animationengine.controller.CompoundController;
import static org.hamcrest.collection.IsArrayContaining.*;
import static org.mockito.Mockito.*;
/**
 * Unit testcase for CompoundController
 * @author Herwin
 *
 */
/**
 * Unit testcase for CompoundController
 * @author Herwin
 *
 */
public class CompoundControllerTest
{
    private CompoundController compoundController;
    
    private PhysicalHumanoid mockPhysicalHumanoid = mock(PhysicalHumanoid.class);
    
    @Before
    public void setup()
    {
        compoundController = new CompoundController();
        String str = "<CompoundController xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\">"+
                     "<required>"+
                         "<Controller class=\"hmi.physics.controller.HingeJointController\" id=\"elbow\">"+
                         "<bmlt:parameter name=\"joint\" value=\"l_elbow\"/>"+
                         "<bmlt:parameter name=\"ds\" value=\"1\"/>"+
                         "<bmlt:parameter name=\"ks\" value=\"200\"/>"+
                         "<bmlt:parameter name=\"angle\" value=\"3\"/>"+
                         "</Controller>"+
                     "</required>"+
                     "<desired>"+
                         "<Controller class=\"hmi.physics.controller.HingeJointController\" id=\"wrist\">"+
                         "<bmlt:parameter name=\"joint\" value=\"l_wrist\"/>"+
                         "<bmlt:parameter name=\"ds\" value=\"2\"/>"+
                         "<bmlt:parameter name=\"ks\" value=\"100\"/>"+
                         "<bmlt:parameter name=\"angle\" value=\"1\"/>"+
                         "</Controller>"+
                         "<Controller class=\"hmi.physics.controller.HingeJointController\" id=\"thumb\">"+
                         "<bmlt:parameter name=\"joint\" value=\"l_thumb1\"/>"+
                         "</Controller>"+
                         "<Controller class=\"hmi.physics.controller.HingeJointController\" id=\"elbow\">"+
                         "<bmlt:parameter name=\"joint\" value=\"l_elbow\"/>"+
                         "</Controller>"+
                     "</desired>"+
                     "</CompoundController>";
        compoundController.readXML(str);
    }
    
    @Test
    public void testGetParameterValue() throws ControllerParameterNotFoundException
    {
        assertEquals("l_elbow",compoundController.getParameterValue("elbow:joint"));
        assertEquals(1,Float.parseFloat(compoundController.getParameterValue("elbow:ds")),0.001);
        assertEquals(200,Float.parseFloat(compoundController.getParameterValue("elbow:ks")),0.001);
        assertEquals(3,Float.parseFloat(compoundController.getParameterValue("elbow:angle")),0.001);
        
        assertEquals("l_wrist",compoundController.getParameterValue("wrist:joint"));
        assertEquals(2,Float.parseFloat(compoundController.getParameterValue("wrist:ds")),0.001);
        assertEquals(100,Float.parseFloat(compoundController.getParameterValue("wrist:ks")),0.001);
        assertEquals(1,Float.parseFloat(compoundController.getParameterValue("wrist:angle")),0.001);
    }
    
    @Test
    public void testGetJointIDs()
    {
        assertThat(compoundController.getRequiredJointIDs(),hasItemInArray("l_elbow"));
        assertEquals(1,compoundController.getRequiredJointIDs().length);
        
        assertThat(compoundController.getDesiredJointIDs(),hasItemInArray("l_wrist"));
        assertThat(compoundController.getDesiredJointIDs(),hasItemInArray("l_thumb1"));        
        assertThat(compoundController.getDesiredJointIDs(),hasItemInArray("l_elbow"));
        assertEquals(3,compoundController.getDesiredJointIDs().length);        
    }
    
    @Test
    public void testCopy() throws ControllerParameterNotFoundException
    {
        PhysicalController cCopy = compoundController.copy(mockPhysicalHumanoid);
        
        assertEquals("l_elbow",cCopy.getParameterValue("elbow:joint"));
        assertEquals(1,Float.parseFloat(cCopy.getParameterValue("elbow:ds")),0.001);
        assertEquals(200,Float.parseFloat(cCopy.getParameterValue("elbow:ks")),0.001);
        assertEquals(3,Float.parseFloat(cCopy.getParameterValue("elbow:angle")),0.001);
        
        assertEquals("l_wrist",cCopy.getParameterValue("wrist:joint"));
        assertEquals(2,Float.parseFloat(cCopy.getParameterValue("wrist:ds")),0.001);
        assertEquals(100,Float.parseFloat(cCopy.getParameterValue("wrist:ks")),0.001);
        assertEquals(1,Float.parseFloat(cCopy.getParameterValue("wrist:angle")),0.001);
    }
}
