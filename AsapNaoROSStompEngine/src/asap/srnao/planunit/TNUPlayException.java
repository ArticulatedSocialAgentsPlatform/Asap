/*******************************************************************************
 *******************************************************************************/
package asap.srnao.planunit;

import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Thrown when playback of a TNU fails
 * @author Daniel
 *
 */
public class TNUPlayException extends TimedPlanUnitPlayException
{
    public TimedNaoUnit timedNU;
    public TNUPlayException(String str, TimedNaoUnit tnu)
    {
        super(str,tnu);
        timedNU = tnu;        
    }
    
    private static final long serialVersionUID = -6983568422653209435L;
}
