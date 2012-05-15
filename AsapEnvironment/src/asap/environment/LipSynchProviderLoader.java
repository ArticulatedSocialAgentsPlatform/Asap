package asap.environment;

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
