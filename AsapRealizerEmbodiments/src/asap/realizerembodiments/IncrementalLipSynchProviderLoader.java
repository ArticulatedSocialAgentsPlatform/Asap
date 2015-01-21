/*******************************************************************************
 *******************************************************************************/
package asap.realizerembodiments;

import hmi.environmentbase.Loader;
import asap.realizer.lipsync.IncrementalLipSynchProvider;


/**
 * Loader interface for IncrementalLipSyncProvider  
 * @author Herwin
 *
 */
public interface IncrementalLipSynchProviderLoader extends Loader
{
    IncrementalLipSynchProvider getLipSyncProvider();
}
