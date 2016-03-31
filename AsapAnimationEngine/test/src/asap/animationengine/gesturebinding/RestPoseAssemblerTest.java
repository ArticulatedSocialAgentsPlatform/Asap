/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import hmi.util.Resources;

import org.junit.Test;

import asap.animationengine.restpose.SkeletonPoseRestPose;

/**
 * Unit tests for the RestPoseAssembler
 * @author welberge
 *
 */
public class RestPoseAssemblerTest
{
    @Test
    public void testReadXML()
    {
        Resources res = new Resources("Humanoids/armandia/restposes");
        String str = "<RestPose type=\"SkeletonPose\" file=\"sitting.xml\"/>";
        RestPoseAssembler rpa = new RestPoseAssembler(res);
        rpa.readXML(str);
        assertThat(rpa.getRestPose(),instanceOf(SkeletonPoseRestPose.class));
    }
}
