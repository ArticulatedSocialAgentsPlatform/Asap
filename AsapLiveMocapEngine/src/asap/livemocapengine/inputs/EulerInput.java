package asap.livemocapengine.inputs;

import asap.utils.Sensor;

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
