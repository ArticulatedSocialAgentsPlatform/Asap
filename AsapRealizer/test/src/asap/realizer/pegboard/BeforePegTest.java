/*******************************************************************************
 *******************************************************************************/
package asap.realizer.pegboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import asap.realizertestutil.util.TimePegUtil;

/**
 * unit tests for the BeforePeg
 * @author hvanwelbergen
 *
 */
public class BeforePegTest extends AbstractTimePegTest
{
    private static final double TIME_PRECISION = 0.0001;
    
    @Override
    public TimePeg createTimePeg(BMLBlockPeg peg)
    {
        TimePeg link = TimePegUtil.createTimePeg(peg, 10);
        return new BeforePeg(link,2,peg);
    }
    
    @Test
    public void testMoveLinkOfUnknownAfterPeg()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10);
        BeforePeg bPeg = new BeforePeg(link,2);
        assertEquals(TimePeg.VALUE_UNKNOWN,bPeg.getGlobalValue(), TIME_PRECISION);
        link.setGlobalValue(14);
        assertEquals(TimePeg.VALUE_UNKNOWN,bPeg.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testMoveLinkForward()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10);
        BeforePeg bPeg = new BeforePeg(link,2);
        bPeg.setGlobalValue(13);
        link.setGlobalValue(15);
        assertEquals(13,bPeg.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetAfterPeg()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 10);
        BeforePeg aPeg = new BeforePeg(link,2);
        aPeg.setGlobalValue(13);
        assertEquals(13,aPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(11,link.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetAfterPegLocal()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG, 7);
        BeforePeg bPeg = new BeforePeg(link,2);
        bPeg.setLocalValue(10);
        assertEquals(10,bPeg.getLocalValue(), TIME_PRECISION);
        assertEquals(8,link.getLocalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetAfterPegLocalUnknownLink()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,TimePeg.VALUE_UNKNOWN);
        BeforePeg aPeg = new BeforePeg(link,2);
        aPeg.setLocalValue(8);
        assertEquals(8,aPeg.getLocalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN,link.getLocalValue(), TIME_PRECISION);
    }
    
    @Test
    public void testSetAfterPegGlobalUnknownLink()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,TimePeg.VALUE_UNKNOWN);
        BeforePeg aPeg = new BeforePeg(link,2);
        aPeg.setGlobalValue(8);
        assertEquals(8,aPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN,link.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void setGlobalUnknown()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,10);
        BeforePeg aPeg = new BeforePeg(link,2);
        aPeg.setGlobalValue(TimePeg.VALUE_UNKNOWN);
        assertEquals(TimePeg.VALUE_UNKNOWN,aPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(10,link.getGlobalValue(), TIME_PRECISION);
    }
    
    @Test
    public void setLocalUnknown()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,10);
        BeforePeg aPeg = new BeforePeg(link,2);
        aPeg.setLocalValue(TimePeg.VALUE_UNKNOWN);
        assertEquals(TimePeg.VALUE_UNKNOWN,aPeg.getLocalValue(), TIME_PRECISION);
        assertEquals(10,link.getLocalValue(), TIME_PRECISION);
    }
    
    @Test
    public void setLocalLinkMove()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,10);
        BeforePeg aPeg = new BeforePeg(link,2);
        aPeg.setLocalValue(16);
        link.setLocalValue(13);
        assertEquals(15,aPeg.getLocalValue(), TIME_PRECISION);
        assertEquals(13,link.getLocalValue(), TIME_PRECISION);
    }
    
    @Test
    public void setGlobalLinkMove()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,10);
        BeforePeg aPeg = new BeforePeg(link,2);
        aPeg.setGlobalValue(16);
        link.setGlobalValue(13);
        assertEquals(15,aPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(13,link.getGlobalValue(), TIME_PRECISION);
    }    
    
    @Test
    public void testBothUnknown()
    {
        TimePeg link = TimePegUtil.createTimePeg(BMLBlockPeg.GLOBALPEG,TimePeg.VALUE_UNKNOWN);
        BeforePeg aPeg = new BeforePeg(link,Double.MAX_VALUE);
        assertEquals(TimePeg.VALUE_UNKNOWN, aPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, link.getGlobalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, aPeg.getLocalValue(), TIME_PRECISION);
        assertEquals(TimePeg.VALUE_UNKNOWN, link.getLocalValue(), TIME_PRECISION);
    }

    @Test
    public void testLinkFromDifferentBlock()
    {
        TimePeg link = TimePegUtil.createTimePeg(new BMLBlockPeg("bml2", 0.5),9);
        BeforePeg bPeg = new BeforePeg(link, 2, new BMLBlockPeg("bml1", 0.2));
        bPeg.setGlobalValue(12);
        assertEquals(12, bPeg.getGlobalValue(), TIME_PRECISION);
        assertEquals(10, link.getGlobalValue(), TIME_PRECISION);
        assertEquals(9.5, link.getLocalValue(),TIME_PRECISION);
    }    
}
