/*******************************************************************************
 *******************************************************************************/
package asap.realizertestutil.util;

import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.TimePeg;

/**
 * Test utilities to rapidly construct timepegs
 * @author welberge
 *
 */
public final class TimePegUtil
{
    private TimePegUtil(){};
    
    public static TimePeg createTimePeg(BMLBlockPeg bbPeg, double globalValue)
    {
        TimePeg tp = new TimePeg(bbPeg);
        tp.setGlobalValue(globalValue);
        return tp;
    }
    
    public static TimePeg createTimePeg(double globalValue)
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(globalValue);
        return tp;
    }
    
    public static TimePeg createAbsoluteTimePeg(double globalValue)
    {
        TimePeg tp = new TimePeg(BMLBlockPeg.GLOBALPEG);
        tp.setGlobalValue(globalValue);
        tp.setAbsoluteTime(true);
        return tp;
    }
}
