/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt.builder;

import saiba.bml.builder.BehaviourBuilder;
import saiba.bml.core.Behaviour;
import asap.bml.ext.bmlt.BMLTAudioFileBehaviour;
import asap.bml.ext.bmlt.BMLTBehaviour;

/**
 * Builds a BMLTAudioBehaviour
 * @author Herwin
 */
public class BMLTAudioFileBehaviourBuilder
{
    private final BehaviourBuilder builder;
    
    public BMLTAudioFileBehaviourBuilder(String bmlId, String id, String filename)
    {
        builder = new BehaviourBuilder(BMLTAudioFileBehaviour.xmlTag(), bmlId, id);
        builder.namespace(BMLTBehaviour.BMLTNAMESPACE);
        builder.param("fileName", filename);
    }
    
    public Behaviour build()
    {
        return builder.build();
    }
}
