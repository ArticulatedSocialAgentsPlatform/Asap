package asap.animationengine.motionunit;

import lombok.Getter;

/**
 * A failure occurred when constructing tmu
 * @author hvanwelbergen
 *
 */
public class TMUSetupException extends Exception
{
    private static final long serialVersionUID = 1L;
    
    @Getter
    private final TimedAnimationUnit tmu;
    
    public TMUSetupException(String str, TimedAnimationUnit m)
    {
        super(str);
        tmu = m;        
    }
    
    
    public TMUSetupException(String str, TimedAnimationUnit m, Exception ex)
    {
        super(str);
        tmu = m;
        this.initCause(ex);
    }
}