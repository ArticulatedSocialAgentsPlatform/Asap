package asap.speechengine.ttsbinding;


/**
 * Produces a SAPITTSBinding 
 * @author hvanwelbergen
 *
 */
public class SAPITTSBindingFactory implements TTSBindingFactory
{
    @Override
    public TTSBinding createBinding()
    {
        return new SAPITTSBinding();
    }
}