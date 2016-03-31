/*******************************************************************************
 *******************************************************************************/
package asap.realizer.pegboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the OffsetPeg
 * @author hvanwelbergen
 *
 */
public class OffsetPegTest extends AbstractTimePegTest
{
    @Override
    public TimePeg createTimePeg(BMLBlockPeg peg)
    {
        TimePeg link = new TimePeg(peg);
        return new OffsetPeg(link,10);
    }
    
    @Test
    public void testSetOffsetPegGlobal()
    {
        TimePeg link = new TimePeg(new BMLBlockPeg("bml1",0.3));
        OffsetPeg peg = new OffsetPeg(link,2);
        peg.setGlobalValue(10);
        assertEquals(10, peg.getGlobalValue(), TIME_PRECISION);
        assertEquals(8,link.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetOffsetPegLinkGlobal()
    {
        TimePeg link = new TimePeg(new BMLBlockPeg("bml1",0.3));
        OffsetPeg peg = new OffsetPeg(link,2);
        link.setGlobalValue(10);
        assertEquals(12, peg.getGlobalValue(), TIME_PRECISION);
        assertEquals(10,link.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetOffsetPegLocal()
    {
        TimePeg link = new TimePeg(new BMLBlockPeg("bml1",0.3));
        OffsetPeg peg = new OffsetPeg(link,2);
        peg.setLocalValue(10);
        assertEquals(10, peg.getLocalValue(), TIME_PRECISION);
        assertEquals(8,link.getLocalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetOffsetPegLinkLocal()
    {
        TimePeg link = new TimePeg(new BMLBlockPeg("bml1",0.3));
        OffsetPeg peg = new OffsetPeg(link,2);
        link.setLocalValue(10);
        assertEquals(12, peg.getLocalValue(), TIME_PRECISION);
        assertEquals(10,link.getLocalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetOffsetPegGlobalValueLinkInDifferentBlock()
    {
        TimePeg link = new TimePeg(new BMLBlockPeg("bml1",0.3));
        OffsetPeg peg = new OffsetPeg(link,2,new BMLBlockPeg("bml2",0.5));
        peg.setGlobalValue(10);
        assertEquals(10, peg.getGlobalValue(), TIME_PRECISION);
        assertEquals(8,link.getGlobalValue(), TIME_PRECISION);
        assertEquals(7.7, link.getLocalValue(),TIME_PRECISION);
        assertEquals(9.5, peg.getLocalValue(),TIME_PRECISION);
    }
    
    @Test
    public void testSetOffsetPegLocalValueLinkInDifferentBlock()
    {
        TimePeg link = new TimePeg(new BMLBlockPeg("bml1",0.3));
        OffsetPeg peg = new OffsetPeg(link,2,new BMLBlockPeg("bml2",0.5));
        peg.setLocalValue(10);
        assertEquals(10.5, peg.getGlobalValue(), TIME_PRECISION);
        assertEquals(8.5,link.getGlobalValue(), TIME_PRECISION);
        assertEquals(8.2, link.getLocalValue(),TIME_PRECISION);
        assertEquals(10, peg.getLocalValue(),TIME_PRECISION);
    }
    
    @Test
    public void testSetOffsetPegToUnknown()
    {
        TimePeg link = new TimePeg(new BMLBlockPeg("bml1",0.3));
        OffsetPeg peg = new OffsetPeg(link,2);
        link.setGlobalValue(2);
        peg.setGlobalValue(TimePeg.VALUE_UNKNOWN);
        assertEquals(TimePeg.VALUE_UNKNOWN, link.getGlobalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, peg.getGlobalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, link.getLocalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, peg.getLocalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetLinkToUnknown()
    {
        TimePeg link = new TimePeg(new BMLBlockPeg("bml1",0.3));
        OffsetPeg peg = new OffsetPeg(link,2);
        peg.setGlobalValue(2);
        link.setGlobalValue(TimePeg.VALUE_UNKNOWN);
        assertEquals(TimePeg.VALUE_UNKNOWN, link.getGlobalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, peg.getGlobalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, link.getLocalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, peg.getLocalValue(), TIME_PRECISION);
    }
    
}
