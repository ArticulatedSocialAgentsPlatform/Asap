/*******************************************************************************
 *******************************************************************************/
package asap.faceengine;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.EmotionConverter;
import hmi.faceanimation.converters.FACSConverter;
import lombok.Delegate;
import asap.faceengine.faceunit.FaceUnit;
import asap.faceengine.faceunit.TimedFaceUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;

/**
 * Testing stub for FaceUnit
 * @author hvanwelbergen
 *
 */
public class StubFaceUnit implements FaceUnit
{
    @Delegate private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
        
    @Override
    public void play(double t) throws MUPlayException
    {
                
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {
                
    }
   

    @Override
    public double getPreferedDuration()
    {
        return 3;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {
                
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
                
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        return null;
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        return 0;
    }

    @Override
    public boolean hasValidParameters()
    {
        return true;
    }

    @Override
    public TimedFaceUnit createTFU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new TimedFaceUnit(bfm, bbPeg, bmlId, id, this, pb);
    }

    @Override
    public FaceUnit copy(FaceController fc, FACSConverter fconv, EmotionConverter econv)
    {
        return this;
    }

    @Override
    public void interruptFromHere()
    {
                
    }
}
