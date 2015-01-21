/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import hmi.util.Resources;

import org.junit.Test;

import asap.animationengine.motionunit.StubAnimationUnit;

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
        mu.readXML("<MotionUnit type=\"class\" class=\"asap.animationengine.motionunit.StubAnimationUnit\"/>");
        assertThat(mu.getMotionUnit(), instanceOf(StubAnimationUnit.class));        
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
