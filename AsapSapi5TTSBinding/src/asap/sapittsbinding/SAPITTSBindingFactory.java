/*******************************************************************************
 *******************************************************************************/
package asap.sapittsbinding;

import asap.speechengine.ttsbinding.TTSBinding;
import asap.speechengine.ttsbinding.TTSBindingFactory;


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