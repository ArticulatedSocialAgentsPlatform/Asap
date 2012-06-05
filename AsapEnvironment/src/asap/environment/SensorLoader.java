package asap.environment;

import asap.utils.Sensor;

/**
 * Loader that provides a sensor
 * @author welberge
 */
public interface SensorLoader extends Loader
{
    Sensor getSensor();
}
