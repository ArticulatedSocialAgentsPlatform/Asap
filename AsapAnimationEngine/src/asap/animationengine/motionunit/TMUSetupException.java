/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.motionunit;

import lombok.Getter;
import asap.realizer.planunit.TimedPlanUnitSetupException;

/**
 * A failure occurred when constructing tmu
 * @author hvanwelbergen
 *
 */
public class TMUSetupException extends TimedPlanUnitSetupException
{
    private static final long serialVersionUID = 1L;
    
    @Getter
    private final TimedAnimationUnit tmu;
    
    public TMUSetupException(String str, TimedAnimationUnit m)
    {
        super(str, m);
        tmu = m;        
    }
    
    
    public TMUSetupException(String str, TimedAnimationUnit m, Exception ex)
    {
        super(str,m,ex);
        tmu = m;        
    }
}