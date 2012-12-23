package asap.animationengine.gaze;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.MUSetupException;
import asap.motionunit.MUPlayException;

public class DynamicTorsoGazeMU extends GazeMU
{
    public DynamicTorsoGazeMU()
    {
        setupKeyPositions();
    }
    
    @Override
    public void play(double t) throws MUPlayException
    {
        if (t < RELATIVE_READY_TIME)
        {

        }
        else if (t > 0.75)
        {

        }
        else
        {

        }
    }

    @Override
    public void setStartPose() throws MUPlayException
    {

    }

    @Override
    public double getReadyDuration()
    {
        // TODO
        return 1;
    }

    @Override
    public void setDurations(double prepDur, double relaxDur)
    {
        // TODO
    }

    @Override
    public void setEndRotation(float[] gazeDir)
    {

    }

    @Override
    public GazeMU copy(AnimationPlayer p) throws MUSetupException
    {
        // TODO
        return null;
    }
}
