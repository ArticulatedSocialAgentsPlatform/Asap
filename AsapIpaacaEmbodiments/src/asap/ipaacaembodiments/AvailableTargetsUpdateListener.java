package asap.ipaacaembodiments;

/**
 * Gets called by the IpaacaEmbodiment whenever a new list of available joints/morph targets is provided by the renderer
 * @author hvanwelbergen
 *
 */
public interface AvailableTargetsUpdateListener
{
    public void update();
}
