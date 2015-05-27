package asap.realizerembodiments;

import asap.realizer.visualprosody.VisualProsodyProvider;
import hmi.environmentbase.Loader;

/**
 * Loader interface for VisualProsodyProvider  
 * @author Herwin
 *
 */
public interface VisualProsodyProviderLoader extends Loader
{
    VisualProsodyProvider getVisualProsodyProvider();
}
