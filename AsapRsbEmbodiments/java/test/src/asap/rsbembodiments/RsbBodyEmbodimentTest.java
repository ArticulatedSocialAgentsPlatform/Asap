package asap.rsbembodiments;

import static org.junit.Assert.assertEquals;
import hmi.animation.Hanim;
import hmi.testutil.animation.HanimBody;

import org.junit.Test;

import rsb.RSBException;

/**
 * Unit test for the RsbBodyEmbodiment
 * @author hvanwelbergen
 * 
 */
public class RsbBodyEmbodimentTest
{
    @Test(timeout=500)
    public void test() throws RSBException, InterruptedException
    {
        StubBody sb = new StubBody(HanimBody.getLOA1HanimBody());
        sb.startServer();
        RsbBodyEmbodiment body = new RsbBodyEmbodiment("idx", "billie");
        body.initialize();        
        sb.deactivate();
        assertEquals(Hanim.HumanoidRoot, body.getAnimationVJoint().getSid());
    }
}
