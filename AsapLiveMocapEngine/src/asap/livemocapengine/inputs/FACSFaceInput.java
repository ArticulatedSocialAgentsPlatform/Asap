package asap.livemocapengine.inputs;

import asap.utils.AUConfig;
import asap.utils.Sensor;

/**
 * A sensor for ekman's action units
 * @author welberge
 *
 */
public interface FACSFaceInput extends Sensor
{
    AUConfig[] getAUConfigs();
}
