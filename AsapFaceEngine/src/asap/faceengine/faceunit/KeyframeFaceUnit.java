/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.faceunit;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.FaceInterpolator;

import java.util.List;

import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.ParameterNotFoundException;

/**
 * Skeleton implementation for different KeyframeFaceUnits
 * @author herwinvw
 *
 */
public abstract class KeyframeFaceUnit implements FaceUnit
{

    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    protected final FaceInterpolator mi;
    protected FaceController faceController;

    public KeyframeFaceUnit(FaceInterpolator mi)
    {
        this.mi = mi;
    }
    
    protected float[] getInterpolatedValue(double t) 
    {
        return mi.interpolate(mi.getStartTime() + getPreferedDuration() * t);
    }

    protected void setupCopy(KeyframeFaceUnit copy, FaceController fc)
    {
        copy.setFaceController(fc);
        for (KeyPosition keypos : getKeyPositions())
        {
            copy.addKeyPosition(keypos.deepCopy());
        }
    }

    @Override
    public double getPreferedDuration()
    {
        return mi.getEndTime() - mi.getStartTime();
    }

    public void setFaceController(FaceController fc)
    {
        faceController = fc;
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {

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
        throw new ParameterNotFoundException(name);
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        throw new ParameterNotFoundException(name);
    }

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    @Override
    public List<KeyPosition> getKeyPositions()
    {
        return keyPositionManager.getKeyPositions();
    }

    @Override
    public void setKeyPositions(List<KeyPosition> p)
    {
        keyPositionManager.setKeyPositions(p);
    }

    @Override
    public KeyPosition getKeyPosition(String id)
    {
        return keyPositionManager.getKeyPosition(id);
    }

    @Override
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
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
    public void interruptFromHere()
    {
        // no interrupt implementation
    }

}
