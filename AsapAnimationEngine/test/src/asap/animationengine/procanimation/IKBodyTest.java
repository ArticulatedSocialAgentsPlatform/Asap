package asap.animationengine.procanimation;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Vec3f;
import hmi.testutil.animation.HanimBody;
import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests cases for IKBody
 * @author hvanwelbergen
 */
public class IKBodyTest
{
    private static final float POSITION_PRECISION = 0.01f;
    private VJoint human;
    private IKBody body;
    
    @Before
    public void setup()
    {
        human = HanimBody.getLOA1HanimBody();
        human.getPart(Hanim.l_elbow).setTranslation(0,-0.5f,0);
        human.getPart(Hanim.l_wrist).setTranslation(0,-0.3f,0);
        human.getPart(Hanim.r_elbow).setTranslation(0,-0.5f,0);
        human.getPart(Hanim.r_wrist).setTranslation(0,-0.3f,0);
        body = new IKBody(human);
    }
    
    @Test
    public void testSetLocalLeftHand()
    {
        float refPos[] = Vec3f.getVec3f(0.1f, -0.5f, 0.1f);
        body.setLocalLeftHand(refPos);
        float vec[]=Vec3f.getVec3f();
        human.getPart(Hanim.l_wrist).getPathTranslation(human, vec);
        assertVec3fEquals(refPos,vec,POSITION_PRECISION);
    }
    
    @Test
    public void testSetLocalRightHand()
    {
        float refPos[] = Vec3f.getVec3f(0.1f, -0.5f, 0.1f);
        body.setLocalRightHand(refPos);
        float vec[]=Vec3f.getVec3f();
        human.getPart(Hanim.r_wrist).getPathTranslation(human, vec);
        assertVec3fEquals(refPos,vec,POSITION_PRECISION);
    }
}
