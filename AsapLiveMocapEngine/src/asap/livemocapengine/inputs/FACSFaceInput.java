/*******************************************************************************
 *******************************************************************************/
package asap.livemocapengine.inputs;

import hmi.environmentbase.Sensor;
import hmi.faceembodiments.AUConfig;

/**
 * A sensor for ekman's action units
 * @author welberge
 *
 */
public interface FACSFaceInput extends Sensor
{
    AUConfig[] getAUConfigs();
}
