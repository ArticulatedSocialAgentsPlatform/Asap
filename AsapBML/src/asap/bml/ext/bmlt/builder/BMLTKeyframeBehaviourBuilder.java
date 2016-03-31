/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt.builder;

import saiba.bml.builder.BehaviourBuilder;
import saiba.bml.core.Behaviour;
import asap.bml.ext.bmlt.BMLTBehaviour;
import asap.bml.ext.bmlt.BMLTKeyframeBehaviour;

/**
 * Builds a BMLTKeyframeBehaviour
 * @author Herwin
 *
 */
public class BMLTKeyframeBehaviourBuilder
{
    private final BehaviourBuilder builder;
    
    public BMLTKeyframeBehaviourBuilder(String bmlId, String id)
    {
        builder = new BehaviourBuilder(BMLTKeyframeBehaviour.xmlTag(), bmlId, id);
        builder.namespace(BMLTBehaviour.BMLTNAMESPACE);
    }
    
    public BMLTKeyframeBehaviourBuilder name(String name)
    {
        builder.param("name", name);
        return this;
    }
    
    public BMLTKeyframeBehaviourBuilder content(String content)
    {
        builder.content(content);
        return this;
    }
    
    public Behaviour build()
    {
        return builder.build();
    }
}
