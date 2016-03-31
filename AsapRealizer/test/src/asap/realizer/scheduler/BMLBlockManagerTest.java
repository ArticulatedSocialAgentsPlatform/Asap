/*******************************************************************************
 *******************************************************************************/
package asap.realizer.scheduler;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.TimedPlanUnitState;

/**
 * Unit test cases for the BMLBlockManager
 * @author Herwin
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BMLScheduler.class)
public class BMLBlockManagerTest
{
    private BMLScheduler mockScheduler = mock(BMLScheduler.class);
    private BMLBlockManager bbm = new BMLBlockManager();
    private PegBoard pb = new PegBoard();

    @Before
    public void setup()
    {
        final Set<String> bml1Behs = new HashSet<String>();
        bml1Behs.add("beh1");
        final Set<String> beh1Syncs = new HashSet<String>();
        beh1Syncs.add("s1");
        when(mockScheduler.getBehaviours("bml1")).thenReturn(bml1Behs);
        when(mockScheduler.getTimedSyncs("bml1", "beh1")).thenReturn(beh1Syncs);
    }

    @Test
    public void testGetBMLBlocks()
    {
        BMLBBlock bb1 = new BMLBBlock("bml1", mockScheduler, pb);
        BMLBBlock bb2 = new BMLBBlock("bml2", mockScheduler, pb);
        bbm.addBMLBlock(bb1);
        bbm.addBMLBlock(bb2);
        assertTrue(bbm.getBMLBlocks().size() == 2);
        assertThat(bbm.getBMLBlocks(), hasItem("bml1"));
        assertThat(bbm.getBMLBlocks(), hasItem("bml2"));
    }

    @Test
    public void testSetState()
    {
        BMLBBlock bb1 = new BMLBBlock("bml1", mockScheduler, pb);
        bbm.addBMLBlock(bb1);
        bbm.startBlock("bml1", 0);
        assertEquals(TimedPlanUnitState.IN_EXEC, bbm.getBMLBlockState("bml1"));
    }

    @Test
    public void testActivate()
    {
        BMLBBlock bb1 = new BMLBBlock("bml1", mockScheduler, pb);
        bb1.setState(TimedPlanUnitState.PENDING);
        bbm.addBMLBlock(bb1);
        bbm.activateBlock("bml1", 0);
        assertEquals(TimedPlanUnitState.LURKING, bbm.getBMLBlockState("bml1"));
    }

    @Test
    public void testActivateRunning()
    {
        BMLBBlock bb1 = new BMLBBlock("bml1", mockScheduler, pb);
        bbm.addBMLBlock(bb1);
        bbm.startBlock("bml1", 0);
        bbm.activateBlock("bml1", 0);
        verify(mockScheduler, times(1)).startBlock("bml1", 0);
        // assertEquals(TimedPlanUnitState.IN_EXEC,bbm.getBMLBlockState("bml1"));
    }

    @Test
    public void testRemove()
    {
        BMLBBlock bb1 = new BMLBBlock("bml1", mockScheduler, pb);
        BMLBBlock bb2 = new BMLBBlock("bml2", mockScheduler, pb);
        bbm.addBMLBlock(bb1);
        bbm.addBMLBlock(bb2);
        bbm.removeBMLBlock("bml1", 0);
        assertEquals(1, bbm.getBMLBlocks().size());
        assertThat(bbm.getBMLBlocks(), hasItem("bml2"));
    }

    @Test
    public void testProgress()
    {
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "start", 0, 0));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml1", "beh1", "stroke", 1, 1));
        bbm.syncProgress(new BMLSyncPointProgressFeedback("bml2", "beh2", "start", 2, 2));

        assertEquals(2, bbm.getSyncsPassed("bml1", "beh1").size());
        assertThat(bbm.getSyncsPassed("bml1", "beh1"), hasItems("start", "stroke"));

        assertEquals(1, bbm.getSyncsPassed("bml2", "beh2").size());
        assertThat(bbm.getSyncsPassed("bml2", "beh2"), hasItem("start"));
    }
}
