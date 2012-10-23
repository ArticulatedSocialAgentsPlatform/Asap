package asap.animationengine.ace;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import static org.junit.Assert.assertEquals;
import hmi.math.Vec3f;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

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
        gs.setST(new TPConstraint(0.1, 1));
        gs.addGuidingStroke(new LinearGStroke(GStrokePhaseID.STP_PREP, Vec3f.getZero()));
        gs.addGuidingStroke(new LinearGStroke(GStrokePhaseID.STP_PREP, Vec3f.getVec3f(1, 2, 3)));
        gs.addGuidingStroke(new LinearGStroke(GStrokePhaseID.STP_PREP, Vec3f.getVec3f(2, 3, 4)));
    }

//    @Test
//    public void testGetEndTimeOfEmptyGS()
//    {
//        GuidingSequence geEmpty = new GuidingSequence();
//        geEmpty.setST(new TPConstraint(2, 1));
//        assertEquals(2, geEmpty.getEndTime(), PRECISION);
//    }
//
//    @Test
//    public void testGetEndTime()
//    {
//        assertEquals(0.6, gs.getEndTime(), PRECISION);
//    }

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

//    @Test
//    public void testGetStartTimeOfStroke()
//    {
//        assertEquals(0.1, gs.getStartTimeOfStroke(0), PRECISION);
//        assertEquals(0.2, gs.getStartTimeOfStroke(1), PRECISION);
//        assertEquals(0.4, gs.getStartTimeOfStroke(2), PRECISION);
//    }
//
//    @Test
//    public void testGetStrokeIndexAt()
//    {
//        assertEquals(0, gs.getStrokeIndexAt(0));
//        assertEquals(0, gs.getStrokeIndexAt(0.1));
//        assertEquals(1, gs.getStrokeIndexAt(0.3));
//        assertEquals(2, gs.getStrokeIndexAt(0.5));
//        assertEquals(3, gs.getStrokeIndexAt(0.8));
//    }
//
//    @Test
//    public void testPostpone()
//    {
//        gs.postPone(0.1);
//        assertEquals(0.2, gs.getStartTimeOfStroke(0), PRECISION);
//        assertEquals(0.3, gs.getStartTimeOfStroke(1), PRECISION);
//        assertEquals(0.5, gs.getStartTimeOfStroke(2), PRECISION);
//        assertEquals(0.7, gs.getEndTime(), PRECISION);
//    }
//
//    @Test
//    public void testReplaceFirstStroke()
//    {
//        List<GuidingStroke> gSub = ImmutableList.<GuidingStroke> of(new LinearGStroke(GStrokePhaseID.STP_PREP, new TPConstraint(0.11, 1),
//                Vec3f.getVec3f(0, 1, 1)), new LinearGStroke(GStrokePhaseID.STP_PREP, new TPConstraint(0.12, 1), Vec3f.getVec3f(1, 0, 1)));
//        gs.replaceStroke(0, gSub);
//        assertEquals(0.11, gs.getStartTimeOfStroke(1), PRECISION);
//        assertEquals(0.12, gs.getStartTimeOfStroke(2), PRECISION);
//    }
//
//    @Test
//    public void testReplaceSecondStroke()
//    {
//        List<GuidingStroke> gSub = ImmutableList.<GuidingStroke> of(new LinearGStroke(GStrokePhaseID.STP_PREP, new TPConstraint(0.21, 1),
//                Vec3f.getVec3f(0, 1, 1)), new LinearGStroke(GStrokePhaseID.STP_PREP, new TPConstraint(0.22, 1), Vec3f.getVec3f(1, 0, 1)));
//        gs.replaceStroke(1, gSub);
//        assertEquals(0.21, gs.getStartTimeOfStroke(2), PRECISION);
//        assertEquals(0.22, gs.getStartTimeOfStroke(3), PRECISION);
//    }

}
