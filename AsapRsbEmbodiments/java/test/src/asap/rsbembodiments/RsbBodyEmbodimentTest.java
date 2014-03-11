package asap.rsbembodiments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hmi.animation.Hanim;
import hmi.testutil.animation.HanimBody;

import org.junit.Test;

import com.google.common.collect.Lists;

import rsb.RSBException;

/**
 * Unit test for the RsbBodyEmbodiment
 * @author hvanwelbergen
 * 
 */
public class RsbBodyEmbodimentTest
{
    @Test//(timeout=2000)
    public void test() throws RSBException, InterruptedException
    {
        StubBody sb = new StubBody(HanimBody.getLOA1HanimBody());
        sb.startServer();
        RsbBodyEmbodiment body = new RsbBodyEmbodiment("idx", "billie");
        body.initialize(Lists.newArrayList(Hanim.all_body_joints));
        sb.deactivate();
        assertEquals(Hanim.HumanoidRoot, body.getAnimationVJoint().getSid());
        assertNotNull(body.getAnimationVJoint().getPartBySid(Hanim.l_shoulder));
    }
}
