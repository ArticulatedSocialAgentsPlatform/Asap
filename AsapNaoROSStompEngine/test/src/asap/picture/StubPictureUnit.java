package asap.picture;

import lombok.Delegate;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.srnao.display.PictureDisplay;
import asap.srnao.planunit.NUPlayException;
import asap.srnao.planunit.NUPrepareException;
import asap.srnao.planunit.NaoUnit;
import asap.srnao.planunit.TimedNaoUnit;

/**
 * Testing stub for the StubPictureUnit
 * @author hvanwelbergen
 * 
 */
public class StubPictureUnit implements NaoUnit
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
    public void prepareImages() throws NUPrepareException
    {

    }

    @Override
    public void startUnit(double time) throws NUPlayException
    {

    }

    @Override
    public void play(double t) throws NUPlayException
    {

    }

    @Override
    public void cleanup()
    {

    }

    @Override
    public TimedNaoUnit createTNU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        return new TimedNaoUnit(bfm, bbPeg, bmlId, id, this);
    }

    @Override
    public double getPreferedDuration()
    {
        return 3;
    }

    @Override
    public NaoUnit copy(PictureDisplay display)
    {
        return this;
    }
}
