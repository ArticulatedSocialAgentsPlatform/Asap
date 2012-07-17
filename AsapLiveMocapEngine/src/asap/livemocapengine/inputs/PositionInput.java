package asap.livemocapengine.inputs;

import asap.utils.Sensor;

public interface PositionInput extends Sensor
{
    /**
     * Get the [x,y,z] value of the position
     */
    public float[] getPosition();
}
