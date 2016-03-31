/*******************************************************************************
 *******************************************************************************/
package asap.emitterengine.planunit;

import asap.realizer.planunit.TimedPlanUnitPlayException;

/**
 * Thrown when playback of a TEU fails
 * @author Dennis
 *
 */
public class TEUPlayException extends TimedPlanUnitPlayException
{
    public TimedEmitterUnit timedEU;
    public TEUPlayException(String str, TimedEmitterUnit teu)
    {
        super(str,teu);
        timedEU = teu;        
    }
    
    private static final long serialVersionUID = -6983533422653209435L;
}
