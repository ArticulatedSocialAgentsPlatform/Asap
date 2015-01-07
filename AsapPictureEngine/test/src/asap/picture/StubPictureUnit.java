/*******************************************************************************
 *******************************************************************************/
package asap.picture;

import lombok.Delegate;
import asap.picture.display.PictureDisplay;
import asap.picture.planunit.PUPlayException;
import asap.picture.planunit.PUPrepareException;
import asap.picture.planunit.PictureUnit;
import asap.picture.planunit.TimedPictureUnit;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;

/**
 * Testing stub for the StubPictureUnit
 * @author hvanwelbergen
 * 
 */
public class StubPictureUnit implements PictureUnit
{
    @Delegate
    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

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
    public void prepareImages() throws PUPrepareException
    {

    }

    @Override
    public void startUnit(double time) throws PUPlayException
    {

    }

    @Override
    public void play(double t) throws PUPlayException
    {

    }

    @Override
    public void cleanup()
    {

    }

    @Override
    public TimedPictureUnit createTPU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        return new TimedPictureUnit(bfm, bbPeg, bmlId, id, this);
    }

    @Override
    public double getPreferedDuration()
    {
        return 3;
    }

    @Override
    public PictureUnit copy(PictureDisplay display)
    {
        return this;
    }
}
