/*******************************************************************************
 *******************************************************************************/
package asap.realizer.anticipator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;

/**
 * Unit tests for the anticipator
 * @author hvanwelbergen
 *
 */
public class AnticipatorTest
{
    private PegBoard pegBoard = new PegBoard();
    private static final String ANTICIPATOR_ID = "anticip1";
    private Anticipator anticip = new Anticipator(ANTICIPATOR_ID, pegBoard);
    private static final double PRECISION = 0.01;
    
    @Test
    public void testAdd()
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        anticip.addSynchronisationPoint("test1", tp);
        assertEquals(tp, anticip.getSynchronisationPoint("test1"));
        assertEquals(tp, pegBoard.getTimePeg(BMLBlockPeg.ANTICIPATOR_PEG_ID,ANTICIPATOR_ID,"test1"));
    }
    
    @Test
    public void testUpdate()
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        anticip.addSynchronisationPoint("test1", tp);
        anticip.setSynchronisationPoint("test1", 10);
        assertEquals(10, anticip.getSynchronisationPoint("test1").getGlobalValue(), PRECISION);
        assertEquals(10, tp.getGlobalValue(), PRECISION);        
    }
    
    @Test
    public void testgetTimePegs()
    {
        TimePeg tp1 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        TimePeg tp2 = new TimePeg(BMLBlockPeg.GLOBALPEG);
        anticip.addSynchronisationPoint("test1", tp1);
        anticip.addSynchronisationPoint("test2", tp2);
        assertThat(anticip.getTimePegs(),IsIterableContainingInAnyOrder.containsInAnyOrder(tp1,tp2));
    }
}
