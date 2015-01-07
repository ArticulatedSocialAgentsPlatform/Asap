/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import hmi.math.Vec3f;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the GuidingSequence
 * @author hvanwelbergen
 * 
 */
public class GuidingSequenceTest
{
    private GuidingSequence gs = new GuidingSequence();
    private static final float PRECISION = 0.0001f;

    @Before
    public void before()
    {
        gs.setStartTime(0.1);
        gs.addGuidingStroke(new LinearGStroke(GStrokePhaseID.STP_PREP, Vec3f.getZero()));
        gs.addGuidingStroke(new LinearGStroke(GStrokePhaseID.STP_STROKE, Vec3f.getVec3f(1, 2, 3)));
        gs.addGuidingStroke(new LinearGStroke(GStrokePhaseID.STP_STROKE, Vec3f.getVec3f(2, 3, 4)));
        gs.addGuidingStroke(new LinearGStroke(GStrokePhaseID.STP_STROKE, Vec3f.getVec3f(1, 2, 3)));
        gs.addGuidingStroke(new LinearGStroke(GStrokePhaseID.STP_STROKE, Vec3f.getVec3f(2, 3, 4)));
        gs.getStroke(0).setEDt(1);
        gs.getStroke(1).setEDt(1);
        gs.getStroke(2).setEDt(1);
        gs.getStroke(3).setEDt(1);
        gs.getStroke(4).setEDt(1);
    }

    @Test
    public void testGetEndPos()
    {

        float[] result = Vec3f.getVec3f();
        gs.getEndPos(result);
        assertVec3fEquals(2f, 3f, 4f, result, PRECISION);
    }

    @Test
    public void testGetStartPosOfStroke()
    {
        gs.setStartPos(Vec3f.getVec3f(0, 0, 1));
        float[] result = Vec3f.getVec3f();
        gs.getStartPosOfStroke(result, 0);
        assertVec3fEquals(0f, 0f, 1f, result, PRECISION);
        gs.getStartPosOfStroke(result, 1);
        assertVec3fEquals(0f, 0f, 0f, result, PRECISION);
        gs.getStartPosOfStroke(result, 2);
        assertVec3fEquals(1f, 2f, 3f, result, PRECISION);
    }

    @Test
    public void testgetEndVelocity()
    {
        assertVec3fEquals(0f, 0f, 0f, gs.getStrokeEndVelocityOf(0), PRECISION);;
        assertThat(Vec3f.length(gs.getStrokeEndVelocityOf(1)), greaterThan(0f));
        assertVec3fEquals(0f, 0f, 0f, gs.getStrokeEndVelocityOf(2), PRECISION);
        assertVec3fEquals(0f, 0f, 0f, gs.getStrokeEndVelocityOf(3), PRECISION);
        assertVec3fEquals(0f, 0f, 0f, gs.getStrokeEndVelocityOf(4), PRECISION);;
    }    
}
