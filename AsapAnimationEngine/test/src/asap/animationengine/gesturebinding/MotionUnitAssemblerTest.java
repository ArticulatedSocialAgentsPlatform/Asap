package asap.animationengine.gesturebinding;

import hmi.util.Resources;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.Test;

import asap.animationengine.gesturebinding.MotionUnitAssembler;
import asap.animationengine.motionunit.StubMotionUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

class NotAMotionUnit {}

/**
 * Unit test cases for the MotionUnitAssembler
 * @author welberge
 */
public class MotionUnitAssemblerTest
{
    private Resources mockResources = mock(Resources.class); 
    private MotionUnitAssembler mu = new MotionUnitAssembler(mockResources);
    
    
    
    @Test
    public void testEmpty()
    {
        assertNull(mu.getMotionUnit());
    }
    
    @Test
    public void testMotionUnitFromClass()
    {
        mu.readXML("<MotionUnit type=\"class\" class=\"asap.animationengine.motionunit.StubMotionUnit\"/>");
        assertThat(mu.getMotionUnit(), instanceOf(StubMotionUnit.class));        
    }
    
    @Test
    public void testMotionUnitFromClassThatDoesNotImplementMotionUnit()
    {
        mu.readXML("<MotionUnit type=\"class\" class=\"asap.animationengine.gesturebinding.NotAMotionUnit\"/>");   
        assertNull(mu.getMotionUnit());
    }
    
    @Test
    public void testMotionUnitFromInterface()
    {
        mu.readXML("<MotionUnit type=\"class\" class=\"asap.animationengine.motionunit.MotionUnit\"/>");   
        assertNull(mu.getMotionUnit());
    }
}
