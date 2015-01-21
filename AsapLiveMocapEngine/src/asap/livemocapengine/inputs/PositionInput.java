/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.inputs;

import hmi.environmentbase.Sensor;

public interface PositionInput extends Sensor
{
    /**
     * Get the [x,y,z] value of the position
     */
    float[] getPosition();
}
