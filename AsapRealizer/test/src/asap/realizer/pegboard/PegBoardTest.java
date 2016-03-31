/*******************************************************************************
 *******************************************************************************/
package asap.realizer.pegboard;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test cases for the PegBoard
 * @author welberge
 * 
 */
public class PegBoardTest
{
    private PegBoard pb = new PegBoard();
    private static final double TIME_PRECISION = 0.0001;
    @Test
    public void testAddTimePeg()
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(10);
        pb.addTimePeg("bml1", "beh1", "start", tp);

        assertEquals(tp, pb.getTimePeg("bml1", "beh1", "start"));
        assertThat(pb.getPegKeys(tp), containsInAnyOrder(new PegKey("bml1", "beh1", "start")));
    }

    @Test
    public void testGetTimedSyncs()
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(10);
        pb.addTimePeg("bml1", "beh1", "start", tp);

        TimePeg tp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh1", "stroke", tp2);

        TimePeg tp3 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp3.setGlobalValue(1);
        pb.addTimePeg("bml2", "beh1", "strokeEnd", tp3);

        TimePeg tp4 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp4.setGlobalValue(1);
        pb.addTimePeg("bml1", "beh2", "start", tp4);

        TimePeg tp5 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp5.setGlobalValue(11);
        pb.addTimePeg("bml1", "beh1", "end", tp5);
        assertThat(pb.getTimedSyncs("bml1", "beh1"), containsInAnyOrder("start", "end"));
    }

    @Test
    public void testGetBehaviours()
    {
        pb.addTimePeg("bml1","beh1","start", new TimePeg(BMLBlockPeg.GLOBALPEG));
        pb.addTimePeg("bml1","beh2","start", new TimePeg(BMLBlockPeg.GLOBALPEG));
        pb.addTimePeg("bml2","beh3","start", new TimePeg(BMLBlockPeg.GLOBALPEG));
        pb.addTimePeg("bml1","beh1","end", new TimePeg(BMLBlockPeg.GLOBALPEG));
        assertThat(pb.getBehaviours("bml1"), containsInAnyOrder("beh1", "beh2"));
    }
    
    @Test
    public void testRemoveBehaviour()
    {
        TimePeg tp1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh1", "s1", tp1);
        tp1.setGlobalValue(1);

        TimePeg tp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml2", "beh1", "s4", tp2);
        tp2.setGlobalValue(1);

        pb.addTimePeg("bml1", "beh1", "s4", tp2);

        TimePeg tp4 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh2", "s5", tp4);
        tp4.setGlobalValue(1);

        assertNotSame(null, pb.getTimePeg("bml1", "beh1", "s1"));
        assertNotSame(null, pb.getTimePeg("bml1", "beh1", "s4"));
        assertTrue(pb.getTimedSyncs("bml1", "beh1").size() == 2);

        pb.removeBehaviour("bml1", "beh1");
        assertTrue(pb.getTimedSyncs("bml1", "beh1").size() == 0);
        assertSame(null, pb.getTimePeg("bml1", "beh1", "s1"));
        assertSame(null, pb.getTimePeg("bml1", "beh1", "s4"));

        assertThat(pb.getPegKeys(tp2), containsInAnyOrder(new PegKey("bml2", "beh1", "s4")));
        assertThat(pb.getPegKeys(tp1), hasSize(0));
    }

    @Test
    public void testOffsetPeg()
    {
        TimePeg tp1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp1.setGlobalValue(1);
        pb.addTimePeg("bml1", "beh1", "s1", tp1);

        TimePeg tpOffset = new OffsetPeg(tp1, 2, BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh1", "s2", tpOffset);

        assertThat(pb.getPegKeys(tp1), containsInAnyOrder(new PegKey("bml1", "beh1", "s1"), new PegKey("bml1", "beh1", "s2")));
        assertThat(pb.getPegKeys(tpOffset), containsInAnyOrder(new PegKey("bml1", "beh1", "s1"), new PegKey("bml1", "beh1", "s2")));
        assertEquals(tp1, pb.getTimePeg("bml1", "beh1", "s1"));
        assertEquals(tpOffset, pb.getTimePeg("bml1", "beh1", "s2"));
    }

    @Test
    public void testGetBehaviorClusterOneBeh()
    {
        TimePeg tp1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh1", "start", tp1);
        assertThat(pb.getBehaviorCluster("bml1", "beh1").getBehaviors(), containsInAnyOrder(new BehaviorKey("bml1", "beh1")));
    }

    @Test
    public void testGetBehaviorClusterLinkThroughBehavior()
    {
        TimePeg tp1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh1", "start", tp1);
        TimePeg tp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh1", "end", tp2);
        pb.addTimePeg("bml1", "beh2", "s1", tp2);

        assertThat(pb.getBehaviorCluster("bml1", "beh1").getBehaviors(),
                containsInAnyOrder(new BehaviorKey("bml1", "beh1"), new BehaviorKey("bml1", "beh2")));
        assertThat(pb.getBehaviorCluster("bml1", "beh2").getBehaviors(),
                containsInAnyOrder(new BehaviorKey("bml1", "beh1"), new BehaviorKey("bml1", "beh2")));
    }

    @Test
    public void testGetBehaviorClusterNoLink()
    {
        TimePeg tp1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh1", "start", tp1);
        TimePeg tp3 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh2", "s1", tp3);

        assertFalse(pb.getBehaviorCluster("bml1", "beh1").isGrounded());
        assertThat(pb.getBehaviorCluster("bml1", "beh1").getBehaviors(), containsInAnyOrder(new BehaviorKey("bml1", "beh1")));
        assertThat(pb.getBehaviorCluster("bml1", "beh2").getBehaviors(), containsInAnyOrder(new BehaviorKey("bml1", "beh2")));
    }

    @Test
    public void testGetBehaviorClusterThreeLinks()
    {
        TimePeg tp1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh1", "start", tp1);
        TimePeg tp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh1", "end", tp2);
        pb.addTimePeg("bml1", "beh2", "s1", tp2);
        TimePeg tp3 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh2", "s2", tp3);
        pb.addTimePeg("bml1", "beh3", "stroke", tp3);
        tp3.setAbsoluteTime(true);

        assertTrue(pb.getBehaviorCluster("bml1", "beh1").isGrounded());
        assertThat(pb.getBehaviorCluster("bml1", "beh1").getBehaviors(),
                containsInAnyOrder(new BehaviorKey("bml1", "beh1"), new BehaviorKey("bml1", "beh2"), new BehaviorKey("bml1", "beh3")));
        assertTrue(pb.getBehaviorCluster("bml1", "beh2").isGrounded());
        assertThat(pb.getBehaviorCluster("bml1", "beh2").getBehaviors(),
                containsInAnyOrder(new BehaviorKey("bml1", "beh1"), new BehaviorKey("bml1", "beh2"), new BehaviorKey("bml1", "beh3")));
        assertTrue(pb.getBehaviorCluster("bml1", "beh3").isGrounded());
        assertThat(pb.getBehaviorCluster("bml1", "beh3").getBehaviors(),
                containsInAnyOrder(new BehaviorKey("bml1", "beh1"), new BehaviorKey("bml1", "beh2"), new BehaviorKey("bml1", "beh3")));

    }
    
    @Test
    public void testGetBehaviorClusterWithOffsetPeg()
    {
        TimePeg tp1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh1", "start", tp1);
        OffsetPeg tp2 = new OffsetPeg(tp1,1);
        pb.addTimePeg("bml1", "beh2", "end", tp2);
        
        assertFalse(pb.getBehaviorCluster("bml1", "beh1").isGrounded());
        assertThat(pb.getBehaviorCluster("bml1", "beh1").getBehaviors(),
                containsInAnyOrder(new BehaviorKey("bml1", "beh1"), new BehaviorKey("bml1", "beh2")));
        assertFalse(pb.getBehaviorCluster("bml1", "beh2").isGrounded());
        assertThat(pb.getBehaviorCluster("bml1", "beh2").getBehaviors(),
                containsInAnyOrder(new BehaviorKey("bml1", "beh1"), new BehaviorKey("bml1", "beh2")));
    }
    
    @Test
    public void testShiftCluster()
    {
        final double tp1PegInitialValue = 10;
        final double tp2PegInitialValue = 12;
        final double shift = 2;
        TimePeg tp1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh1", "start", tp1);
        pb.addTimePeg("bml1", "beh2", "stroke", tp1);
        tp1.setGlobalValue(tp1PegInitialValue);
        TimePeg tp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        pb.addTimePeg("bml1", "beh2", "end", tp2);
        tp2.setGlobalValue(tp2PegInitialValue);
        
        BehaviorCluster bc = new BehaviorCluster(ImmutableSet.of(new BehaviorKey("bml1","beh1"), new BehaviorKey("bml1","beh2")),false);
        pb.shiftCluster(bc, shift);
        assertEquals(tp1PegInitialValue+shift, tp1.getGlobalValue(),TIME_PRECISION);
        assertEquals(tp2PegInitialValue+shift, tp2.getGlobalValue(),TIME_PRECISION);
    }
}
