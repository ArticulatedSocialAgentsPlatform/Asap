/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.inputs;

import hmi.environmentbase.Sensor;

/**
 * Input element for euler angles
 * @author welberge
 *
 */
public interface EulerInput extends Sensor
{
    float getPitchDegrees();
    float getYawDegrees();
    float getRollDegrees();
}
