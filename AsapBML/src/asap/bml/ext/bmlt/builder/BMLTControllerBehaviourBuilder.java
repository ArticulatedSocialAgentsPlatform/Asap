/*******************************************************************************
 *******************************************************************************/
package asap.bml.ext.bmlt.builder;

import saiba.bml.builder.BehaviourBuilder;
import saiba.bml.core.Behaviour;
import asap.bml.ext.bmlt.BMLTBehaviour;
import asap.bml.ext.bmlt.BMLTControllerBehaviour;

/**
 * Builds a BMLTControllerBehaviour
 * @author Herwin
 *
 */
public class BMLTControllerBehaviourBuilder
{
    private final BehaviourBuilder builder;
    
    public BMLTControllerBehaviourBuilder(String bmlId, String id, String className)
    {
        builder = new BehaviourBuilder(BMLTControllerBehaviour.xmlTag(), bmlId, id);
        builder.namespace(BMLTBehaviour.BMLTNAMESPACE);
        builder.param("class", className);
    }
    
    public BMLTControllerBehaviourBuilder name(String name)
    {
        builder.param("name", name);
        return this;
    }
    
    public Behaviour build()
    {
        return builder.build();
    }
}
