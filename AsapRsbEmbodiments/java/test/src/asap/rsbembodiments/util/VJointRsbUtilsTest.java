package asap.rsbembodiments.util;

import static org.junit.Assert.assertEquals;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.testutil.animation.HanimBody;
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

    private Joint createJoint(String id, String parent, float q[], float t[])
    {
        return Joint.newBuilder().setId(id).setParentId(parent).addAllLocalRotation(Floats.asList(q))
                .addAllLocalTranslation(Floats.asList(t)).build();
    }

    @Test
    public void testToVJointRootOnly()
    {
        List<Joint> vjs = new ArrayList<Joint>();
        float qExpected[] = Quat4f.getQuat4fFromAxisAngleDegrees(1, 1, 1, 50);
        vjs.add(createJoint("HumanoidRoot", "-", qExpected, Vec3f.getVec3f(1,2,3)));
        VJoint vj = VJointRsbUtils.toVJoint(vjs);
        assertEquals("HumanoidRoot", vj.getSid());
        assertEquals(null, vj.getParent());
        float q[] = Quat4f.getQuat4f();
        vj.getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExpected, q, PRECISION);
    }

    @Test
    public void testToVJoint()
    {
        List<Joint> vjs = new ArrayList<Joint>();
        float qExpected[] = Quat4f.getQuat4fFromAxisAngleDegrees(1, 1, 1, 50);
        float tExpected[] = Vec3f.getVec3f(1,2,3);
        vjs.add(createJoint(Hanim.HumanoidRoot, "-", qExpected, tExpected));   
        vjs.add(createJoint(Hanim.r_shoulder, Hanim.HumanoidRoot, qExpected, tExpected));
        vjs.add(createJoint(Hanim.r_elbow, Hanim.r_shoulder, qExpected, tExpected));
        vjs.add(createJoint(Hanim.l_shoulder, Hanim.HumanoidRoot, qExpected, tExpected));
        vjs.add(createJoint(Hanim.l_elbow, Hanim.l_shoulder, qExpected, tExpected));
        VJoint vj = VJointRsbUtils.toVJoint(vjs);
        assertEquals(5, vj.getParts().size());
    }
    
    @Test
    public void testConvertBackAndForth()
    {
        VJoint vj = VJointRsbUtils.toVJoint(VJointRsbUtils.toRsbJointList(HanimBody.getLOA1HanimBody()));
        assertEquals(Hanim.HumanoidRoot, vj.getSid());
    }
}
