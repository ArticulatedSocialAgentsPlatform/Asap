package asap.rsbembodiments.util;

import static org.junit.Assert.assertEquals;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.testutil.math.Quat4fTestUtil;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.primitives.Floats;

import asap.rsbembodiments.Rsbembodiments.Joint;

/**
 * Unit tests for VJointRsbUtils
 * @author hvanwelbergen
 * 
 */
public class VJointRsbUtilsTest
{
    private static final float PRECISION = 0.0001f;
    @Test
    public void toVJointRootOnly()
    {
        List<Joint> vjs = new ArrayList<Joint>();
        float qExpected[] = Quat4f.getQuat4fFromAxisAngleDegrees(1,1,1,50);
        vjs.add(Joint.newBuilder().setId("HumanoidRoot").setParentId("-").addAllLocalRotation(Floats.asList(qExpected))
                .addAllLocalTranslation(Floats.asList(1, 2, 3)).build());
        VJoint vj = VJointRsbUtils.toVJoint(vjs);
        assertEquals("HumanoidRoot", vj.getSid());
        assertEquals(null, vj.getParent());
        float q[] = Quat4f.getQuat4f();
        vj.getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExpected,q, PRECISION);
    }
    
    
}
