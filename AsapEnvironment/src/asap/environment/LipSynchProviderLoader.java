package asap.environment;

import hmi.elckerlyc.lipsync.LipSynchProvider;

/**
 * Loader interface for LipSynchProvider  
 * @author Herwin
 *
 */
public interface LipSynchProviderLoader extends Loader
{
    LipSynchProvider getLipSyncProvider();
}
