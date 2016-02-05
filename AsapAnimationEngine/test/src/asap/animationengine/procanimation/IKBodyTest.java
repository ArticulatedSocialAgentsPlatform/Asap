/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.procanimation;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import hmi.animation.AnalyticalIKSolver;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Vec3f;
import hmi.testutil.animation.HanimBody;

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
        human.setTranslation(Vec3f.getVec3f(1, 1, 1));
        human.setRollPitchYawDegrees(0, 0, 20);
        human.getPart(Hanim.l_elbow).setTranslation(0, -0.5f, 0);
        human.getPart(Hanim.l_wrist).setTranslation(0, -0.3f, 0);
        human.getPart(Hanim.r_elbow).setTranslation(0, -0.5f, 0);
        human.getPart(Hanim.r_wrist).setTranslation(0, -0.3f, 0);

        human.getPart(Hanim.l_knee).setTranslation(0, -0.5f, 0);
        human.getPart(Hanim.l_ankle).setTranslation(0, -0.3f, 0);
        human.getPart(Hanim.r_knee).setTranslation(0, -0.5f, 0);
        human.getPart(Hanim.r_ankle).setTranslation(0, -0.3f, 0);

        //VJoint sceneJoint = new VJoint();
        //sceneJoint.addChild(human);
        
        body = new IKBody(human);
    }

    @Test
    public void testSetLocalLeftHand()
    {
        float refPos[] = Vec3f.getVec3f(0.1f, -0.5f, 0.1f);
        body.setLocalLeftHand(refPos);
        float vec[] = Vec3f.getVec3f();
        AnalyticalIKSolver.translateToLocalSystem(null, human.getPartBySid(Hanim.l_shoulder), human.getPartBySid(Hanim.l_wrist)
                .getPosition(), vec);
        assertVec3fEquals(refPos, vec, POSITION_PRECISION);
    }

    @Test
    public void testSetLocalRightHand()
    {
        float refPos[] = Vec3f.getVec3f(0.1f, -0.5f, 0.1f);
        body.setLocalRightHand(refPos);
        float vec[] = human.getPart(Hanim.r_wrist).getPosition();
        AnalyticalIKSolver.translateToLocalSystem(null, human.getPartBySid(Hanim.r_shoulder), human.getPartBySid(Hanim.r_wrist)
                .getPosition(), vec);
        assertVec3fEquals(refPos, vec, POSITION_PRECISION);
    }

    @Test
    public void testLeftHand()
    {
        float refPos[] = Vec3f.getVec3f(1 + 0.1f, 1 - 0.5f, 1 + 0.1f);
        body.setLeftHand(refPos);
        float vec[] = human.getPart(Hanim.l_wrist).getPosition();
        assertVec3fEquals(refPos, vec, POSITION_PRECISION);
    }

    @Test
    public void testLeftFoot()
    {
        float refPos[] = Vec3f.getVec3f(1 + 0.1f, 1 - 0.5f, 1 + 0.1f);
        body.setLeftFoot(refPos, true);
        float vec[] = human.getPart(Hanim.l_ankle).getPosition();
        assertVec3fEquals(refPos, vec, POSITION_PRECISION);
    }
}
