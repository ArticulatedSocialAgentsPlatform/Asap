/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt.builder;

import saiba.bml.builder.BehaviourBuilder;
import saiba.bml.core.Behaviour;
import asap.bml.ext.bmlt.BMLTBehaviour;
import asap.bml.ext.bmlt.BMLTProcAnimationBehaviour;

/**
 * Builds a BMLTProcAnimationBehaviour
 * @author Herwin
 *
 */
public class BMLTProcAnimationBehaviourBuilder
{
    private final BehaviourBuilder builder;
    
    public BMLTProcAnimationBehaviourBuilder(String bmlId, String id, String name)
    {
        builder = new BehaviourBuilder(BMLTProcAnimationBehaviour.xmlTag(), bmlId, id);
        builder.param("name",name);
        builder.namespace(BMLTBehaviour.BMLTNAMESPACE);        
    }
    
    public Behaviour build()
    {
        return builder.build();
    }
}
