/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Test;

import saiba.bml.core.GazeShiftBehaviour;
import asap.animationengine.gaze.DynamicRestGaze;

/**
 * unit tests for the RestGazeAssembler
 * @author hvanwelbergen
 *
 */
public class RestGazeSpecTest
{
    @Test
    public void test() throws IOException
    {
        String xmlString = 
                // @formatter:off
                "<RestGazeSpec>"+
                        "<constraints>"+
                            "<constraint name=\"influence\" value=\"EYES\"/>"+
                        "</constraints>"+
                        "<parametermap>"+
                            "<parameter src=\"target\" dst=\"target\"/>" +
                        "</parametermap>"+
                        "<parameterdefaults>"+
                            "<parameterdefault name=\"param1\" value=\"value1\"/>"+
                        "</parameterdefaults>"+
                        "<RestGaze type=\"class\" class=\"asap.animationengine.gaze.DynamicRestGaze\"/>"+    
                "</RestGazeSpec>";
                //@formatter:on
        RestGazeSpec spec = new RestGazeSpec();
        spec.readXML(xmlString);
        
        String gazeBehaviorXML = 
             // @formatter:off
                "<gazeShift target=\"target1\" id=\"gaze1\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\" influence=\"EYES\"/>";
              //@formatter:on
        GazeShiftBehaviour b = new GazeShiftBehaviour("bml1",new XMLTokenizer(gazeBehaviorXML));
        assertTrue(spec.satisfiesConstraints(b));
        
        assertThat(spec.getRestGaze(),instanceOf(DynamicRestGaze.class));
    }
}
