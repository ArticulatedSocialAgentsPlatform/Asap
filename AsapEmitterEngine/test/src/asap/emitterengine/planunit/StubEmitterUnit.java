/*******************************************************************************
 *******************************************************************************/
package asap.emitterengine.planunit;

import lombok.Delegate;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;

/**
 * Testing stub for the EmitterUnit
 * @author Herwin
 *
 */
public class StubEmitterUnit implements EmitterUnit
{
    @Delegate
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();  
    
    public void cleanup()
    {
        
    }

    public TimedEmitterUnit createTEU(FeedbackManager bfm, BMLBlockPeg bbPeg,String bmlId,String id)
    {
        return new TimedEmitterUnit(bfm,bbPeg,bmlId,id,this);
    }
    
    @Override
    public void setFloatParameterValue(String name, float value)
    {
    }
    
    @Override
    public boolean hasValidParameters()
    {
        return true;
    }

    @Override
    public void setParameterValue(String name, String value)
    {
    }

    @Override
    public String getParameterValue(String name)
    {
        return null;
    }

    @Override
    public void play(double t)
    {

    }    

    @Override
    public double getPreferedDuration()
    {
        return 0;
    }

    @Override
    public float getFloatParameterValue(String name)
    {
        return 0;
    }

    @Override
    public void startUnit(double t)
    {
                
    }
}
