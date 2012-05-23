package asap.animationengine.gesturebinding;

import hmi.util.Resources;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;

import asap.animationengine.restpose.SkeletonPoseRestPose;
import asap.realizer.pegboard.PegBoard;

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
        RestPoseAssembler rpa = new RestPoseAssembler(res, new PegBoard());
        rpa.readXML(str);
        assertThat(rpa.getRestPose(),instanceOf(SkeletonPoseRestPose.class));
    }
}
