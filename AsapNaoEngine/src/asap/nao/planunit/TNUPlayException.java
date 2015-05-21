/*******************************************************************************
 *******************************************************************************/
package asap.nao.planunit;

import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Thrown when playback of a TNU fails
 * @author Kaspar ten Buuren
 *
 */
@SuppressWarnings("serial")
public class TNUPlayException extends TimedPlanUnitPlayException
{
    public TimedNaoUnit timedNU;
    public TNUPlayException(String str, TimedNaoUnit tnu)
    {
        super(str,tnu);
        timedNU = tnu;        
    }
}
