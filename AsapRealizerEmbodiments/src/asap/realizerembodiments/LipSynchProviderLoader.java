/*******************************************************************************
 *******************************************************************************/
package asap.realizerembodiments;

import hmi.environmentbase.Loader;
import asap.realizer.lipsync.LipSynchProvider;

/**
 * Loader interface for LipSynchProvider  
 * @author Herwin
 *
 */
public interface LipSynchProviderLoader extends Loader
{
    LipSynchProvider getLipSyncProvider();
}
