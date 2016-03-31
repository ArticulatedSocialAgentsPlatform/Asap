/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import asap.animationengine.gaze.DynamicRestGaze;

/**
 * Unit tests for the RestGazeAssembler
 * @author hvanwelbergen
 *
 */
public class RestGazeAssemblerTest
{
    @Test
    public void test()
    {
        String str = "<RestGaze type=\"class\" class=\"asap.animationengine.gaze.DynamicRestGaze\"/>";
        RestGazeAssembler rga = new RestGazeAssembler();
        rga.readXML(str);
        assertThat(rga.getRestGaze(),instanceOf(DynamicRestGaze.class));
    }
}
