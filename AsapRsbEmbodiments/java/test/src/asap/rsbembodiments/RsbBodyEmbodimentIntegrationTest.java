package asap.rsbembodiments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.testutil.animation.HanimBody;
import hmi.testutil.math.Quat4fTestUtil;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import com.google.common.collect.Lists;

import rsb.RSBException;

/**
 * Unit test for the RsbBodyEmbodiment
 * @author hvanwelbergen
 * 
 */
public class RsbBodyEmbodimentIntegrationTest
{
    private static final float PRECISION = 0.001f;
    
    @Test(timeout = 4000)
    public void test() throws RSBException, InterruptedException
    {
        VJoint remoteBody = new VJoint(Hanim.HumanoidRoot, Hanim.HumanoidRoot);
        VJoint vRShoulder = new VJoint(Hanim.r_shoulder, Hanim.r_shoulder);
        vRShoulder.setTranslation(Vec3f.getVec3f(-1,1,0));
        remoteBody.addChild(vRShoulder);
        VJoint vLShoulder = new VJoint(Hanim.l_shoulder, Hanim.l_shoulder);
        vLShoulder.setTranslation(Vec3f.getVec3f(1,1,0));
        remoteBody.addChild(vLShoulder);
        VJoint vRElbow = new VJoint(Hanim.r_elbow, Hanim.r_elbow);
        vRElbow.setTranslation(Vec3f.getVec3f(0,-1,0));
        vRShoulder.addChild(vRElbow);
        
        StubBody sb = new StubBody(remoteBody);
        sb.startServer();
        RsbBodyEmbodiment body = new RsbBodyEmbodiment("idx", "billie");
        body.initialize(Lists.newArrayList(Hanim.HumanoidRoot, Hanim.vl5, Hanim.r_shoulder, Hanim.l_shoulder, Hanim.r_elbow));
        sb.deactivate();
        assertEquals(Hanim.HumanoidRoot, body.getAnimationVJoint().getSid());
        assertNotNull(body.getAnimationVJoint().getPartBySid(Hanim.l_shoulder));
        Thread.sleep(1000);
        assertThat(sb.getJointList(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.HumanoidRoot, Hanim.r_shoulder, Hanim.l_shoulder, Hanim.r_elbow));

        float qExpected[] = Quat4f.getQuat4fFromAxisAngle(0f, 1f, 0f, (float) Math.PI*0.3f);
        body.getAnimationVJoint().getPart(Hanim.HumanoidRoot).setRotation(qExpected);
        body.getAnimationVJoint().getPart(Hanim.r_shoulder).setRotation(qExpected);
        body.copy();
        Thread.sleep(500);
        float q[] = Quat4f.getQuat4f();
        remoteBody.getPart(Hanim.r_shoulder).getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExpected, q, PRECISION);
    }
}
