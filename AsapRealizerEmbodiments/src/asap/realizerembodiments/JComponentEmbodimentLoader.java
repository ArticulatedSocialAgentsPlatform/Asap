package asap.realizerembodiments;

import hmi.environmentbase.EmbodimentLoader;

/**
 * Loader for JComponentEmbodiments
 * @author Herwin
 *
 */
public interface JComponentEmbodimentLoader extends EmbodimentLoader
{
    @Override
    JComponentEmbodiment getEmbodiment();
}
