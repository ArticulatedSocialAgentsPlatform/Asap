/*******************************************************************************
 *******************************************************************************/
package asap.speechengine.ttsbinding;

import hmi.environmentbase.Loader;

/**
 * Loader for a TTSBinding 
 * @author hvanwelbergen
 */
public interface TTSBindingLoader extends Loader
{
    TTSBinding getTTSBinding();
}
