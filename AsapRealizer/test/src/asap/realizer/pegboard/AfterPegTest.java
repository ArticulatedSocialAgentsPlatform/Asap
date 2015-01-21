/*******************************************************************************
 *******************************************************************************/
package asap.realizer.pegboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import asap.realizertestutil.util.TimePegUtil;

/**
 * unit tests for the AfterPeg
 * @author hvanwelbergen
 *
 */
public class AfterPegTest extends AbstractTimePegTest
{
    @Override
    public TimePeg createTimePeg(BMLBlockPeg peg)
    {
        TimePeg link = TimePegUtil.createTimePeg(peg, 10);
        return new AfterPeg(link,2);
    }
    
    private static final double TIME_PRECISION = 0.0001;
    
    @Test
    public void testMoveLinkOfUnknownAfterPeg()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10);
        AfterPeg aPeg = new AfterPeg(link,2);
        assertEquals(TimePeg.VALUE_UNKNOWN,aPeg.getGlobalValue(), TIME_PRECISION);
        link.setGlobalValue(14);
        assertEquals(TimePeg.VALUE_UNKNOWN,aPeg.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testMoveLinkBack()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10);
        AfterPeg aPeg = new AfterPeg(link,2);
        aPeg.setGlobalValue(13);
        link.setGlobalValue(8);
        assertEquals(13,aPeg.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetAfterPeg()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10);
        AfterPeg aPeg = new AfterPeg(link,2);
        aPeg.setGlobalValue(8);
        assertEquals(8,aPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(6,link.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetAfterPegLocal()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10);
        AfterPeg aPeg = new AfterPeg(link,2);
        aPeg.setLocalValue(8);
        assertEquals(8,aPeg.getLocalValue(), TIME_PRECISION);
        assertEquals(6,link.getLocalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetAfterPegLocalUnknownLink()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,TimePeg.VALUE_UNKNOWN);
        AfterPeg aPeg = new AfterPeg(link,2);
        aPeg.setLocalValue(8);
        assertEquals(8,aPeg.getLocalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN,link.getLocalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetAfterPegGlobalUnknownLink()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,TimePeg.VALUE_UNKNOWN);
        AfterPeg aPeg = new AfterPeg(link,2);
        aPeg.setGlobalValue(8);
        assertEquals(8,aPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN,link.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void setGlobalUnknown()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,10);
        AfterPeg aPeg = new AfterPeg(link,2);
        aPeg.setGlobalValue(TimePeg.VALUE_UNKNOWN);
        assertEquals(TimePeg.VALUE_UNKNOWN,aPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(10,link.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void setLocalUnknown()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,10);
        AfterPeg aPeg = new AfterPeg(link,2);
        aPeg.setLocalValue(TimePeg.VALUE_UNKNOWN);
        assertEquals(TimePeg.VALUE_UNKNOWN,aPeg.getLocalValue(), TIME_PRECISION);
        assertEquals(10,link.getLocalValue(), TIME_PRECISION);
    }
    
    @Test
    public void setLocalLinkMove()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,10);
        AfterPeg aPeg = new AfterPeg(link,2);
        aPeg.setLocalValue(15);
        link.setLocalValue(16);
        assertEquals(18,aPeg.getLocalValue(), TIME_PRECISION);
        assertEquals(16,link.getLocalValue(), TIME_PRECISION);
    }
    
    @Test
    public void setGlobalLinkMove()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,10);
        AfterPeg aPeg = new AfterPeg(link,2);
        aPeg.setGlobalValue(15);
        link.setGlobalValue(16);
        assertEquals(18,aPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(16,link.getGlobalValue(), TIME_PRECISION);
    }    
    
    @Test
    public void testBothUnknown()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,TimePeg.VALUE_UNKNOWN);
        AfterPeg aPeg = new AfterPeg(link,Double.MAX_VALUE);
        assertEquals(TimePeg.VALUE_UNKNOWN, aPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, link.getGlobalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, aPeg.getLocalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, link.getLocalValue(), TIME_PRECISION);
    }

    @Test
    public void testLinkFromDifferentBlock()
    {
        TimePeg link = TimePegUtil.createTimePeg(new BMLBlockPeg("bml2", 0.5),10);
        AfterPeg aPeg = new AfterPeg(link, 2, new BMLBlockPeg("bml1", 0.2));
        aPeg.setGlobalValue(9);
        assertEquals(9, aPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(7, link.getGlobalValue(), TIME_PRECISION);
        assertEquals(6.5, link.getLocalValue(),TIME_PRECISION);
    }
}
